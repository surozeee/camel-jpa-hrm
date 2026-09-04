package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EmployeeInsurance;
import com.jojolaptech.camel.model.postgres.company.EmployeeInsuranceEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeInsuranceRepository;
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
public class EmployeeInsuranceProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeInsuranceProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgEmployeeInsuranceRepository targetRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeeInsurance> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(EmployeeInsurance::getId).collect(Collectors.toSet());
        Set<Long> existingIds = targetRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, row -> row, (left, right) -> left));

        List<EmployeeInsuranceEntity> toSave = new ArrayList<>();
        for (EmployeeInsurance source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployee() == null) {
                log.warn("Skipping employeeInsurance id={}, missing employee", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn("Skipping employeeInsurance id={}, employee not migrated", source.getId());
                continue;
            }

            EmployeeInsuranceEntity mapped =
                    EmployeeProfileMigrationMapper.fromEmployeeInsurance(source, employee.getId());
            if (mapped == null) {
                log.warn("Skipping employeeInsurance id={}, mapping failed", source.getId());
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
