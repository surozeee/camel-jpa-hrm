package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.JobStatus;
import com.jojolaptech.camel.model.postgres.company.EmployeeEmploymentHistoryEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeEmploymentHistoryRepository;
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

/**
 * Step 22zb: jobStatus → hrm_employee_employment_history (EMPLOYMENT_TYPE_CHANGE).
 */
@Component
@RequiredArgsConstructor
public class JobStatusHistoryProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(JobStatusHistoryProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgEmployeeEmploymentHistoryRepository historyRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<JobStatus> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream()
                .map(row -> OrgStructureLeftoversMigrationMapper.JOB_STATUS_MYSQL_ID_OFFSET + row.getId())
                .collect(Collectors.toSet());
        Set<Long> existingIds = historyRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        List<EmployeeEmploymentHistoryEntity> toSave = new ArrayList<>();
        for (JobStatus source : batch) {
            long mysqlId = OrgStructureLeftoversMigrationMapper.JOB_STATUS_MYSQL_ID_OFFSET + source.getId();
            if (existingIds.contains(mysqlId)) {
                continue;
            }
            if (source.getEmployee() == null) {
                log.warn("Skipping jobStatus id={}, missing employee", source.getId());
                continue;
            }
            if (source.getFromDate() == null) {
                log.warn("Skipping jobStatus id={}, missing fromDate", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping jobStatus id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }

            EmployeeEmploymentHistoryEntity mapped =
                    OrgStructureLeftoversMigrationMapper.fromJobStatus(source, employee);
            if (mapped == null) {
                log.warn("Skipping jobStatus id={}, missing effectiveFrom", source.getId());
                continue;
            }
            toSave.add(mapped);
            existingIds.add(mysqlId);
        }

        if (!toSave.isEmpty()) {
            historyRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
