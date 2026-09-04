package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.LeaveBalance;
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
import java.util.Comparator;
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

@Component
@RequiredArgsConstructor
public class LeaveBalanceProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(LeaveBalanceProcessor.class);

    /** Offset so SET_OPENING audit mysql_ids never collide with leaveAdjustment ids. */
    static final long SET_OPENING_MYSQL_OFFSET = 9_000_000_000_000L;

    private final PgEmployeeRepository employeeRepository;
    private final PgLeaveTypeRepository leaveTypeRepository;
    private final PgCompanyRepository companyRepository;
    private final PgCompanyFiscalYearRepository companyFiscalYearRepository;
    private final PgLeaveBalanceRepository leaveBalanceRepository;
    private final PgLeaveOpeningAdjustmentRepository leaveOpeningAdjustmentRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<LeaveBalance> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Map<String, LeaveBalance> latestByEmpLeave = new HashMap<>();
        for (LeaveBalance row : batch) {
            if (row.getEmployee() == null || row.getLeave() == null) {
                continue;
            }
            String key = row.getEmployee().getId() + ":" + row.getLeave().getId();
            LeaveBalance existing = latestByEmpLeave.get(key);
            if (existing == null || isNewer(row, existing)) {
                latestByEmpLeave.put(key, row);
            }
        }
        List<LeaveBalance> winners = new ArrayList<>(latestByEmpLeave.values());

        Set<Long> employeeMysqlIds = winners.stream()
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<Long> leaveMysqlIds = winners.stream()
                .map(row -> row.getLeave().getId())
                .collect(Collectors.toSet());
        Map<Long, LeaveTypeEntity> leaveTypeByMysqlId = leaveTypeRepository.findByMysqlIdIn(leaveMysqlIds).stream()
                .collect(Collectors.toMap(LeaveTypeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<Long> companyMysqlIds = winners.stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companyByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<Long> fiscalMysqlIds = winners.stream()
                .filter(row -> row.getFiscal() != null)
                .map(row -> row.getFiscal().getId())
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
        Map<String, LeaveBalanceEntity> existingByEmpLeaveType = new HashMap<>();
        if (!employeeIds.isEmpty() && !leaveTypeIds.isEmpty()) {
            for (LeaveBalanceEntity row :
                    leaveBalanceRepository.findByEmployeeIdInAndLeaveTypeIdIn(employeeIds, leaveTypeIds)) {
                existingByEmpLeaveType.put(row.getEmployeeId() + ":" + row.getLeaveTypeId(), row);
            }
        }

        Set<Long> balanceMysqlIds = winners.stream().map(LeaveBalance::getId).collect(Collectors.toSet());
        Set<Long> existingBalanceMysqlIds = new HashSet<>(leaveBalanceRepository.findMysqlIdsByMysqlIdIn(balanceMysqlIds));

        Set<Long> setOpeningMysqlIds = balanceMysqlIds.stream()
                .map(id -> SET_OPENING_MYSQL_OFFSET + id)
                .collect(Collectors.toSet());
        Set<Long> existingAdjustmentMysqlIds =
                new HashSet<>(leaveOpeningAdjustmentRepository.findMysqlIdsByMysqlIdIn(setOpeningMysqlIds));

        List<LeaveBalanceEntity> balancesToSave = new ArrayList<>();
        List<LeaveOpeningAdjustmentEntity> adjustmentsToSave = new ArrayList<>();
        int imported = 0;

        for (LeaveBalance source : winners) {
            if (existingBalanceMysqlIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployee() == null || source.getLeave() == null) {
                log.warn("Skipping leaveBalance id={}, missing employee/leave", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping leaveBalance id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }
            LeaveTypeEntity leaveType = leaveTypeByMysqlId.get(source.getLeave().getId());
            if (leaveType == null) {
                log.warn(
                        "Skipping leaveBalance id={}, leaveType mysqlId={} not migrated",
                        source.getId(),
                        source.getLeave().getId());
                continue;
            }

            CompanyEntity company = source.getCompany() != null
                    ? companyByMysqlId.get(source.getCompany().getId())
                    : null;
            if (company == null) {
                log.warn("Skipping leaveBalance id={}, company not migrated", source.getId());
                continue;
            }

            String empLeaveKey = employee.getId() + ":" + leaveType.getId();
            LeaveBalanceEntity entity = existingByEmpLeaveType.get(empLeaveKey);
            if (entity != null) {
                if (entity.getMysqlId() == null) {
                    entity.setMysqlId(source.getId());
                    balancesToSave.add(entity);
                    existingBalanceMysqlIds.add(source.getId());
                    imported++;
                } else {
                    log.info(
                            "Skipping leaveBalance id={}, balance already exists for employee+leaveType (mysqlId={})",
                            source.getId(),
                            entity.getMysqlId());
                }
                continue;
            }

            double remaining = LeaveApplicationMigrationMapper.coalesce(source.getDays(), 0.0);
            double opening;
            double used;
            if (source.getOpeningLeave() == null) {
                opening = remaining;
                used = 0.0;
            } else {
                opening = LeaveApplicationMigrationMapper.coalesce(source.getOpeningLeave(), remaining);
                used = LeaveApplicationMigrationMapper.maxZero(opening - remaining);
            }
            double total = remaining + used;

            UUID openingFiscalYearId = null;
            if (source.getFiscal() != null) {
                CompanyFiscalYearEntity fiscal = fiscalByMysqlId.get(source.getFiscal().getId());
                if (fiscal != null) {
                    openingFiscalYearId = fiscal.getId();
                }
            }

            entity = LeaveBalanceEntity.builder()
                    .mysqlId(source.getId())
                    .employeeId(employee.getId())
                    .leaveTypeId(leaveType.getId())
                    .totalLeaves(total)
                    .usedLeaves(used)
                    .remainingLeaves(remaining)
                    .pendingLeaves(0.0)
                    .openingLeaves(opening)
                    .openingSetAt(LeaveApplicationMigrationMapper.toLocalDateTime(source.getSetDate()))
                    .openingFiscalYearId(openingFiscalYearId)
                    .openingRemarks("Migrated from leaveBalance")
                    .build();
            balancesToSave.add(entity);
            existingByEmpLeaveType.put(empLeaveKey, entity);
            existingBalanceMysqlIds.add(source.getId());
            imported++;
        }

        if (!balancesToSave.isEmpty()) {
            leaveBalanceRepository.saveAll(balancesToSave);
        }

        for (LeaveBalanceEntity balance : balancesToSave) {
            if (balance.getMysqlId() == null) {
                continue;
            }
            long adjustmentMysqlId = SET_OPENING_MYSQL_OFFSET + balance.getMysqlId();
            if (existingAdjustmentMysqlIds.contains(adjustmentMysqlId)) {
                continue;
            }
            LeaveBalance source = winners.stream()
                    .filter(row -> Objects.equals(row.getId(), balance.getMysqlId()))
                    .findFirst()
                    .orElse(null);
            CompanyEntity company = source != null && source.getCompany() != null
                    ? companyByMysqlId.get(source.getCompany().getId())
                    : null;
            if (company == null) {
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.values().stream()
                    .filter(e -> e.getId().equals(balance.getEmployeeId()))
                    .findFirst()
                    .orElse(null);

            adjustmentsToSave.add(LeaveOpeningAdjustmentEntity.builder()
                    .mysqlId(adjustmentMysqlId)
                    .companyId(company.getId())
                    .branchId(employee != null ? employee.getBranchId() : null)
                    .employeeId(balance.getEmployeeId())
                    .leaveTypeId(balance.getLeaveTypeId())
                    .leaveBalanceId(balance.getId())
                    .companyFiscalYearId(balance.getOpeningFiscalYearId())
                    .action(LeaveOpeningAdjustmentActionEnum.SET_OPENING)
                    .days(balance.getOpeningLeaves())
                    .previousTotalLeaves(0.0)
                    .newTotalLeaves(balance.getTotalLeaves())
                    .previousOpeningLeaves(0.0)
                    .newOpeningLeaves(balance.getOpeningLeaves())
                    .remarks(balance.getOpeningRemarks())
                    .adjustedAt(balance.getOpeningSetAt())
                    .build());
            existingAdjustmentMysqlIds.add(adjustmentMysqlId);
        }

        if (!adjustmentsToSave.isEmpty()) {
            leaveOpeningAdjustmentRepository.saveAll(adjustmentsToSave);
        }

        exchange.setProperty("batchImported", imported);
    }

    private static boolean isNewer(LeaveBalance candidate, LeaveBalance current) {
        Comparator<LeaveBalance> cmp = Comparator
                .comparing(LeaveBalance::getSetDate, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(LeaveBalance::getId, Comparator.nullsFirst(Comparator.naturalOrder()));
        return cmp.compare(candidate, current) > 0;
    }
}
