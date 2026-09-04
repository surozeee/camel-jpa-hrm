package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.postgres.company.DepartmentEntity;
import com.jojolaptech.camel.model.postgres.company.DivisionEntity;
import com.jojolaptech.camel.repository.postgres.company.PgDepartmentRepository;
import com.jojolaptech.camel.repository.postgres.company.PgDivisionRepository;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
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
 * Step 21a: derive Division from root departments and link divisionId on the subtree.
 */
@Component
@RequiredArgsConstructor
public class DivisionSeedProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(DivisionSeedProcessor.class);

    private final PgDepartmentRepository departmentRepository;
    private final PgDivisionRepository divisionRepository;

    @Override
    public void process(Exchange exchange) {
        List<DepartmentEntity> roots = departmentRepository.findByParentDepartmentIsNull();
        if (roots.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = roots.stream()
                .map(r -> OrgMigrationMapper.orgMasterMysqlId(r.getMysqlId(), r.getMysqlBranchId()))
                .collect(Collectors.toSet());
        Map<Long, DivisionEntity> existingByMysqlId = divisionRepository.findByMysqlIdIn(mysqlIds).stream()
                .collect(Collectors.toMap(DivisionEntity::getMysqlId, d -> d, (a, b) -> a));

        Map<UUID, List<DepartmentEntity>> deptsByBranch = departmentRepository.findAll().stream()
                .collect(Collectors.groupingBy(DepartmentEntity::getBranchId));

        List<DivisionEntity> divisionsToSave = new ArrayList<>();
        List<DepartmentEntity> departmentsToSave = new ArrayList<>();
        int imported = 0;

        for (DepartmentEntity root : roots) {
            long mysqlId = OrgMigrationMapper.orgMasterMysqlId(root.getMysqlId(), root.getMysqlBranchId());
            DivisionEntity division = existingByMysqlId.get(mysqlId);
            if (division == null) {
                division = DivisionEntity.builder()
                        .mysqlId(mysqlId)
                        .code(truncate("DIV-" + root.getMysqlId() + "-" + root.getMysqlBranchId(), 64))
                        .name(root.getName())
                        .description("Derived from root department (migrated)")
                        .companyId(root.getCompanyId())
                        .branchId(root.getBranchId())
                        .headEmployeeId(null)
                        .build();
                existingByMysqlId.put(mysqlId, division);
            } else {
                division.setCode(truncate("DIV-" + root.getMysqlId() + "-" + root.getMysqlBranchId(), 64));
                division.setName(root.getName());
                division.setDescription("Derived from root department (migrated)");
                division.setCompanyId(root.getCompanyId());
                division.setBranchId(root.getBranchId());
            }
            divisionsToSave.add(division);
            imported++;
        }

        if (!divisionsToSave.isEmpty()) {
            divisionsToSave = divisionRepository.saveAll(divisionsToSave);
            for (DivisionEntity saved : divisionsToSave) {
                existingByMysqlId.put(saved.getMysqlId(), saved);
            }
        }

        for (DepartmentEntity root : roots) {
            long mysqlId = OrgMigrationMapper.orgMasterMysqlId(root.getMysqlId(), root.getMysqlBranchId());
            DivisionEntity division = existingByMysqlId.get(mysqlId);
            if (division == null || division.getId() == null) {
                continue;
            }
            UUID divisionId = division.getId();
            List<DepartmentEntity> branchDepts = deptsByBranch.getOrDefault(root.getBranchId(), List.of());
            Map<UUID, List<DepartmentEntity>> childrenByParentId = new HashMap<>();
            for (DepartmentEntity dept : branchDepts) {
                if (dept.getParentDepartment() != null && dept.getParentDepartment().getId() != null) {
                    childrenByParentId
                            .computeIfAbsent(dept.getParentDepartment().getId(), k -> new ArrayList<>())
                            .add(dept);
                }
            }

            Queue<DepartmentEntity> queue = new ArrayDeque<>();
            queue.add(root);
            Set<UUID> visited = new HashSet<>();
            while (!queue.isEmpty()) {
                DepartmentEntity current = queue.poll();
                if (current.getId() == null || !visited.add(current.getId())) {
                    continue;
                }
                if (!Objects.equals(current.getDivisionId(), divisionId)) {
                    current.setDivisionId(divisionId);
                    departmentsToSave.add(current);
                }
                for (DepartmentEntity child : childrenByParentId.getOrDefault(current.getId(), List.of())) {
                    queue.add(child);
                }
            }
        }

        if (!departmentsToSave.isEmpty()) {
            departmentRepository.saveAll(departmentsToSave);
        }

        log.info("Division seed upserted {} divisions, linked {} departments", imported, departmentsToSave.size());
        exchange.setProperty("batchImported", imported);
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
