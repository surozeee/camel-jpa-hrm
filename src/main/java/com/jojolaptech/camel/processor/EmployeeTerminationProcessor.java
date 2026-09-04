package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EmployeeTermination;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
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
 * Step 22y: employeeTermination → employee.termination_date when currently null.
 */
@Component
@RequiredArgsConstructor
public class EmployeeTerminationProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeTerminationProcessor.class);

    private final PgEmployeeRepository employeeRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeeTermination> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getCompanyEmployee() != null && row.getCompanyEmployee().getEmployee() != null)
                .map(row -> row.getCompanyEmployee().getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Map<UUID, EmployeeEntity> toSaveById = new HashMap<>();
        int updated = 0;
        for (EmployeeTermination source : batch) {
            if (source.getCompanyEmployee() == null || source.getCompanyEmployee().getEmployee() == null) {
                log.warn("Skipping employeeTermination id={}, missing companyEmployee.employee", source.getId());
                continue;
            }
            LocalDate terminationDate = toLocalDate(source.getEndDate());
            if (terminationDate == null) {
                log.warn("Skipping employeeTermination id={}, missing endDate", source.getId());
                continue;
            }
            EmployeeEntity employee =
                    employeeByMysqlId.get(source.getCompanyEmployee().getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping employeeTermination id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getCompanyEmployee().getEmployee().getId());
                continue;
            }
            if (employee.getTerminationDate() != null) {
                continue;
            }
            employee.setTerminationDate(terminationDate);
            toSaveById.put(employee.getId(), employee);
            updated++;
        }

        if (!toSaveById.isEmpty()) {
            employeeRepository.saveAll(new ArrayList<>(toSaveById.values()));
        }
        exchange.setProperty("batchImported", updated);
    }

    private static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
