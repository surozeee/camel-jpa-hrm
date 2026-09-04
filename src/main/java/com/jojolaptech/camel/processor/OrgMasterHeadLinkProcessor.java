package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.BranchDepartmentHead;
import com.jojolaptech.camel.model.postgres.company.DepartmentEntity;
import com.jojolaptech.camel.model.postgres.company.DivisionEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.TeamEntity;
import com.jojolaptech.camel.repository.postgres.company.PgDepartmentRepository;
import com.jojolaptech.camel.repository.postgres.company.PgDivisionRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgTeamRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
 * Step 22zj: patch division.headEmployeeId and team.leaderEmployeeId from active heads.
 */
@Component
@RequiredArgsConstructor
public class OrgMasterHeadLinkProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(OrgMasterHeadLinkProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgDepartmentRepository departmentRepository;
    private final PgDivisionRepository divisionRepository;
    private final PgTeamRepository teamRepository;

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

        Set<Long> deptMysqlIds = new HashSet<>();
        Set<Long> branchMysqlIds = new HashSet<>();
        for (BranchDepartmentHead row : batch) {
            if (row.getDepartment() != null) {
                deptMysqlIds.add(row.getDepartment().getId());
            }
            if (row.getBranch() != null) {
                branchMysqlIds.add(row.getBranch().getId());
            }
        }

        Map<String, DepartmentEntity> deptByKey = new HashMap<>();
        if (!deptMysqlIds.isEmpty() && !branchMysqlIds.isEmpty()) {
            for (DepartmentEntity dept :
                    departmentRepository.findByMysqlIdInAndMysqlBranchIdIn(deptMysqlIds, branchMysqlIds)) {
                deptByKey.put(
                        OrgMigrationMapper.departmentKey(dept.getMysqlId(), dept.getMysqlBranchId()), dept);
            }
        }

        Set<UUID> divisionIds = deptByKey.values().stream()
                .map(DepartmentEntity::getDivisionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, DivisionEntity> divisionById = divisionIds.isEmpty()
                ? Map.of()
                : divisionRepository.findAllById(divisionIds).stream()
                        .collect(Collectors.toMap(DivisionEntity::getId, d -> d, (a, b) -> a));

        Set<UUID> departmentIds = deptByKey.values().stream()
                .map(DepartmentEntity::getId)
                .collect(Collectors.toSet());
        Map<UUID, TeamEntity> teamByDepartmentId = new HashMap<>();
        for (UUID departmentId : departmentIds) {
            teamRepository.findFirstByDepartmentId(departmentId).ifPresent(team ->
                    teamByDepartmentId.put(departmentId, team));
        }

        Map<UUID, DivisionEntity> divisionsToSave = new HashMap<>();
        Map<UUID, TeamEntity> teamsToSave = new HashMap<>();
        int applied = 0;

        for (BranchDepartmentHead source : batch) {
            if (source.getEmployee() == null || source.getEndDate() != null) {
                continue;
            }
            if (source.getDepartment() == null || source.getBranch() == null) {
                log.warn("Skipping branchDepartmentHead id={}, missing department/branch", source.getId());
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

            String key = OrgMigrationMapper.departmentKey(
                    source.getDepartment().getId(), source.getBranch().getId());
            DepartmentEntity department = deptByKey.get(key);
            if (department == null) {
                log.warn(
                        "Skipping branchDepartmentHead id={}, department mysqlId={} branchMysqlId={} not migrated",
                        source.getId(),
                        source.getDepartment().getId(),
                        source.getBranch().getId());
                continue;
            }

            if (department.getDivisionId() != null) {
                DivisionEntity division = divisionById.get(department.getDivisionId());
                if (division != null && !Objects.equals(division.getHeadEmployeeId(), employee.getId())) {
                    division.setHeadEmployeeId(employee.getId());
                    divisionsToSave.put(division.getId(), division);
                }
            }

            TeamEntity team = teamByDepartmentId.get(department.getId());
            if (team != null && !Objects.equals(team.getLeaderEmployeeId(), employee.getId())) {
                // Count only first assignment so pipeline QA matches teams-with-leader PG count
                if (team.getLeaderEmployeeId() == null) {
                    applied++;
                }
                team.setLeaderEmployeeId(employee.getId());
                teamsToSave.put(team.getId(), team);
            }
        }

        if (!divisionsToSave.isEmpty()) {
            divisionRepository.saveAll(new ArrayList<>(divisionsToSave.values()));
        }
        if (!teamsToSave.isEmpty()) {
            teamRepository.saveAll(new ArrayList<>(teamsToSave.values()));
        }

        log.info(
                "Org master head-link applied {} new team leaders ({} divisions, {} teams patched)",
                applied,
                divisionsToSave.size(),
                teamsToSave.size());
        exchange.setProperty("batchImported", applied);
    }
}
