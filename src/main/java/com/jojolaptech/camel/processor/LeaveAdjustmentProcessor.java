package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.LeaveAdjustment;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyFiscalYearEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.LeaveBalanceEntity;
import com.jojolaptech.camel.model.postgres.company.LeaveOpeningAdjustmentEntity;
import com.jojolaptech.camel.model.postgres.company.LeaveTypeEntity;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveOpeningAdjustmentActionEnum;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyFiscalYearRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLeaveBalanceRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLeaveOpeningAdjustmentRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLeaveTypeRepository;
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
public class LeaveAdjustmentProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(LeaveAdjustmentProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgLeaveTypeRepository leaveTypeRepository;
    private final PgCompanyRepository companyRepository;
    private final PgCompanyFiscalYearRepository companyFiscalYearRepository;
    private final PgLeaveBalanceRepository leaveBalanceRepository;
    private final PgLeaveOpeningAdjustmentRepository leaveOpeningAdjustmentRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<LeaveAdjustment> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> adjustmentMysqlIds = batch.stream().map(LeaveAdjustment::getId).collect(Collectors.toSet());
        Set<Long> existingAdjustmentIds =
                new HashSet<>(leaveOpeningAdjustmentRepository.findMysqlIdsByMysqlIdIn(adjustmentMysqlIds));

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<Long> leaveMysqlIds = batch.stream()
                .filter(row -> row.getLeaves() != null)
                .map(row -> row.getLeaves().getId())
                .collect(Collectors.toSet());
        Map<Long, LeaveTypeEntity> leaveTypeByMysqlId = leaveTypeRepository.findByMysqlIdIn(leaveMysqlIds).stream()
                .collect(Collectors.toMap(LeaveTypeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<Long> companyMysqlIds = batch.stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companyByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<Long> fiscalMysqlIds = batch.stream()
                .filter(row -> row.getFiscalYear() != null)
                .map(row -> row.getFiscalYear().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyFiscalYearEntity> fiscalByMysqlId =
                companyFiscalYearRepository.findByMysqlIdIn(fiscalMysqlIds).stream()
                        .collect(Collectors.toMap(CompanyFiscalYearEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<UUID> employeeIds = employeeByMysqlId.values().stream()
                .map(EmployeeEntity::getId)
                .collect(Collectors.toSet());
        Set<UUID> leaveTypeIds = leaveTypeByMysqlId.values().stream()
                .map(LeaveTypeEntity::getId)
                .collect(Collectors.toSet());
        Map<String, LeaveBalanceEntity> balanceByEmpLeave = new HashMap<>();
        if (!employeeIds.isEmpty() && !leaveTypeIds.isEmpty()) {
            for (LeaveBalanceEntity row :
                    leaveBalanceRepository.findByEmployeeIdInAndLeaveTypeIdIn(employeeIds, leaveTypeIds)) {
                balanceByEmpLeave.put(row.getEmployeeId() + ":" + row.getLeaveTypeId(), row);
            }
        }

        List<LeaveBalanceEntity> balancesToSave = new ArrayList<>();
        List<LeaveOpeningAdjustmentEntity> adjustmentsToSave = new ArrayList<>();
        int imported = 0;

        for (LeaveAdjustment source : batch) {
            if (existingAdjustmentIds.contains(source.getId())) {
                continue;
            }
            LeaveOpeningAdjustmentActionEnum action =
                    LeaveApplicationMigrationMapper.mapAdjustmentAction(source.getLeaveAdjustmentType());
            if (action == null) {
                log.warn("Skipping leaveAdjustment id={}, missing leaveAdjustmentType", source.getId());
                continue;
            }
            if (source.getEmployee() == null || source.getLeaves() == null || source.getCompany() == null) {
                log.warn("Skipping leaveAdjustment id={}, missing employee/leaves/company", source.getId());
                continue;
            }

            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping leaveAdjustment id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }
            LeaveTypeEntity leaveType = leaveTypeByMysqlId.get(source.getLeaves().getId());
            if (leaveType == null) {
                log.warn(
                        "Skipping leaveAdjustment id={}, leaveType mysqlId={} not migrated",
                        source.getId(),
                        source.getLeaves().getId());
                continue;
            }
            CompanyEntity company = companyByMysqlId.get(source.getCompany().getId());
            if (company == null) {
                log.warn(
                        "Skipping leaveAdjustment id={}, company mysqlId={} not migrated",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }

            UUID fiscalYearId = null;
            if (source.getFiscalYear() != null) {
                CompanyFiscalYearEntity fiscal = fiscalByMysqlId.get(source.getFiscalYear().getId());
                if (fiscal != null) {
                    fiscalYearId = fiscal.getId();
                }
            }

            String key = employee.getId() + ":" + leaveType.getId();
            LeaveBalanceEntity balance = balanceByEmpLeave.get(key);
            if (balance == null) {
                balance = LeaveBalanceEntity.builder()
                        .employeeId(employee.getId())
                        .leaveTypeId(leaveType.getId())
                        .totalLeaves(0.0)
                        .usedLeaves(0.0)
                        .remainingLeaves(0.0)
                        .pendingLeaves(0.0)
                        .openingLeaves(0.0)
                        .build();
                balanceByEmpLeave.put(key, balance);
            }

            double days = LeaveApplicationMigrationMapper.coalesce(source.getDays(), 0.0);
            double previousTotal = LeaveApplicationMigrationMapper.coalesce(balance.getTotalLeaves(), 0.0);
            double previousOpening = LeaveApplicationMigrationMapper.coalesce(balance.getOpeningLeaves(), 0.0);
            double previousRemaining = LeaveApplicationMigrationMapper.coalesce(balance.getRemainingLeaves(), 0.0);

            double newTotal;
            double newRemaining;
            if (action == LeaveOpeningAdjustmentActionEnum.ADD) {
                newTotal = previousTotal + days;
                newRemaining = previousRemaining + days;
            } else {
                newTotal = LeaveApplicationMigrationMapper.maxZero(previousTotal - days);
                newRemaining = LeaveApplicationMigrationMapper.maxZero(previousRemaining - days);
            }

            balance.setTotalLeaves(newTotal);
            balance.setRemainingLeaves(newRemaining);
            balancesToSave.add(balance);

            adjustmentsToSave.add(LeaveOpeningAdjustmentEntity.builder()
                    .mysqlId(source.getId())
                    .companyId(company.getId())
                    .branchId(employee.getBranchId())
                    .employeeId(employee.getId())
                    .leaveTypeId(leaveType.getId())
                    .leaveBalanceId(balance.getId())
                    .companyFiscalYearId(fiscalYearId)
                    .action(action)
                    .days(days)
                    .previousTotalLeaves(previousTotal)
                    .newTotalLeaves(newTotal)
                    .previousOpeningLeaves(previousOpening)
                    .newOpeningLeaves(previousOpening)
                    .remarks(LeaveApplicationMigrationMapper.truncate(source.getRemarks(), 500))
                    .adjustedAt(LeaveApplicationMigrationMapper.toLocalDateTime(source.getAdjustedDate()))
                    .build());
            existingAdjustmentIds.add(source.getId());
            imported++;
        }

        if (!balancesToSave.isEmpty()) {
            leaveBalanceRepository.saveAll(balancesToSave);
            // Refresh leaveBalanceId on adjustments that were created against unsaved shells.
            for (LeaveOpeningAdjustmentEntity adjustment : adjustmentsToSave) {
                if (adjustment.getLeaveBalanceId() == null) {
                    LeaveBalanceEntity balance =
                            balanceByEmpLeave.get(adjustment.getEmployeeId() + ":" + adjustment.getLeaveTypeId());
                    if (balance != null) {
                        adjustment.setLeaveBalanceId(balance.getId());
                    }
                }
            }
        }
        if (!adjustmentsToSave.isEmpty()) {
            leaveOpeningAdjustmentRepository.saveAll(adjustmentsToSave);
        }

        exchange.setProperty("batchImported", imported);
    }
}
