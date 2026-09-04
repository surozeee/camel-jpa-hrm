package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EmployeeJobLevel;
import com.jojolaptech.camel.model.postgres.company.EmployeeEmploymentHistoryEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.GradeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeEmploymentHistoryRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgGradeRepository;
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
 * Step 22za: employeeJobLevel → hrm_employee_employment_history (GRADE_CHANGE).
 */
@Component
@RequiredArgsConstructor
public class EmployeeJobLevelHistoryProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeJobLevelHistoryProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgGradeRepository gradeRepository;
    private final PgEmployeeEmploymentHistoryRepository historyRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeeJobLevel> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(EmployeeJobLevel::getId).collect(Collectors.toSet());
        Set<Long> existingIds = historyRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<Long> gradeMysqlIds = batch.stream()
                .filter(row -> row.getJobLevel() != null)
                .map(row -> row.getJobLevel().getId())
                .collect(Collectors.toSet());
        Map<Long, GradeEntity> gradeByMysqlId = gradeRepository.findByMysqlIdIn(gradeMysqlIds).stream()
                .collect(Collectors.toMap(GradeEntity::getMysqlId, g -> g, (a, b) -> a));

        List<EmployeeEmploymentHistoryEntity> toSave = new ArrayList<>();
        for (EmployeeJobLevel source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployee() == null) {
                log.warn("Skipping employeeJobLevel id={}, missing employee", source.getId());
                continue;
            }
            if (source.getStartDate() == null) {
                log.warn("Skipping employeeJobLevel id={}, missing startDate", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping employeeJobLevel id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }
            GradeEntity grade = source.getJobLevel() != null
                    ? gradeByMysqlId.get(source.getJobLevel().getId())
                    : null;
            if (grade == null && source.getJobLevel() != null) {
                log.warn(
                        "employeeJobLevel id={}: grade for jobLevel mysqlId={} not migrated; writing name only",
                        source.getId(),
                        source.getJobLevel().getId());
            }

            EmployeeEmploymentHistoryEntity mapped =
                    OrgStructureLeftoversMigrationMapper.fromEmployeeJobLevel(source, employee, grade);
            if (mapped == null) {
                log.warn("Skipping employeeJobLevel id={}, missing effectiveFrom", source.getId());
                continue;
            }
            toSave.add(mapped);
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            historyRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
