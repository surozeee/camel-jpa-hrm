package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Branch;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import java.util.ArrayList;
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

@Component
@RequiredArgsConstructor
public class BranchProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(BranchProcessor.class);

    private final PgBranchRepository branchRepository;
    private final PgCompanyRepository companyRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Branch> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> existingIds = branchRepository.findMysqlIdsByMysqlIdIn(
                batch.stream().map(Branch::getId).toList());
        Set<Long> companyIds = batch.stream()
                .map(branch -> branch.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companiesByMysqlId = companyRepository.findByMysqlIdIn(companyIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, company -> company));

        List<BranchEntity> toSave = new ArrayList<>();
        for (Branch source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            CompanyEntity company = companiesByMysqlId.get(source.getCompany().getId());
            if (company == null) {
                log.warn("Skipping branch id={}, company mysqlId={} not migrated yet",
                        source.getId(), source.getCompany().getId());
                continue;
            }
            String name = OrgMigrationMapper.trimToNull(source.getBranchName());
            if (name == null) {
                log.warn("Skipping branch id={}, branchName is blank", source.getId());
                continue;
            }

            BranchEntity branch = BranchEntity.builder()
                    .mysqlId(source.getId())
                    .name(name)
                    .code(OrgMigrationMapper.trimToNull(source.getCode()))
                    .contactNo(OrgMigrationMapper.trimToNull(source.getPhoneNo()))
                    .email(OrgMigrationMapper.trimToNull(source.getEmail()))
                    .description(OrgMigrationMapper.branchDescription(source))
                    .headoffice(OrgMigrationMapper.isHeadOffice(source))
                    .company(company)
                    .build();
            branch.setStatus(StatusEnum.ACTIVE);
            toSave.add(branch);
        }

        if (!toSave.isEmpty()) {
            branchRepository.saveAll(toSave);
            branchRepository.flush();
        }

        log.info("Branch batch imported {} of {} rows", toSave.size(), batch.size());
        exchange.setProperty("batchImported", toSave.size());
    }
}
