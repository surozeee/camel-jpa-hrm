package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.LeaveBranchDepartment;
import com.jojolaptech.camel.model.mysql.Leaves;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.BranchLeaveTypeEntity;
import com.jojolaptech.camel.model.postgres.company.LeaveTypeEntity;
import com.jojolaptech.camel.repository.mysql.LeaveBranchDepartmentRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchLeaveTypeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLeaveTypeRepository;
import java.util.ArrayList;
import java.util.HashMap;
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

@Component
@RequiredArgsConstructor
public class BranchLeaveTypeProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(BranchLeaveTypeProcessor.class);

    private final PgLeaveTypeRepository leaveTypeRepository;
    private final PgBranchRepository branchRepository;
    private final PgBranchLeaveTypeRepository branchLeaveTypeRepository;
    private final LeaveBranchDepartmentRepository leaveBranchDepartmentRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Leaves> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> leaveMysqlIds = batch.stream().map(Leaves::getId).collect(Collectors.toSet());
        Map<Long, LeaveTypeEntity> leaveTypeByMysqlId = leaveTypeRepository.findByMysqlIdIn(leaveMysqlIds).stream()
                .collect(Collectors.toMap(LeaveTypeEntity::getMysqlId, Function.identity()));

        Map<Long, Set<Long>> branchMysqlIdsByLeaveId = new HashMap<>();
        List<LeaveBranchDepartment> assignments = leaveBranchDepartmentRepository.findByLeaveIdIn(leaveMysqlIds);
        for (LeaveBranchDepartment assignment : assignments) {
            if (assignment.getBranch() == null || assignment.getLeaves() == null) {
                continue;
            }
            branchMysqlIdsByLeaveId
                    .computeIfAbsent(assignment.getLeaves().getId(), id -> new HashSet<>())
                    .add(assignment.getBranch().getId());
        }

        Set<Long> companyMysqlIds = batch.stream()
                .filter(leave -> leave.getCompany() != null)
                .map(leave -> leave.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, List<BranchEntity>> branchesByCompanyMysqlId = new HashMap<>();
        for (Long companyMysqlId : companyMysqlIds) {
            branchesByCompanyMysqlId.put(
                    companyMysqlId,
                    branchRepository.findByCompanyMysqlIdOrderByMysqlIdAsc(companyMysqlId));
        }

        Set<Long> branchMysqlIds = branchesByCompanyMysqlId.values().stream()
                .flatMap(List::stream)
                .map(BranchEntity::getMysqlId)
                .collect(Collectors.toSet());
        Set<String> existingKeys = branchLeaveTypeRepository.findExistingKeys(branchMysqlIds, leaveMysqlIds).stream()
                .map(pair -> LeaveMigrationMapper.branchLeaveKey((Long) pair[0], (Long) pair[1]))
                .collect(Collectors.toSet());

        List<BranchLeaveTypeEntity> toSave = new ArrayList<>();
        for (Leaves source : batch) {
            LeaveTypeEntity leaveType = leaveTypeByMysqlId.get(source.getId());
            if (leaveType == null) {
                log.warn("Skipping branch leave type for leave id={}, leave type not migrated", source.getId());
                continue;
            }

            Set<Long> targetBranchMysqlIds = branchMysqlIdsByLeaveId.get(source.getId());
            if (targetBranchMysqlIds == null || targetBranchMysqlIds.isEmpty()) {
                List<BranchEntity> companyBranches =
                        branchesByCompanyMysqlId.getOrDefault(source.getCompany().getId(), List.of());
                targetBranchMysqlIds = companyBranches.stream()
                        .map(BranchEntity::getMysqlId)
                        .collect(Collectors.toSet());
            }

            Map<Long, BranchEntity> branchByMysqlId = branchesByCompanyMysqlId
                    .getOrDefault(source.getCompany().getId(), List.of())
                    .stream()
                    .collect(Collectors.toMap(BranchEntity::getMysqlId, Function.identity()));

            for (Long branchMysqlId : targetBranchMysqlIds) {
                String key = LeaveMigrationMapper.branchLeaveKey(branchMysqlId, source.getId());
                if (!existingKeys.add(key)) {
                    continue;
                }
                BranchEntity branch = branchByMysqlId.get(branchMysqlId);
                if (branch == null) {
                    log.warn(
                            "Skipping branch leave type leaveId={}, branch mysqlId={} not migrated",
                            source.getId(),
                            branchMysqlId);
                    continue;
                }
                toSave.add(BranchLeaveTypeEntity.builder()
                        .mysqlBranchId(branchMysqlId)
                        .mysqlLeaveId(source.getId())
                        .branchId(branch.getId())
                        .leaveType(leaveType)
                        .isPaid(leaveType.getIsPaid())
                        .requiresApproval(leaveType.getRequiresApproval())
                        .requiresMedicalCertificate(leaveType.getRequiresMedicalCertificate())
                        .maxDaysPerYear(leaveType.getMaxDaysPerYear())
                        .maxDaysPerRequest(leaveType.getMaxDaysPerRequest())
                        .canCarryForward(leaveType.getCanCarryForward())
                        .maxCarryForwardDays(leaveType.getMaxCarryForwardDays())
                        .remarks("Migrated from legacy leaves / leaveBranchDepartment")
                        .build());
            }
        }

        if (!toSave.isEmpty()) {
            branchLeaveTypeRepository.saveAll(toSave);
        }

        exchange.setProperty("batchImported", toSave.size());
    }
}
