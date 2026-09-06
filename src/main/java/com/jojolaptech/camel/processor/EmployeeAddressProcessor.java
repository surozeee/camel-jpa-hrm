package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EmployeeAddress;
import com.jojolaptech.camel.model.postgres.company.AddressEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgAddressRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
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

@Component
@RequiredArgsConstructor
public class EmployeeAddressProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeAddressProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgAddressRepository addressRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeeAddress> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(EmployeeAddress::getId).collect(Collectors.toSet());
        Set<Long> existingIds = addressRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(a -> a.getEmployee() != null)
                .map(a -> a.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, UUID> employeeIdByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, EmployeeEntity::getId, (a, b) -> a));

        List<AddressEntity> toSave = new ArrayList<>();
        for (EmployeeAddress source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployee() == null) {
                log.warn("Skipping employeeAddress id={}, missing employee", source.getId());
                continue;
            }
            UUID employeeId = employeeIdByMysqlId.get(source.getEmployee().getId());
            if (employeeId == null) {
                log.warn(
                        "Skipping employeeAddress id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }
            AddressEntity address = EmployeeProfileMigrationMapper.fromEmployeeAddress(source, employeeId);
            if (address == null) {
                log.warn("Skipping employeeAddress id={}, mapping failed", source.getId());
                continue;
            }
            toSave.add(address);
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            addressRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
