package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EmployeeGrade;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.GradeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgGradeRepository;
import java.util.ArrayList;
import java.util.HashMap;
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

/**
 * Links open employeeGrade rows onto employee.gradeId via JobLevel → GradeEntity.mysqlId.
 */
@Component
@RequiredArgsConstructor
public class EmployeeGradeLinkProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeGradeLinkProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgGradeRepository gradeRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeeGrade> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<Long> gradeMysqlIds = batch.stream()
                .filter(row -> row.getJobLevelGrade() != null && row.getJobLevelGrade().getJobLevel() != null)
                .map(row -> row.getJobLevelGrade().getJobLevel().getId())
                .collect(Collectors.toSet());
        Map<Long, UUID> gradeIdByMysqlId = gradeRepository.findByMysqlIdIn(gradeMysqlIds).stream()
                .collect(Collectors.toMap(GradeEntity::getMysqlId, GradeEntity::getId, (a, b) -> a));

        Map<UUID, EmployeeEntity> toSaveById = new HashMap<>();
        int linked = 0;
        for (EmployeeGrade source : batch) {
            if (source.getEmployee() == null
                    || source.getJobLevelGrade() == null
                    || source.getJobLevelGrade().getJobLevel() == null) {
                log.warn("Skipping employeeGrade id={}, missing employee/jobLevel", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping employeeGrade id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }
            UUID gradeId = gradeIdByMysqlId.get(source.getJobLevelGrade().getJobLevel().getId());
            if (gradeId == null) {
                log.warn(
                        "Skipping employeeGrade id={}, grade mysqlId={} not migrated",
                        source.getId(),
                        source.getJobLevelGrade().getJobLevel().getId());
                continue;
            }
            if (gradeId.equals(employee.getGradeId())) {
                continue;
            }
            employee.setGradeId(gradeId);
            toSaveById.put(employee.getId(), employee);
            linked++;
        }

        if (!toSaveById.isEmpty()) {
            employeeRepository.saveAll(new ArrayList<>(toSaveById.values()));
        }
        exchange.setProperty("batchImported", linked);
    }
}
