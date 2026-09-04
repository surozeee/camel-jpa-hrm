package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.BranchDepartmentHead;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
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
 * Step 22z: active branchDepartmentHead → employee.is_department_head=true.
 */
@Component
@RequiredArgsConstructor
public class BranchDepartmentHeadProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(BranchDepartmentHeadProcessor.class);

    private final PgEmployeeRepository employeeRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<BranchDepartmentHead> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Map<UUID, EmployeeEntity> toSaveById = new HashMap<>();
        int updated = 0;
        for (BranchDepartmentHead source : batch) {
            if (source.getEmployee() == null) {
                log.warn("Skipping branchDepartmentHead id={}, missing employee", source.getId());
                continue;
            }
            if (source.getEndDate() != null) {
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping branchDepartmentHead id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }
            if (Boolean.TRUE.equals(employee.getIsDepartmentHead())) {
                continue;
            }
            employee.setIsDepartmentHead(true);
            toSaveById.put(employee.getId(), employee);
            updated++;
        }

        if (!toSaveById.isEmpty()) {
            employeeRepository.saveAll(new ArrayList<>(toSaveById.values()));
        }
        exchange.setProperty("batchImported", updated);
    }
}
