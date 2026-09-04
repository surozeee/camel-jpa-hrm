package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EmployeeJob;
import com.jojolaptech.camel.model.postgres.company.EmployeeDesignationEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeDesignationRepository;
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

/**
 * Step 22w: active employeeJob → employee.designation_id via jobTitle mysql id.
 */
@Component
@RequiredArgsConstructor
public class EmployeeDesignationLinkProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeDesignationLinkProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgEmployeeDesignationRepository designationRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeeJob> batch = exchange.getMessage().getBody(List.class);
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

        Set<Long> jobTitleMysqlIds = batch.stream()
                .filter(row -> row.getJob() != null)
                .map(row -> row.getJob().getId())
                .collect(Collectors.toSet());
        Map<Long, UUID> designationIdByJobMysqlId = designationRepository.findByMysqlIdIn(jobTitleMysqlIds).stream()
                .collect(Collectors.toMap(
                        EmployeeDesignationEntity::getMysqlId, EmployeeDesignationEntity::getId, (a, b) -> a));

        Map<UUID, EmployeeEntity> toSaveById = new HashMap<>();
        int linked = 0;
        for (EmployeeJob source : batch) {
            if (source.getEmployee() == null || source.getJob() == null) {
                log.warn("Skipping employeeJob id={}, missing employee/job", source.getId());
                continue;
            }
            boolean active = Boolean.TRUE.equals(source.getIsactive()) || source.getEnddate() == null;
            if (!active) {
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping employeeJob id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }
            UUID designationId = designationIdByJobMysqlId.get(source.getJob().getId());
            if (designationId == null) {
                log.warn(
                        "Skipping employeeJob id={}, designation for jobTitle mysqlId={} not migrated",
                        source.getId(),
                        source.getJob().getId());
                continue;
            }
            if (Objects.equals(designationId, employee.getDesignationId())) {
                continue;
            }
            employee.setDesignationId(designationId);
            toSaveById.put(employee.getId(), employee);
            linked++;
        }

        if (!toSaveById.isEmpty()) {
            employeeRepository.saveAll(new ArrayList<>(toSaveById.values()));
        }
        exchange.setProperty("batchImported", linked);
    }
}
