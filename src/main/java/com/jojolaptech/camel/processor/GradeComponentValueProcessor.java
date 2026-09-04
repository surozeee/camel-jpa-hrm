package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.JobLevelPayroll;
import com.jojolaptech.camel.model.postgres.company.BranchSalaryBreakdownEntity;
import com.jojolaptech.camel.model.postgres.company.GradeComponentValueEntity;
import com.jojolaptech.camel.model.postgres.company.GradeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgBranchSalaryBreakdownRepository;
import com.jojolaptech.camel.repository.postgres.company.PgGradeComponentValueRepository;
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

/** Migrates jobLevelPayroll → hrm_grade_component_value. */
@Component
@RequiredArgsConstructor
public class GradeComponentValueProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(GradeComponentValueProcessor.class);

    private final PgGradeRepository gradeRepository;
    private final PgBranchSalaryBreakdownRepository breakdownRepository;
    private final PgGradeComponentValueRepository gradeComponentValueRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<JobLevelPayroll> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> existingIds = gradeComponentValueRepository.findMysqlIdsByMysqlIdIn(
                batch.stream().map(JobLevelPayroll::getId).collect(Collectors.toSet()));

        Set<Long> gradeMysqlIds = batch.stream()
                .filter(row -> row.getJobLevel() != null)
                .map(row -> row.getJobLevel().getId())
                .collect(Collectors.toSet());
        Map<Long, GradeEntity> gradeByMysqlId = gradeRepository.findByMysqlIdIn(gradeMysqlIds).stream()
                .collect(Collectors.toMap(GradeEntity::getMysqlId, g -> g, (a, b) -> a));

        Set<Long> headingMysqlIds = batch.stream()
                .filter(row -> row.getPayrollHeading() != null)
                .map(row -> row.getPayrollHeading().getId())
                .collect(Collectors.toSet());
        Map<Long, BranchSalaryBreakdownEntity> breakdownByMysqlId =
                breakdownRepository.findByMysqlIdIn(headingMysqlIds).stream()
                        .collect(Collectors.toMap(
                                BranchSalaryBreakdownEntity::getMysqlId, b -> b, (a, b) -> a));

        List<GradeComponentValueEntity> toSave = new ArrayList<>();
        for (JobLevelPayroll source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getJobLevel() == null || source.getPayrollHeading() == null) {
                log.warn("Skipping jobLevelPayroll id={}, missing jobLevel or payrollHeading", source.getId());
                continue;
            }
            GradeEntity grade = gradeByMysqlId.get(source.getJobLevel().getId());
            if (grade == null) {
                log.warn(
                        "Skipping jobLevelPayroll id={}, grade mysqlId={} not migrated",
                        source.getId(),
                        source.getJobLevel().getId());
                continue;
            }
            BranchSalaryBreakdownEntity breakdown =
                    breakdownByMysqlId.get(source.getPayrollHeading().getId());
            if (breakdown == null) {
                log.warn(
                        "Skipping jobLevelPayroll id={}, breakdown mysqlId={} not migrated",
                        source.getId(),
                        source.getPayrollHeading().getId());
                continue;
            }

            LocalDate effectiveFrom = PayrollHeadingMigrationMapper.toLocalDate(source.getStartDate());
            if (effectiveFrom == null) {
                effectiveFrom = LocalDate.of(2000, 1, 1);
            }

            toSave.add(GradeComponentValueEntity.builder()
                    .mysqlId(source.getId())
                    .companyId(grade.getCompanyId())
                    .gradeId(grade.getId())
                    .branchSalaryBreakdownId(breakdown.getId())
                    .amount(source.getPayrollAmount() != null ? source.getPayrollAmount() : BigDecimal.ZERO)
                    .effectiveFrom(effectiveFrom)
                    .effectiveTo(PayrollHeadingMigrationMapper.toLocalDate(source.getEndDate()))
                    .remarks("migrated from jobLevelPayroll#" + source.getId())
                    .build());
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            gradeComponentValueRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
