package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.BranchDepartment;
import com.jojolaptech.camel.model.mysql.Department;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.DepartmentEntity;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import com.jojolaptech.camel.repository.mysql.BranchDepartmentRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgDepartmentRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
public class DepartmentProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(DepartmentProcessor.class);
    public static final String MIGRATION_SOURCE = "departmentMigrationSource";
    public static final String SOURCE_BRANCH_DEPARTMENT = "branchDepartment";
    public static final String SOURCE_ORPHAN = "orphan";

    private final PgDepartmentRepository departmentRepository;
    private final PgBranchRepository branchRepository;
    private final BranchDepartmentRepository branchDepartmentRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        String source = exchange.getProperty(MIGRATION_SOURCE, SOURCE_BRANCH_DEPARTMENT, String.class);
        if (SOURCE_ORPHAN.equals(source)) {
            processOrphanDepartments(exchange.getMessage().getBody(List.class), exchange);
            return;
        }
        processBranchDepartments(exchange.getMessage().getBody(List.class), exchange);
    }

    @SuppressWarnings("unchecked")
    private void processBranchDepartments(List<BranchDepartment> batch, Exchange exchange) {
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        List<Long> deptIds = batch.stream().map(bd -> bd.getDepartment().getId()).toList();
        List<Long> branchIds = batch.stream().map(bd -> bd.getBranch().getId()).toList();
        Set<String> existingKeys = departmentRepository.findExistingKeys(deptIds, branchIds);

        Map<Long, BranchEntity> branchesByMysqlId = branchRepository.findByMysqlIdIn(branchIds).stream()
                .collect(Collectors.toMap(BranchEntity::getMysqlId, branch -> branch));

        List<DepartmentEntity> toSave = new ArrayList<>();
        Map<String, Department> sourceByKey = new HashMap<>();
        Set<String> keysInBatch = new HashSet<>();
        for (BranchDepartment link : batch) {
            Department department = link.getDepartment();
            Long branchMysqlId = link.getBranch().getId();
            String key = OrgMigrationMapper.departmentKey(department.getId(), branchMysqlId);
            if (existingKeys.contains(key) || !keysInBatch.add(key)) {
                continue;
            }
            BranchEntity branch = branchesByMysqlId.get(branchMysqlId);
            if (branch == null) {
                log.warn("Skipping department id={} branchId={}, branch not migrated yet",
                        department.getId(), branchMysqlId);
                continue;
            }
            toSave.add(buildDepartment(department, branch));
            sourceByKey.put(key, department);
        }

        int imported = saveAndLinkParents(toSave, sourceByKey);
        log.info("Department (branchDepartment) batch imported {} of {} rows", imported, batch.size());
        exchange.setProperty("batchImported", imported);
    }

    @SuppressWarnings("unchecked")
    private void processOrphanDepartments(List<Department> batch, Exchange exchange) {
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        List<Long> deptIds = batch.stream().map(Department::getId).toList();
        Set<Long> linkedDeptIds = branchDepartmentRepository.findDepartmentIdsLinkedToBranch(deptIds);

        Map<Long, BranchEntity> defaultBranchByCompanyId = new HashMap<>();
        List<DepartmentEntity> toSave = new ArrayList<>();
        Map<String, Department> sourceByKey = new HashMap<>();
        Set<String> keysInBatch = new HashSet<>();
        for (Department department : batch) {
            if (linkedDeptIds.contains(department.getId())) {
                continue;
            }
            Long companyMysqlId = department.getCompany().getId();
            BranchEntity branch = defaultBranchByCompanyId.computeIfAbsent(
                    companyMysqlId,
                    this::findDefaultBranchForCompany);
            if (branch == null) {
                log.warn("Skipping orphan department id={}, no branch for company mysqlId={}",
                        department.getId(), companyMysqlId);
                continue;
            }
            String key = OrgMigrationMapper.departmentKey(department.getId(), branch.getMysqlId());
            if (!keysInBatch.add(key)) {
                continue;
            }
            if (departmentRepository.findByMysqlIdAndMysqlBranchId(department.getId(), branch.getMysqlId()).isPresent()) {
                continue;
            }
            toSave.add(buildDepartment(department, branch));
            sourceByKey.put(key, department);
        }

        int imported = saveAndLinkParents(toSave, sourceByKey);
        log.info("Department (orphan) batch imported {} of {} rows", imported, batch.size());
        exchange.setProperty("batchImported", imported);
    }

    private BranchEntity findDefaultBranchForCompany(Long companyMysqlId) {
        List<BranchEntity> branches = branchRepository.findByCompanyMysqlIdOrderByMysqlIdAsc(companyMysqlId);
        return branches.isEmpty() ? null : branches.getFirst();
    }

    private DepartmentEntity buildDepartment(Department source, BranchEntity branch) {
        DepartmentEntity department = DepartmentEntity.builder()
                .mysqlId(source.getId())
                .mysqlBranchId(branch.getMysqlId())
                .name(OrgMigrationMapper.trimToNull(source.getName()))
                .description(OrgMigrationMapper.departmentDescription(source))
                .branchId(branch.getId())
                .companyId(branch.getCompany().getId())
                .isLeafDepartment(source.getParentDepartment() == null)
                .build();
        department.setStatus(StatusEnum.ACTIVE);
        return department;
    }

    private int saveAndLinkParents(List<DepartmentEntity> toSave, Map<String, Department> sourceByKey) {
        if (toSave.isEmpty()) {
            return 0;
        }

        departmentRepository.saveAll(toSave);
        departmentRepository.flush();

        List<DepartmentEntity> parentUpdates = new ArrayList<>();
        for (DepartmentEntity saved : toSave) {
            String key = OrgMigrationMapper.departmentKey(saved.getMysqlId(), saved.getMysqlBranchId());
            Department source = sourceByKey.get(key);
            if (source == null || source.getParentDepartment() == null) {
                continue;
            }
            departmentRepository
                    .findByMysqlIdAndMysqlBranchId(
                            source.getParentDepartment().getId(),
                            saved.getMysqlBranchId())
                    .ifPresent(parent -> {
                        saved.setParentDepartment(parent);
                        saved.setIsLeafDepartment(false);
                        parentUpdates.add(saved);
                    });
        }

        if (!parentUpdates.isEmpty()) {
            departmentRepository.saveAll(parentUpdates);
            departmentRepository.flush();
        }
        return toSave.size();
    }
}
