package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EmployeeProject;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.ExperienceEntity;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgExperienceRepository;
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
 * Step 22zg: employeeProject → hrm_experience (tagged as projects).
 */
@Component
@RequiredArgsConstructor
public class EmployeeProjectProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeProjectProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgExperienceRepository targetRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeeProject> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream()
                .map(row -> PimsLeftoversMigrationMapper.PROJECT_EXPERIENCE_OFFSET + row.getId())
                .collect(Collectors.toSet());
        Set<Long> existingIds = targetRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        List<ExperienceEntity> toSave = new ArrayList<>();
        for (EmployeeProject source : batch) {
            long mysqlId = PimsLeftoversMigrationMapper.PROJECT_EXPERIENCE_OFFSET + source.getId();
            if (existingIds.contains(mysqlId)) {
                continue;
            }
            if (OrgMigrationMapper.trimToNull(source.getName()) == null) {
                log.warn("Skipping employeeProject id={}, blank name", source.getId());
                continue;
            }
            if (source.getEmployee() == null) {
                log.warn("Skipping employeeProject id={}, missing employee", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping employeeProject id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }
            ExperienceEntity mapped =
                    PimsLeftoversMigrationMapper.fromEmployeeProject(source, employee.getId(), employee.getHireDate());
            if (mapped == null) {
                log.warn("Skipping employeeProject id={}, mapping failed", source.getId());
                continue;
            }
            toSave.add(mapped);
            existingIds.add(mysqlId);
        }

        if (!toSave.isEmpty()) {
            targetRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
