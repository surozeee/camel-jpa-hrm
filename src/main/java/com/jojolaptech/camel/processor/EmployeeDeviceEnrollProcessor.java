package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Employee;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.DeviceMacEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeDeviceEnrollEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgDeviceMacRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeDeviceEnrollRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeDeviceEnrollProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeDeviceEnrollProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgBranchRepository branchRepository;
    private final PgDeviceMacRepository deviceMacRepository;
    private final PgEmployeeDeviceEnrollRepository employeeDeviceEnrollRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Employee> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> employeeMysqlIds = batch.stream().map(Employee::getId).collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<Long> enrollMysqlIds = employeeByMysqlId.values().stream()
                .map(EmployeeEntity::getMysqlId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> existingEnrollIds = enrollMysqlIds.isEmpty()
                ? Set.of()
                : new java.util.HashSet<>(employeeDeviceEnrollRepository.findMysqlIdsByMysqlIdIn(enrollMysqlIds));

        Set<UUID> branchIds = employeeByMysqlId.values().stream()
                .map(EmployeeEntity::getBranchId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, BranchEntity> branchById = branchIds.isEmpty()
                ? Map.of()
                : branchRepository.findByIdInWithCompany(branchIds).stream()
                        .collect(Collectors.toMap(BranchEntity::getId, b -> b, (a, b) -> a));

        Set<UUID> companyIds = branchById.values().stream()
                .filter(b -> b.getCompany() != null)
                .map(b -> b.getCompany().getId())
                .collect(Collectors.toSet());
        Map<UUID, DeviceMacEntity> firstDeviceByCompany = new HashMap<>();
        if (!companyIds.isEmpty()) {
            for (DeviceMacEntity device : deviceMacRepository.findByCompanyIdIn(companyIds)) {
                firstDeviceByCompany.putIfAbsent(device.getCompanyId(), device);
            }
        }

        List<EmployeeDeviceEnrollEntity> toSave = new ArrayList<>();
        for (Employee source : batch) {
            EmployeeEntity employee = employeeByMysqlId.get(source.getId());
            if (employee == null || employee.getMysqlId() == null) {
                continue;
            }
            if (existingEnrollIds.contains(employee.getMysqlId())) {
                continue;
            }
            String enrollId = employee.getEnrollId();
            if (enrollId == null || enrollId.isBlank()) {
                continue;
            }
            if (employee.getBranchId() == null) {
                log.warn(
                        "Skipping device enroll for employee mysqlId={}, no branch",
                        employee.getMysqlId());
                continue;
            }
            BranchEntity branch = branchById.get(employee.getBranchId());
            if (branch == null || branch.getCompany() == null) {
                log.warn(
                        "Skipping device enroll for employee mysqlId={}, branch/company missing",
                        employee.getMysqlId());
                continue;
            }
            DeviceMacEntity device = firstDeviceByCompany.get(branch.getCompany().getId());
            if (device == null) {
                log.warn(
                        "Skipping device enroll for employee mysqlId={}, no device mac for company",
                        employee.getMysqlId());
                continue;
            }

            toSave.add(EmployeeDeviceEnrollEntity.builder()
                    .mysqlId(employee.getMysqlId())
                    .enrollId(enrollId.trim())
                    .employeeId(employee.getId())
                    .deviceMacId(device.getId())
                    .build());
            existingEnrollIds.add(employee.getMysqlId());
        }

        if (!toSave.isEmpty()) {
            employeeDeviceEnrollRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
