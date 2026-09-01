package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EmployeeAddress;
import com.jojolaptech.camel.model.postgres.company.EmployeeAddressEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeAddressRepository;
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
public class EmployeeAddressProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeAddressProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgEmployeeAddressRepository employeeAddressRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeeAddress> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(EmployeeAddress::getId).collect(Collectors.toSet());
        Set<Long> existingIds = employeeAddressRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, row -> row, (left, right) -> left));

        List<EmployeeAddressEntity> toSave = new ArrayList<>();
        for (EmployeeAddress source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployee() == null) {
                log.warn("Skipping employeeAddress id={}, missing employee", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn("Skipping employeeAddress id={}, employee not migrated", source.getId());
                continue;
            }

            EmployeeAddressEntity address =
                    EmployeeProfileMigrationMapper.fromEmployeeAddress(source, employee.getId());
            if (address == null) {
                log.warn("Skipping employeeAddress id={}, no street/address text", source.getId());
                continue;
            }
            toSave.add(address);
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            employeeAddressRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
