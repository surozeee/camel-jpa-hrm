package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyBranchPayrollHeading;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.BranchSalaryBreakdownEntity;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchSalaryBreakdownRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Step 9p: companyBranchPayrollHeading (active) → set branch_id on BranchSalaryBreakdownEntity when
 * found by companyPayrollHeading.id as mysql_id and branch_id is null.
 */
@Component
@RequiredArgsConstructor
public class CompanyBranchPayrollHeadingProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(CompanyBranchPayrollHeadingProcessor.class);

    private final PgBranchSalaryBreakdownRepository breakdownRepository;
    private final PgBranchRepository branchRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<CompanyBranchPayrollHeading> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> headingMysqlIds = batch.stream()
                .filter(s -> s.getCompanyPayrollHeading() != null)
                .map(s -> s.getCompanyPayrollHeading().getId())
                .collect(Collectors.toSet());
        Map<Long, BranchSalaryBreakdownEntity> breakdowns =
                breakdownRepository.findByMysqlIdIn(headingMysqlIds).stream()
                        .collect(Collectors.toMap(
                                BranchSalaryBreakdownEntity::getMysqlId, Function.identity(), (a, b) -> a));

        Set<Long> branchMysqlIds = batch.stream()
                .filter(s -> s.getBranchDepartment() != null)
                .map(s -> s.getBranchDepartment().getId())
                .collect(Collectors.toSet());
        Map<Long, BranchEntity> branches = branchRepository.findByMysqlIdIn(branchMysqlIds).stream()
                .collect(Collectors.toMap(BranchEntity::getMysqlId, Function.identity(), (a, b) -> a));

        Set<BranchSalaryBreakdownEntity> toSave = new HashSet<>();
        int updated = 0;

        for (CompanyBranchPayrollHeading source : batch) {
            if (!Boolean.TRUE.equals(source.getStatus())) {
                continue;
            }
            if (source.getCompanyPayrollHeading() == null || source.getBranchDepartment() == null) {
                log.warn("Skipping companyBranchPayrollHeading id={}, missing heading/branch", source.getId());
                continue;
            }
            BranchSalaryBreakdownEntity breakdown = breakdowns.get(source.getCompanyPayrollHeading().getId());
            if (breakdown == null) {
                log.warn(
                        "Skipping companyBranchPayrollHeading id={}, breakdown mysqlId={} not found",
                        source.getId(),
                        source.getCompanyPayrollHeading().getId());
                continue;
            }
            if (breakdown.getBranchId() != null) {
                continue;
            }
            BranchEntity branch = branches.get(source.getBranchDepartment().getId());
            if (branch == null) {
                log.warn(
                        "Skipping companyBranchPayrollHeading id={}, branch mysqlId={} not migrated",
                        source.getId(),
                        source.getBranchDepartment().getId());
                continue;
            }
            breakdown.setBranchId(branch.getId());
            toSave.add(breakdown);
            updated++;
        }

        if (!toSave.isEmpty()) {
            breakdownRepository.saveAll(new ArrayList<>(toSave));
        }
        exchange.setProperty("batchImported", updated);
    }
}
