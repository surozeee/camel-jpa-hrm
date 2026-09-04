package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.JobCategories;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.SkillCategoryEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgSkillCategoryRepository;
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

/**
 * Step 22zi: jobCategories → hrm_skill_category (company-scoped).
 */
@Component
@RequiredArgsConstructor
public class JobCategoriesProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(JobCategoriesProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgSkillCategoryRepository targetRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<JobCategories> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream()
                .map(row -> PimsLeftoversMigrationMapper.JOB_CATEGORIES_OFFSET + row.getId())
                .collect(Collectors.toSet());
        Set<Long> existingIds = targetRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> companyMysqlIds = batch.stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companyByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, c -> c, (a, b) -> a));

        List<SkillCategoryEntity> toSave = new ArrayList<>();
        for (JobCategories source : batch) {
            long mysqlId = PimsLeftoversMigrationMapper.JOB_CATEGORIES_OFFSET + source.getId();
            if (existingIds.contains(mysqlId)) {
                continue;
            }
            if (OrgMigrationMapper.trimToNull(source.getJobName()) == null) {
                log.warn("Skipping jobCategories id={}, blank jobName", source.getId());
                continue;
            }
            if (source.getCompany() == null) {
                log.warn("Skipping jobCategories id={}, missing company", source.getId());
                continue;
            }
            CompanyEntity company = companyByMysqlId.get(source.getCompany().getId());
            if (company == null) {
                log.warn(
                        "Skipping jobCategories id={}, company mysqlId={} not migrated",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }
            SkillCategoryEntity mapped = PimsLeftoversMigrationMapper.fromJobCategories(source, company.getId());
            if (mapped == null) {
                log.warn("Skipping jobCategories id={}, mapping failed", source.getId());
                continue;
            }
            toSave.add(mapped);
            existingIds.add(mysqlId);
        }

        if (!toSave.isEmpty()) {
            targetRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
