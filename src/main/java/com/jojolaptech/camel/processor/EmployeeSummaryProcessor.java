package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EmployeeSummary;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Step 18e: employeeSummary → append tex to employee.notes with marker
 * {@code [migrated-summary:{id}]} for idempotency.
 */
@Component
@RequiredArgsConstructor
public class EmployeeSummaryProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeSummaryProcessor.class);

    private final PgEmployeeRepository employeeRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeeSummary> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(s -> s.getEmployee() != null)
                .map(s -> s.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employees = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, Function.identity(), (a, b) -> a));

        Set<EmployeeEntity> toSave = new HashSet<>();
        int imported = 0;

        for (EmployeeSummary source : batch) {
            if (source.getEmployee() == null) {
                log.warn("Skipping employeeSummary id={}, missing employee", source.getId());
                continue;
            }
            EmployeeEntity employee = employees.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping employeeSummary id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }
            String marker = CompanyParamsLeftoversMigrationMapper.employeeSummaryMarker(source.getId());
            String notes = employee.getNotes() != null ? employee.getNotes() : "";
            if (notes.contains(marker)) {
                continue;
            }
            String tex = source.getTex() != null ? source.getTex().trim() : "";
            if (tex.isEmpty()) {
                log.warn("Skipping employeeSummary id={}, empty tex", source.getId());
                continue;
            }
            StringBuilder updated = new StringBuilder(notes);
            if (!notes.isEmpty()) {
                updated.append("\n\n");
            }
            updated.append(marker).append('\n').append(tex);
            employee.setNotes(updated.toString());
            toSave.add(employee);
            imported++;
        }

        if (!toSave.isEmpty()) {
            employeeRepository.saveAll(new ArrayList<>(toSave));
        }
        exchange.setProperty("batchImported", imported);
    }
}
