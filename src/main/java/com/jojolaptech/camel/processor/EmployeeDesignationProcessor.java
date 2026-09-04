package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.JobTitle;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeDesignationEntity;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeDesignationRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Step 22v: jobTitle → hrm_employee_designation on the company's primary branch only.
 */
@Component
@RequiredArgsConstructor
public class EmployeeDesignationProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeDesignationProcessor.class);

    private final PgBranchRepository branchRepository;
    private final PgEmployeeDesignationRepository designationRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<JobTitle> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(JobTitle::getId).collect(Collectors.toSet());
        Set<Long> existingIds = designationRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> companyMysqlIds = batch.stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, BranchEntity> primaryBranchByCompanyMysqlId = new HashMap<>();
        for (BranchEntity branch : branchRepository.findByCompanyMysqlIdIn(companyMysqlIds)) {
            if (branch.getCompany() == null || branch.getCompany().getMysqlId() == null) {
                continue;
            }
            Long companyMysqlId = branch.getCompany().getMysqlId();
            BranchEntity existing = primaryBranchByCompanyMysqlId.get(companyMysqlId);
            if (existing == null || compareBranch(branch, existing) < 0) {
                primaryBranchByCompanyMysqlId.put(companyMysqlId, branch);
            }
        }

        List<EmployeeDesignationEntity> toSave = new ArrayList<>();
        for (JobTitle source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getCompany() == null) {
                log.warn("Skipping jobTitle id={}, missing company", source.getId());
                continue;
            }
            BranchEntity primaryBranch = primaryBranchByCompanyMysqlId.get(source.getCompany().getId());
            if (primaryBranch == null) {
                log.warn(
                        "Skipping jobTitle id={}, no migrated branch for company mysqlId={}",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }
            EmployeeDesignationEntity mapped =
                    EmployeeProfileMigrationMapper.fromJobTitle(source, primaryBranch.getId());
            if (mapped == null) {
                log.warn("Skipping jobTitle id={}, missing title", source.getId());
                continue;
            }
            toSave.add(mapped);
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            designationRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }

    private static int compareBranch(BranchEntity left, BranchEntity right) {
        boolean leftHead = Boolean.TRUE.equals(left.getHeadoffice());
        boolean rightHead = Boolean.TRUE.equals(right.getHeadoffice());
        if (leftHead != rightHead) {
            return leftHead ? -1 : 1;
        }
        long leftMysql = left.getMysqlId() == null ? Long.MAX_VALUE : left.getMysqlId();
        long rightMysql = right.getMysqlId() == null ? Long.MAX_VALUE : right.getMysqlId();
        return Long.compare(leftMysql, rightMysql);
    }
}
