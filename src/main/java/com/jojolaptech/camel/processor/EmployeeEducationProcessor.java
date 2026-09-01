package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EmployeeEducation;
import com.jojolaptech.camel.model.postgres.company.EmployeeEducationEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeEducationRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
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
public class EmployeeEducationProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeEducationProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgEmployeeEducationRepository employeeEducationRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeeEducation> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(EmployeeEducation::getId).collect(Collectors.toSet());
        Set<Long> existingIds = employeeEducationRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, row -> row, (left, right) -> left));

        List<EmployeeEducationEntity> toSave = new ArrayList<>();
        for (EmployeeEducation source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployee() == null) {
                log.warn("Skipping employeeEducation id={}, missing employee", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn("Skipping employeeEducation id={}, employee not migrated", source.getId());
                continue;
            }

            EmployeeEducationEntity education =
                    EmployeeProfileMigrationMapper.fromEmployeeEducation(source, employee.getId());
            if (education == null) {
                log.warn("Skipping employeeEducation id={}, missing institution/level", source.getId());
                continue;
            }
            toSave.add(education);
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            employeeEducationRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
