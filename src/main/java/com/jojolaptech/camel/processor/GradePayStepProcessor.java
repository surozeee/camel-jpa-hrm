package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.JobLevelGradeValue;
import com.jojolaptech.camel.model.postgres.company.GradeEntity;
import com.jojolaptech.camel.model.postgres.company.GradePayStepEntity;
import com.jojolaptech.camel.repository.postgres.company.PgGradePayStepRepository;
import com.jojolaptech.camel.repository.postgres.company.PgGradeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

/** Migrates jobLevelGradeValue → hrm_grade_pay_step. */
@Component
@RequiredArgsConstructor
public class GradePayStepProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(GradePayStepProcessor.class);

    private final PgGradeRepository gradeRepository;
    private final PgGradePayStepRepository gradePayStepRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<JobLevelGradeValue> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> existingIds = gradePayStepRepository.findMysqlIdsByMysqlIdIn(
                batch.stream().map(JobLevelGradeValue::getId).collect(Collectors.toSet()));

        Set<Long> gradeMysqlIds = batch.stream()
                .filter(row -> row.getJobLevel() != null)
                .map(row -> row.getJobLevel().getId())
                .collect(Collectors.toSet());
        Map<Long, GradeEntity> gradeByMysqlId = gradeRepository.findByMysqlIdIn(gradeMysqlIds).stream()
                .collect(Collectors.toMap(GradeEntity::getMysqlId, g -> g, (a, b) -> a));

        List<GradePayStepEntity> toSave = new ArrayList<>();
        for (JobLevelGradeValue source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getJobLevel() == null) {
                log.warn("Skipping jobLevelGradeValue id={}, missing jobLevel", source.getId());
                continue;
            }
            GradeEntity grade = gradeByMysqlId.get(source.getJobLevel().getId());
            if (grade == null) {
                log.warn(
                        "Skipping jobLevelGradeValue id={}, grade mysqlId={} not migrated",
                        source.getId(),
                        source.getJobLevel().getId());
                continue;
            }

            LocalDate effectiveFrom = PayrollHeadingMigrationMapper.toLocalDate(source.getStartDate());
            if (effectiveFrom == null) {
                effectiveFrom = LocalDate.of(2000, 1, 1);
            }
            BigDecimal amount = source.getGradeAmount() != null ? source.getGradeAmount() : BigDecimal.ZERO;

            toSave.add(GradePayStepEntity.builder()
                    .mysqlId(source.getId())
                    .companyId(grade.getCompanyId())
                    .gradeId(grade.getId())
                    .stepNumber(source.getGradeNumber() != null ? source.getGradeNumber() : 1)
                    .amount(amount)
                    .effectiveFrom(effectiveFrom)
                    .effectiveTo(PayrollHeadingMigrationMapper.toLocalDate(source.getEndDate()))
                    .remarks("migrated from jobLevelGradeValue#" + source.getId())
                    .build());
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            gradePayStepRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
