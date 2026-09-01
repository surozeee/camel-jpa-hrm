package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Employee;
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
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeMasterAddressProcessor implements Processor {

    private final PgEmployeeRepository employeeRepository;
    private final PgEmployeeAddressRepository employeeAddressRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Employee> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream()
                .flatMap(employee -> List.of(
                                EmployeeProfileMigrationMapper.masterAddressMysqlId(employee.getId(), 1L),
                                EmployeeProfileMigrationMapper.masterAddressMysqlId(employee.getId(), 2L))
                        .stream())
                .collect(Collectors.toSet());
        Set<Long> existingIds = employeeAddressRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> employeeMysqlIds =
                batch.stream().map(Employee::getId).collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, row -> row, (left, right) -> left));

        List<EmployeeAddressEntity> toSave = new ArrayList<>();
        for (Employee source : batch) {
            EmployeeEntity employee = employeeByMysqlId.get(source.getId());
            if (employee == null) {
                continue;
            }
            for (EmployeeAddressEntity address :
                    EmployeeProfileMigrationMapper.fromEmployeeMaster(source, employee.getId())) {
                if (existingIds.contains(address.getMysqlId())) {
                    continue;
                }
                toSave.add(address);
                existingIds.add(address.getMysqlId());
            }
        }

        if (!toSave.isEmpty()) {
            employeeAddressRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
