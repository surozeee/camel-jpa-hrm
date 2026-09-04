package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EmployeeContact;
import com.jojolaptech.camel.model.postgres.company.EmployeeDetailEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeDetailRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
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
 * Step 22x: emergency employeeContact → hrm_employee_detail emergency fields (upsert).
 */
@Component
@RequiredArgsConstructor
public class EmployeeEmergencyContactProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeEmergencyContactProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgEmployeeDetailRepository employeeDetailRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeeContact> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        List<EmployeeContact> emergencyRows = batch.stream()
                .filter(EmployeeProfileMigrationMapper::isEmergencyContact)
                .toList();
        if (emergencyRows.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> employeeMysqlIds = emergencyRows.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<UUID> employeeIds = employeeByMysqlId.values().stream()
                .map(EmployeeEntity::getId)
                .collect(Collectors.toSet());
        Map<UUID, EmployeeDetailEntity> detailByEmployeeId = employeeDetailRepository
                .findByEmployeeIdIn(employeeIds)
                .stream()
                .collect(Collectors.toMap(EmployeeDetailEntity::getEmployeeId, d -> d, (a, b) -> a));

        Map<UUID, EmployeeDetailEntity> toSaveByEmployeeId = new HashMap<>();
        int imported = 0;
        for (EmployeeContact source : emergencyRows) {
            if (source.getEmployee() == null) {
                log.warn("Skipping employeeContact id={}, missing employee", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn("Skipping employeeContact id={}, employee not migrated", source.getId());
                continue;
            }
            EmployeeDetailEntity detail = toSaveByEmployeeId.get(employee.getId());
            if (detail == null) {
                detail = detailByEmployeeId.get(employee.getId());
            }
            if (detail == null) {
                detail = EmployeeProfileMigrationMapper.newEmployeeDetail(source, employee.getId());
                if (detail == null) {
                    log.warn("Skipping employeeContact id={}, no emergency fields", source.getId());
                    continue;
                }
                toSaveByEmployeeId.put(employee.getId(), detail);
                imported++;
            } else {
                boolean beforeBlank = isEmergencyBlank(detail);
                EmployeeProfileMigrationMapper.applyEmergencyContact(detail, source);
                toSaveByEmployeeId.put(employee.getId(), detail);
                if (beforeBlank && !isEmergencyBlank(detail)) {
                    imported++;
                }
            }
        }

        if (!toSaveByEmployeeId.isEmpty()) {
            employeeDetailRepository.saveAll(new ArrayList<>(toSaveByEmployeeId.values()));
        }
        exchange.setProperty("batchImported", imported);
    }

    private static boolean isEmergencyBlank(EmployeeDetailEntity detail) {
        return (detail.getEmergencyContactName() == null || detail.getEmergencyContactName().isBlank())
                && (detail.getEmergencyContactPhone() == null || detail.getEmergencyContactPhone().isBlank());
    }
}
