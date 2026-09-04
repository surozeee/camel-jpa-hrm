package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.LeaveApplication;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.LeaveBalanceEntity;
import com.jojolaptech.camel.model.postgres.company.LeaveRequestEntity;
import com.jojolaptech.camel.model.postgres.company.LeaveTypeEntity;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveStatusEnum;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLeaveBalanceRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLeaveRequestRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLeaveTypeRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

@Component
@RequiredArgsConstructor
public class LeaveApplicationProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(LeaveApplicationProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgLeaveTypeRepository leaveTypeRepository;
    private final PgLeaveRequestRepository leaveRequestRepository;
    private final PgLeaveBalanceRepository leaveBalanceRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<LeaveApplication> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(LeaveApplication::getId).collect(Collectors.toSet());
        Set<Long> existingIds = new HashSet<>(leaveRequestRepository.findMysqlIdsByMysqlIdIn(mysqlIds));

        Set<Long> employeeMysqlIds = new HashSet<>();
        for (LeaveApplication row : batch) {
            if (row.getEmployee() != null) {
                employeeMysqlIds.add(row.getEmployee().getId());
            }
            if (row.getApprovedByEmp() != null) {
                employeeMysqlIds.add(row.getApprovedByEmp().getId());
            }
            if (row.getRecommendedByEmp() != null) {
                employeeMysqlIds.add(row.getRecommendedByEmp().getId());
            }
        }
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<Long> leaveMysqlIds = batch.stream()
                .filter(row -> row.getLeave() != null)
                .map(row -> row.getLeave().getId())
                .collect(Collectors.toSet());
        Map<Long, LeaveTypeEntity> leaveTypeByMysqlId = leaveTypeRepository.findByMysqlIdIn(leaveMysqlIds).stream()
                .collect(Collectors.toMap(LeaveTypeEntity::getMysqlId, e -> e, (a, b) -> a));

        List<LeaveRequestEntity> toSave = new ArrayList<>();
        Map<String, Double> pendingBumpByEmpLeave = new HashMap<>();
        int imported = 0;

        for (LeaveApplication source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployee() == null || source.getLeave() == null) {
                log.warn("Skipping leaveApplication id={}, missing employee/leave", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping leaveApplication id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }
            LeaveTypeEntity leaveType = leaveTypeByMysqlId.get(source.getLeave().getId());
            if (leaveType == null) {
                log.warn(
                        "Skipping leaveApplication id={}, leaveType mysqlId={} not migrated",
                        source.getId(),
                        source.getLeave().getId());
                continue;
            }

            LocalDate startDate = LeaveApplicationMigrationMapper.toLocalDate(source.getLeaveFrom());
            LocalDate endDate = LeaveApplicationMigrationMapper.toLocalDate(source.getLeaveTo());
            if (startDate == null || endDate == null) {
                log.warn("Skipping leaveApplication id={}, missing leaveFrom/leaveTo", source.getId());
                continue;
            }

            boolean hadRecommendation = source.getRecommendedByEmp() != null;
            LeaveStatusEnum status =
                    LeaveApplicationMigrationMapper.mapApplicationStatus(source.getStatus(), hadRecommendation);

            String reason;
            if (source.getSubject() != null && source.getBody() != null && !source.getBody().isBlank()) {
                reason = LeaveApplicationMigrationMapper.truncate(
                        source.getSubject().trim() + ": " + source.getBody().trim(), 500);
            } else {
                reason = LeaveApplicationMigrationMapper.truncate(source.getSubject(), 500);
            }

            LeaveRequestEntity entity = LeaveRequestEntity.builder()
                    .mysqlId(source.getId())
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalDays(LeaveApplicationMigrationMapper.coalesce(source.getTotalDays(), 0.0))
                    .leaveDuration(LeaveApplicationMigrationMapper.mapDuration(source.getLeaveType()))
                    .leaveTypeId(leaveType.getId())
                    .leaveStatus(status)
                    .reason(reason)
                    .remarks(LeaveApplicationMigrationMapper.truncate(source.getAdminRemarks(), 500))
                    .employeeId(employee.getId())
                    .build();

            if (status == LeaveStatusEnum.APPROVED) {
                UUID approvedBy = resolveEmployeeUuid(source.getApprovedByEmp(), employeeByMysqlId);
                var approvedDate = LeaveApplicationMigrationMapper.toLocalDateTime(source.getCheckedDate());
                entity.setApprovedBy(approvedBy);
                entity.setApprovedDate(approvedDate);
                entity.setHrApprovedBy(approvedBy);
                entity.setHrApprovedDate(approvedDate);
            } else if (status == LeaveStatusEnum.SUPERVISOR_RECOMMENDED) {
                entity.setSupervisorApprovedBy(
                        resolveEmployeeUuid(source.getRecommendedByEmp(), employeeByMysqlId));
                entity.setSupervisorApprovedDate(
                        LeaveApplicationMigrationMapper.toLocalDateTime(source.getCheckedDate()));
            } else if (status == LeaveStatusEnum.REJECTED_BY_SUPERVISOR
                    || status == LeaveStatusEnum.REJECTED_BY_FINAL_APPROVER) {
                String rejectionReason = source.getDiscardRemark() != null && !source.getDiscardRemark().isBlank()
                        ? source.getDiscardRemark()
                        : source.getAdminRemarks();
                entity.setRejectionReason(LeaveApplicationMigrationMapper.truncate(rejectionReason, 500));
                entity.setRejectedDate(LeaveApplicationMigrationMapper.toLocalDateTime(source.getCheckedDate()));
                UUID rejector = resolveEmployeeUuid(source.getApprovedByEmp(), employeeByMysqlId);
                if (rejector == null) {
                    rejector = resolveEmployeeUuid(source.getRecommendedByEmp(), employeeByMysqlId);
                }
                entity.setRejectedBy(rejector);
                if (status == LeaveStatusEnum.REJECTED_BY_SUPERVISOR) {
                    entity.setSupervisorRejectedBy(rejector);
                    entity.setSupervisorRejectedDate(entity.getRejectedDate());
                    entity.setSupervisorRejectionReason(entity.getRejectionReason());
                } else {
                    entity.setHrRejectedBy(rejector);
                    entity.setHrRejectedDate(entity.getRejectedDate());
                    entity.setHrRejectionReason(entity.getRejectionReason());
                }
            }

            toSave.add(entity);
            existingIds.add(source.getId());
            imported++;

            if (status == LeaveStatusEnum.PENDING_SUPERVISOR_REVIEW
                    || status == LeaveStatusEnum.SUPERVISOR_RECOMMENDED) {
                String bumpKey = employee.getId() + ":" + leaveType.getId();
                double days = LeaveApplicationMigrationMapper.coalesce(source.getTotalDays(), 0.0);
                pendingBumpByEmpLeave.merge(bumpKey, days, Double::sum);
            }
        }

        if (!toSave.isEmpty()) {
            leaveRequestRepository.saveAll(toSave);
        }

        if (!pendingBumpByEmpLeave.isEmpty()) {
            bumpPendingLeaves(pendingBumpByEmpLeave);
        }

        exchange.setProperty("batchImported", imported);
    }

    private void bumpPendingLeaves(Map<String, Double> pendingBumpByEmpLeave) {
        Set<UUID> employeeIds = new HashSet<>();
        Set<UUID> leaveTypeIds = new HashSet<>();
        for (String key : pendingBumpByEmpLeave.keySet()) {
            String[] parts = key.split(":");
            employeeIds.add(UUID.fromString(parts[0]));
            leaveTypeIds.add(UUID.fromString(parts[1]));
        }
        Map<String, LeaveBalanceEntity> balances = new HashMap<>();
        for (LeaveBalanceEntity row :
                leaveBalanceRepository.findByEmployeeIdInAndLeaveTypeIdIn(employeeIds, leaveTypeIds)) {
            balances.put(row.getEmployeeId() + ":" + row.getLeaveTypeId(), row);
        }
        List<LeaveBalanceEntity> toSave = new ArrayList<>();
        for (Map.Entry<String, Double> entry : pendingBumpByEmpLeave.entrySet()) {
            LeaveBalanceEntity balance = balances.get(entry.getKey());
            if (balance == null) {
                continue;
            }
            double current = LeaveApplicationMigrationMapper.coalesce(balance.getPendingLeaves(), 0.0);
            balance.setPendingLeaves(current + entry.getValue());
            toSave.add(balance);
        }
        if (!toSave.isEmpty()) {
            leaveBalanceRepository.saveAll(toSave);
        }
    }

    private static UUID resolveEmployeeUuid(
            com.jojolaptech.camel.model.mysql.Employee source, Map<Long, EmployeeEntity> byMysqlId) {
        if (source == null) {
            return null;
        }
        EmployeeEntity entity = byMysqlId.get(source.getId());
        return entity != null ? entity.getId() : null;
    }
}
