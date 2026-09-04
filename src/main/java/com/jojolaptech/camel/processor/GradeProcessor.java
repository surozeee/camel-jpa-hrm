package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.JobLevel;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.GradeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgGradeRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Migrates jobLevel → hrm_grade. */
@Component
@RequiredArgsConstructor
public class GradeProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(GradeProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgGradeRepository gradeRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<JobLevel> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> existingIds = gradeRepository.findMysqlIdsByMysqlIdIn(
                batch.stream().map(JobLevel::getId).collect(Collectors.toSet()));

        Set<Long> companyMysqlIds = batch.stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, UUID> companyIdByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, CompanyEntity::getId, (a, b) -> a));

        List<GradeEntity> toSave = new ArrayList<>();
        for (JobLevel source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getCompany() == null) {
                log.warn("Skipping jobLevel id={}, missing company", source.getId());
                continue;
            }
            UUID companyId = companyIdByMysqlId.get(source.getCompany().getId());
            if (companyId == null) {
                log.warn(
                        "Skipping jobLevel id={}, company mysqlId={} not migrated",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }

            String name = source.getJobLevelName() != null ? source.getJobLevelName().trim() : null;
            toSave.add(GradeEntity.builder()
                    .mysqlId(source.getId())
                    .code(slugCode(name, source.getId()))
                    .name(name != null && !name.isEmpty() ? name : "JobLevel-" + source.getId())
                    .companyId(companyId)
                    .build());
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            gradeRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }

    private static String slugCode(String name, Long id) {
        if (name == null || name.isBlank()) {
            return "JL-" + id;
        }
        String slug = name.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (slug.isEmpty()) {
            return "JL-" + id;
        }
        if (slug.length() > 64) {
            slug = slug.substring(0, 64);
        }
        return slug;
    }
}
