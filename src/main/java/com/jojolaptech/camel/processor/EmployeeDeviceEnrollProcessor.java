package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Employee;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
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

    /** Synthetic device rows for companies with no attDeviceMAC (avoids blocking enroll). */
    private static final long PLACEHOLDER_MYSQL_ID_BASE = 60_000_000_000_000L;

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
        Map<UUID, DeviceMacEntity> firstDeviceByBranch = new HashMap<>();
        if (!companyIds.isEmpty()) {
            for (DeviceMacEntity device : deviceMacRepository.findByCompanyIdIn(companyIds)) {
                firstDeviceByCompany.putIfAbsent(device.getCompanyId(), device);
                if (device.getBranchId() != null) {
                    firstDeviceByBranch.putIfAbsent(device.getBranchId(), device);
                }
            }
        }

        List<EmployeeDeviceEnrollEntity> toSave = new ArrayList<>();
        int skippedNoBranch = 0;
        int skippedNoCompany = 0;
        int placeholdersCreated = 0;
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
                skippedNoBranch++;
                continue;
            }
            BranchEntity branch = branchById.get(employee.getBranchId());
            if (branch == null || branch.getCompany() == null) {
                skippedNoCompany++;
                continue;
            }
            CompanyEntity company = branch.getCompany();

            DeviceMacEntity device = firstDeviceByBranch.get(branch.getId());
            if (device == null) {
                device = firstDeviceByCompany.get(company.getId());
            }
            if (device == null) {
                device = ensurePlaceholderDevice(company, branch, firstDeviceByCompany, firstDeviceByBranch);
                if (device != null) {
                    placeholdersCreated++;
                }
            }
            if (device == null) {
                skippedNoCompany++;
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

        if (skippedNoBranch + skippedNoCompany + placeholdersCreated > 0) {
            log.warn(
                    "Device enroll batch: noBranch={}, unresolved={}, placeholdersCreated={}",
                    skippedNoBranch,
                    skippedNoCompany,
                    placeholdersCreated);
        }

        if (!toSave.isEmpty()) {
            employeeDeviceEnrollRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }

    private DeviceMacEntity ensurePlaceholderDevice(
            CompanyEntity company,
            BranchEntity branch,
            Map<UUID, DeviceMacEntity> firstDeviceByCompany,
            Map<UUID, DeviceMacEntity> firstDeviceByBranch) {
        Long companyMysqlId = company.getMysqlId();
        if (companyMysqlId == null) {
            return null;
        }
        String macAddress = "MIGRATE-NO-DEVICE-" + companyMysqlId;
        DeviceMacEntity existing = deviceMacRepository.findByMacAddress(macAddress).orElse(null);
        if (existing != null) {
            firstDeviceByCompany.put(company.getId(), existing);
            firstDeviceByBranch.putIfAbsent(branch.getId(), existing);
            return existing;
        }

        Long placeholderMysqlId = PLACEHOLDER_MYSQL_ID_BASE + companyMysqlId;
        if (deviceMacRepository.findByMysqlId(placeholderMysqlId).isPresent()) {
            DeviceMacEntity byMysql = deviceMacRepository.findByMysqlId(placeholderMysqlId).orElse(null);
            if (byMysql != null) {
                firstDeviceByCompany.put(company.getId(), byMysql);
                firstDeviceByBranch.putIfAbsent(branch.getId(), byMysql);
                return byMysql;
            }
        }

        DeviceMacEntity created = deviceMacRepository.save(DeviceMacEntity.builder()
                .mysqlId(placeholderMysqlId)
                .macAddress(macAddress)
                .deviceName("Migrated placeholder (no attDeviceMAC)")
                .deviceSerialNumber(macAddress)
                .description("Auto-created so employee enrollId can migrate without a legacy device")
                .companyId(company.getId())
                .branchId(branch.getId())
                .build());
        firstDeviceByCompany.put(company.getId(), created);
        firstDeviceByBranch.putIfAbsent(branch.getId(), created);
        log.info(
                "Created placeholder deviceMac for company mysqlId={} branch={} mac={}",
                companyMysqlId,
                branch.getId(),
                macAddress);
        return created;
    }
}
