package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.JobPosition;
import com.jojolaptech.camel.model.postgres.company.EmployeeContractEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeContractRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import java.time.LocalDate;
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
 * Step 22zc: jobPosition → employee.hire_date (if null) + hrm_employee_contract.
 */
@Component
@RequiredArgsConstructor
public class JobPositionProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(JobPositionProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgEmployeeContractRepository contractRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<JobPosition> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> contractMysqlIds = batch.stream()
                .map(row -> OrgStructureLeftoversMigrationMapper.JOB_POSITION_MYSQL_ID_OFFSET + row.getId())
                .collect(Collectors.toSet());
        Set<Long> existingContractIds = contractRepository.findMysqlIdsByMysqlIdIn(contractMysqlIds);

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Map<UUID, EmployeeEntity> employeesToSave = new HashMap<>();
        List<EmployeeContractEntity> contractsToSave = new ArrayList<>();
        int imported = 0;

        for (JobPosition source : batch) {
            if (source.getEmployee() == null) {
                log.warn("Skipping jobPosition id={}, missing employee", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping jobPosition id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }

            boolean changed = false;
            LocalDate appointmentHireDate =
                    OrgStructureLeftoversMigrationMapper.toLocalDate(source.getAppointmentHireDate());
            if (appointmentHireDate != null && employee.getHireDate() == null) {
                employee.setHireDate(appointmentHireDate);
                employeesToSave.put(employee.getId(), employee);
                changed = true;
            }

            long contractMysqlId =
                    OrgStructureLeftoversMigrationMapper.JOB_POSITION_MYSQL_ID_OFFSET + source.getId();
            if (OrgStructureLeftoversMigrationMapper.shouldCreateJobPositionContract(source)
                    && !existingContractIds.contains(contractMysqlId)) {
                EmployeeContractEntity contract =
                        OrgStructureLeftoversMigrationMapper.fromJobPosition(source, employee.getId());
                if (contract == null) {
                    log.warn("Skipping jobPosition id={} contract, missing start date", source.getId());
                } else {
                    contractsToSave.add(contract);
                    existingContractIds.add(contractMysqlId);
                    changed = true;
                }
            }

            if (changed) {
                imported++;
            }
        }

        if (!employeesToSave.isEmpty()) {
            employeeRepository.saveAll(new ArrayList<>(employeesToSave.values()));
        }
        if (!contractsToSave.isEmpty()) {
            contractRepository.saveAll(contractsToSave);
        }
        exchange.setProperty("batchImported", imported);
    }
}
