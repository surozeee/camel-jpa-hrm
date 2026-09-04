package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EmployeeSeminar;
import com.jojolaptech.camel.model.postgres.company.EmployeeSeminarEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeSeminarRepository;
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
public class EmployeeSeminarProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeSeminarProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgEmployeeSeminarRepository targetRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeeSeminar> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(EmployeeSeminar::getId).collect(Collectors.toSet());
        Set<Long> existingIds = targetRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, row -> row, (left, right) -> left));

        List<EmployeeSeminarEntity> toSave = new ArrayList<>();
        for (EmployeeSeminar source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployee() == null) {
                log.warn("Skipping employeeSeminar id={}, missing employee", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn("Skipping employeeSeminar id={}, employee not migrated", source.getId());
                continue;
            }

            EmployeeSeminarEntity mapped =
                    EmployeeProfileMigrationMapper.fromEmployeeSeminar(source, employee.getId());
            if (mapped == null) {
                log.warn("Skipping employeeSeminar id={}, missing seminar name", source.getId());
                continue;
            }
            toSave.add(mapped);
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            targetRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
