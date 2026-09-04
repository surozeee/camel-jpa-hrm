package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.JobCategory;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.SkillCategoryEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgSkillCategoryRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Step 22zh: jobCategory → hrm_skill_category (fan-out per migrated company).
 */
@Component
@RequiredArgsConstructor
public class JobCategoryProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(JobCategoryProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgSkillCategoryRepository targetRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<JobCategory> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        List<CompanyEntity> companies = companyRepository.findAll().stream()
                .filter(c -> c.getMysqlId() != null)
                .collect(Collectors.toList());
        if (companies.isEmpty()) {
            log.warn("Skipping jobCategory batch: no migrated companies");
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream()
                .flatMap(cat -> companies.stream()
                        .map(c -> PimsLeftoversMigrationMapper.jobCategoryMysqlId(cat.getId(), c.getMysqlId())))
                .collect(Collectors.toSet());
        Set<Long> existingIds = targetRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        List<SkillCategoryEntity> toSave = new ArrayList<>();
        for (JobCategory source : batch) {
            if (OrgMigrationMapper.trimToNull(source.getCategoryName()) == null) {
                log.warn("Skipping jobCategory id={}, blank categoryName", source.getId());
                continue;
            }
            for (CompanyEntity company : companies) {
                long mysqlId = PimsLeftoversMigrationMapper.jobCategoryMysqlId(source.getId(), company.getMysqlId());
                if (existingIds.contains(mysqlId)) {
                    continue;
                }
                SkillCategoryEntity mapped =
                        PimsLeftoversMigrationMapper.fromJobCategory(source, company.getId(), company.getMysqlId());
                if (mapped == null) {
                    continue;
                }
                toSave.add(mapped);
                existingIds.add(mysqlId);
            }
        }

        if (!toSave.isEmpty()) {
            targetRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
