package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CalculatedOTLeaveBalance;
import com.jojolaptech.camel.model.mysql.OvertimeAccLeaveParams;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.BranchLeaveTypeEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.LeaveCreditEntity;
import com.jojolaptech.camel.model.postgres.company.LeaveTypeEntity;
import com.jojolaptech.camel.model.postgres.company.OtLeaveAccrualLineEntity;
import com.jojolaptech.camel.model.postgres.company.OtLeaveAccrualRuleEntity;
import com.jojolaptech.camel.model.postgres.company.OtLeaveAccrualRunEntity;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveCreditOperationType;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveCreditStatus;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.OtLeaveAccrualRunStatusEnum;
import com.jojolaptech.camel.model.postgres.company.enums.OtLeaveAccrualSourceEnum;
import com.jojolaptech.camel.processor.CalculatedLeaveMigrationMapper.OtValueParts;
import com.jojolaptech.camel.repository.mysql.OvertimeAccLeaveParamsRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchLeaveTypeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLeaveCreditRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLeaveTypeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgOtLeaveAccrualLineRepository;
import com.jojolaptech.camel.repository.postgres.company.PgOtLeaveAccrualRuleRepository;
import com.jojolaptech.camel.repository.postgres.company.PgOtLeaveAccrualRunRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
public class CalculatedOtLeaveAccrualProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(CalculatedOtLeaveAccrualProcessor.class);
    private static final BigDecimal DEFAULT_HOURS_EQ = new BigDecimal("8.00");

    private final PgEmployeeRepository employeeRepository;
    private final PgCompanyRepository companyRepository;
    private final PgBranchRepository branchRepository;
    private final PgLeaveTypeRepository leaveTypeRepository;
    private final PgBranchLeaveTypeRepository branchLeaveTypeRepository;
    private final OvertimeAccLeaveParamsRepository overtimeAccLeaveParamsRepository;
    private final PgOtLeaveAccrualRunRepository otLeaveAccrualRunRepository;
    private final PgOtLeaveAccrualLineRepository otLeaveAccrualLineRepository;
    private final PgOtLeaveAccrualRuleRepository otLeaveAccrualRuleRepository;
    private final PgLeaveCreditRepository leaveCreditRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<CalculatedOTLeaveBalance> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> lineMysqlIds =
                batch.stream().map(CalculatedOTLeaveBalance::getId).collect(Collectors.toSet());
        Set<Long> existingLineMysqlIds =
                new HashSet<>(otLeaveAccrualLineRepository.findMysqlIdsByMysqlIdIn(lineMysqlIds));

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<Long> companyMysqlIds = batch.stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companyByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, c -> c, (a, b) -> a));

        Map<Long, List<BranchEntity>> branchesByCompanyMysqlId =
                branchRepository.findByCompanyMysqlIdIn(companyMysqlIds).stream()
                        .collect(Collectors.groupingBy(b -> b.getCompany().getMysqlId()));

        Map<Long, OtCompanyParams> paramsByCompanyMysqlId = loadOtParams(companyMysqlIds);

        Set<Long> leaveMysqlIdsFromParams = paramsByCompanyMysqlId.values().stream()
                .map(OtCompanyParams::leaveMysqlId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, LeaveTypeEntity> leaveTypeByMysqlId = leaveTypeRepository.findByMysqlIdIn(leaveMysqlIdsFromParams)
                .stream()
                .collect(Collectors.toMap(LeaveTypeEntity::getMysqlId, l -> l, (a, b) -> a));

        LeaveTypeEntity fallbackCompensation =
                leaveTypeRepository.findCompensationTypesOrderByMysqlIdAsc().stream().findFirst().orElse(null);
        LeaveTypeEntity fallbackAny =
                leaveTypeRepository.findAllWithMysqlIdOrderByMysqlIdAsc().stream().findFirst().orElse(null);

        Set<UUID> branchIds = branchesByCompanyMysqlId.values().stream()
                .flatMap(List::stream)
                .map(BranchEntity::getId)
                .collect(Collectors.toSet());
        Map<UUID, List<BranchLeaveTypeEntity>> branchLeaveTypesByBranchId =
                branchLeaveTypeRepository.findByBranchIdInWithLeaveType(branchIds).stream()
                        .collect(Collectors.groupingBy(BranchLeaveTypeEntity::getBranchId));

        Map<String, List<CalculatedOTLeaveBalance>> groups = new LinkedHashMap<>();
        for (CalculatedOTLeaveBalance row : batch) {
            if (row.getCompany() == null || row.getCalculatedTillDate() == null) {
                continue;
            }
            LocalDate till = CalculatedLeaveMigrationMapper.toLocalDate(row.getCalculatedTillDate());
            if (till == null) {
                continue;
            }
            String key = row.getCompany().getId() + ":" + till;
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }

        Set<Long> runMysqlIds = new HashSet<>();
        for (Map.Entry<String, List<CalculatedOTLeaveBalance>> entry : groups.entrySet()) {
            CalculatedOTLeaveBalance sample = entry.getValue().getFirst();
            LocalDate till = CalculatedLeaveMigrationMapper.toLocalDate(sample.getCalculatedTillDate());
            runMysqlIds.add(CalculatedLeaveMigrationMapper.otRunMysqlId(sample.getCompany().getId(), till));
        }
        Map<Long, OtLeaveAccrualRunEntity> runByMysqlId = otLeaveAccrualRunRepository
                .findByMysqlIdIn(runMysqlIds)
                .stream()
                .collect(Collectors.toMap(OtLeaveAccrualRunEntity::getMysqlId, r -> r, (a, b) -> a));

        // Also resolve by company+period for runs created earlier without matching synthetic id lookup gaps
        Set<UUID> companyIds = companyByMysqlId.values().stream().map(CompanyEntity::getId).collect(Collectors.toSet());
        Set<LocalDate> periods = groups.values().stream()
                .map(list -> CalculatedLeaveMigrationMapper.toLocalDate(list.getFirst().getCalculatedTillDate()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!companyIds.isEmpty() && !periods.isEmpty()) {
            for (OtLeaveAccrualRunEntity run : otLeaveAccrualRunRepository
                    .findByCompanyIdInAndPeriodStartInAndPeriodEndIn(companyIds, periods, periods)) {
                if (run.getMysqlId() != null) {
                    runByMysqlId.putIfAbsent(run.getMysqlId(), run);
                }
            }
        }

        Set<Long> creditMysqlIds = lineMysqlIds.stream()
                .map(CalculatedLeaveMigrationMapper::otLeaveCreditMysqlId)
                .collect(Collectors.toSet());
        Set<Long> existingCreditMysqlIds = new HashSet<>(leaveCreditRepository.findMysqlIdsByMysqlIdIn(creditMysqlIds));

        Map<UUID, OtLeaveAccrualRuleEntity> ruleByBranchId = otLeaveAccrualRuleRepository
                .findByBranchIdIn(branchIds)
                .stream()
                .collect(Collectors.toMap(OtLeaveAccrualRuleEntity::getBranchId, r -> r, (a, b) -> a));

        List<OtLeaveAccrualRunEntity> runsToSave = new ArrayList<>();
        List<LeaveCreditEntity> creditsToSave = new ArrayList<>();
        List<OtLeaveAccrualLineEntity> linesToSave = new ArrayList<>();
        List<OtLeaveAccrualRuleEntity> rulesToSave = new ArrayList<>();
        Map<Long, RunAgg> aggByRunMysqlId = new HashMap<>();
        int imported = 0;

        for (Map.Entry<String, List<CalculatedOTLeaveBalance>> entry : groups.entrySet()) {
            List<CalculatedOTLeaveBalance> group = entry.getValue();
            CalculatedOTLeaveBalance sample = group.getFirst();
            Long companyMysqlId = sample.getCompany().getId();
            CompanyEntity company = companyByMysqlId.get(companyMysqlId);
            if (company == null) {
                log.warn(
                        "Skipping OT leave accrual group companyMysqlId={}, company not migrated",
                        companyMysqlId);
                continue;
            }

            LocalDate tillDate = CalculatedLeaveMigrationMapper.toLocalDate(sample.getCalculatedTillDate());
            if (tillDate == null) {
                continue;
            }

            List<BranchEntity> companyBranches =
                    branchesByCompanyMysqlId.getOrDefault(companyMysqlId, List.of());
            BranchEntity primaryBranch = companyBranches.isEmpty() ? null : companyBranches.getFirst();

            OtCompanyParams otParams = paramsByCompanyMysqlId.getOrDefault(companyMysqlId, OtCompanyParams.defaults());
            BigDecimal hoursEq = otParams.hoursEquivalentToOneDay();
            LeaveTypeEntity leaveType = resolveLeaveType(
                    otParams.leaveMysqlId(),
                    leaveTypeByMysqlId,
                    primaryBranch,
                    branchLeaveTypesByBranchId,
                    fallbackCompensation,
                    fallbackAny);
            if (leaveType == null) {
                log.warn(
                        "Skipping OT leave accrual group companyMysqlId={}, no leave type available",
                        companyMysqlId);
                continue;
            }

            long runMysqlId = CalculatedLeaveMigrationMapper.otRunMysqlId(companyMysqlId, tillDate);
            OtLeaveAccrualRunEntity run = runByMysqlId.get(runMysqlId);
            if (run == null) {
                run = otLeaveAccrualRunRepository
                        .findByCompanyIdAndPeriodStartAndPeriodEnd(company.getId(), tillDate, tillDate)
                        .orElse(null);
            }
            if (run == null) {
                UUID branchId = primaryBranch != null
                        ? primaryBranch.getId()
                        : resolveFirstEmployeeBranch(group, employeeByMysqlId);
                if (branchId == null) {
                    log.warn(
                            "Skipping OT leave accrual group companyMysqlId={}, till={}, no branch",
                            companyMysqlId,
                            tillDate);
                    continue;
                }
                LocalDateTime calculatedAt = tillDate.atStartOfDay();
                run = OtLeaveAccrualRunEntity.builder()
                        .mysqlId(runMysqlId)
                        .companyId(company.getId())
                        .branchId(branchId)
                        .periodStart(tillDate)
                        .periodEnd(tillDate)
                        .runStatus(OtLeaveAccrualRunStatusEnum.APPLIED)
                        .otSource(OtLeaveAccrualSourceEnum.ATTENDANCE_NET)
                        .hoursEquivalentToOneDay(hoursEq)
                        .maxLeaveDays(otParams.maxLeaveDays())
                        .leaveTypeId(leaveType.getId())
                        .calculatedAt(calculatedAt)
                        .appliedAt(calculatedAt)
                        .employeeCount(0)
                        .creditedEmployeeCount(0)
                        .totalLeaveDays(BigDecimal.ZERO)
                        .build();
                runsToSave.add(run);
                runByMysqlId.put(runMysqlId, run);
            }

            seedRuleIfMissing(
                    run.getBranchId(),
                    leaveType.getId(),
                    hoursEq,
                    otParams.maxLeaveDays(),
                    ruleByBranchId,
                    rulesToSave);

            RunAgg agg = aggByRunMysqlId.computeIfAbsent(runMysqlId, id -> new RunAgg());

            for (CalculatedOTLeaveBalance source : group) {
                if (existingLineMysqlIds.contains(source.getId())) {
                    continue;
                }
                if (source.getEmployee() == null) {
                    log.warn("Skipping calculatedOTLeaveBalance id={}, missing employee", source.getId());
                    continue;
                }
                EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
                if (employee == null) {
                    log.warn(
                            "Skipping calculatedOTLeaveBalance id={}, employee mysqlId={} not migrated",
                            source.getId(),
                            source.getEmployee().getId());
                    continue;
                }

                OtValueParts parts =
                        CalculatedLeaveMigrationMapper.parseOtValue(source.getOTValue(), hoursEq);
                long creditMysqlId = CalculatedLeaveMigrationMapper.otLeaveCreditMysqlId(source.getId());

                UUID leaveCreditId = null;
                if (!existingCreditMysqlIds.contains(creditMysqlId)) {
                    UUID creditBranchId =
                            employee.getBranchId() != null ? employee.getBranchId() : run.getBranchId();
                    if (creditBranchId == null) {
                        log.warn(
                                "Skipping calculatedOTLeaveBalance id={}, employee has null branchId",
                                source.getId());
                        continue;
                    }
                    LeaveCreditEntity credit = LeaveCreditEntity.builder()
                            .mysqlId(creditMysqlId)
                            .idempotencyKey("MIGRATE-OT-ACC-" + source.getId())
                            .companyId(company.getId())
                            .branchId(creditBranchId)
                            .employeeId(employee.getId())
                            .leaveTypeId(leaveType.getId())
                            .leaveType(LeaveTypeEnum.COMPENSATORY)
                            .operationType(LeaveCreditOperationType.OT_LEAVE_ACCRUAL)
                            .creditStatus(LeaveCreditStatus.POSTED)
                            .quantity(parts.leaveDays())
                            .effectiveDate(tillDate)
                            .periodStart(tillDate)
                            .periodEnd(tillDate)
                            .systemGenerated(true)
                            .reason("Migrated OT leave accrual")
                            .remarks(CalculatedLeaveMigrationMapper.truncate(
                                    "OTValue=" + source.getOTValue(), 1000))
                            .postedAt(tillDate.atStartOfDay())
                            .build();
                    creditsToSave.add(credit);
                    existingCreditMysqlIds.add(creditMysqlId);
                    leaveCreditId = credit.getId();
                } else {
                    leaveCreditId = leaveCreditRepository
                            .findByMysqlId(creditMysqlId)
                            .map(LeaveCreditEntity::getId)
                            .orElse(null);
                }

                OtLeaveAccrualLineEntity line = OtLeaveAccrualLineEntity.builder()
                        .mysqlId(source.getId())
                        .run(run)
                        .employeeId(employee.getId())
                        .employeeCode(employee.getEmployeeCode())
                        .employeeName(CalculatedLeaveMigrationMapper.employeeDisplayName(
                                employee.getFirstName(), employee.getMiddleName(), employee.getLastName()))
                        .leaveDays(parts.leaveDays())
                        .remainderMinutes(parts.remainderMinutes())
                        .otMinutes(parts.otMinutes())
                        .netMinutes(parts.otMinutes())
                        .undertimeMinutes(0)
                        .previousRemainderMinutes(0)
                        .leaveCreditId(leaveCreditId)
                        .build();
                linesToSave.add(line);
                existingLineMysqlIds.add(source.getId());
                agg.employeeCount++;
                agg.creditedEmployeeCount++;
                agg.totalLeaveDays = agg.totalLeaveDays.add(parts.leaveDays());
                imported++;
            }
        }

        if (!runsToSave.isEmpty()) {
            otLeaveAccrualRunRepository.saveAll(runsToSave);
        }
        if (!creditsToSave.isEmpty()) {
            leaveCreditRepository.saveAll(creditsToSave);
            // Wire leaveCreditId after persist (UUID assigned)
            Map<Long, UUID> creditIdByMysqlId = creditsToSave.stream()
                    .filter(c -> c.getMysqlId() != null)
                    .collect(Collectors.toMap(LeaveCreditEntity::getMysqlId, LeaveCreditEntity::getId, (a, b) -> a));
            for (OtLeaveAccrualLineEntity line : linesToSave) {
                if (line.getLeaveCreditId() == null && line.getMysqlId() != null) {
                    long creditMysqlId = CalculatedLeaveMigrationMapper.otLeaveCreditMysqlId(line.getMysqlId());
                    UUID creditId = creditIdByMysqlId.get(creditMysqlId);
                    if (creditId != null) {
                        line.setLeaveCreditId(creditId);
                    }
                }
            }
        }
        if (!linesToSave.isEmpty()) {
            otLeaveAccrualLineRepository.saveAll(linesToSave);
        }
        if (!rulesToSave.isEmpty()) {
            otLeaveAccrualRuleRepository.saveAll(rulesToSave);
        }

        // Update run aggregates for runs that received new lines
        Set<OtLeaveAccrualRunEntity> runsToUpdate = new HashSet<>();
        for (Map.Entry<Long, RunAgg> entry : aggByRunMysqlId.entrySet()) {
            OtLeaveAccrualRunEntity run = runByMysqlId.get(entry.getKey());
            if (run == null) {
                continue;
            }
            RunAgg agg = entry.getValue();
            if (agg.employeeCount == 0) {
                continue;
            }
            run.setEmployeeCount((run.getEmployeeCount() == null ? 0 : run.getEmployeeCount()) + agg.employeeCount);
            run.setCreditedEmployeeCount(
                    (run.getCreditedEmployeeCount() == null ? 0 : run.getCreditedEmployeeCount())
                            + agg.creditedEmployeeCount);
            BigDecimal existingTotal =
                    run.getTotalLeaveDays() == null ? BigDecimal.ZERO : run.getTotalLeaveDays();
            run.setTotalLeaveDays(existingTotal.add(agg.totalLeaveDays));
            runsToUpdate.add(run);
        }
        if (!runsToUpdate.isEmpty()) {
            otLeaveAccrualRunRepository.saveAll(runsToUpdate);
        }

        exchange.setProperty("batchImported", imported);
    }

    private Map<Long, OtCompanyParams> loadOtParams(Set<Long> companyMysqlIds) {
        Map<Long, OtCompanyParams> result = new HashMap<>();
        if (companyMysqlIds == null || companyMysqlIds.isEmpty()) {
            return result;
        }
        List<OvertimeAccLeaveParams> params =
                overtimeAccLeaveParamsRepository.findByCompanyMysqlIdIn(companyMysqlIds);
        Map<Long, List<OvertimeAccLeaveParams>> byCompany = params.stream()
                .filter(p -> p.getCompany() != null)
                .collect(Collectors.groupingBy(p -> p.getCompany().getId()));
        for (Map.Entry<Long, List<OvertimeAccLeaveParams>> entry : byCompany.entrySet()) {
            Long leaveMysqlId = null;
            BigDecimal hoursEq = DEFAULT_HOURS_EQ;
            BigDecimal maxLeaveDays = null;
            for (OvertimeAccLeaveParams param : entry.getValue()) {
                if (param.getParamName() == null || param.getParamValue() == null) {
                    continue;
                }
                String name = param.getParamName().trim();
                String value = param.getParamValue().trim();
                if ("LeaveId".equalsIgnoreCase(name) || "leave_id".equalsIgnoreCase(name)) {
                    leaveMysqlId = parseLong(value);
                } else if ("HoursEqTo1Day".equalsIgnoreCase(name)
                        || "HoursEquivalentToOneDay".equalsIgnoreCase(name)
                        || name.toLowerCase(Locale.ROOT).contains("hourseq")
                        || name.toLowerCase(Locale.ROOT).contains("hours_eq")) {
                    BigDecimal parsed = CalculatedLeaveMigrationMapper.parseDecimal(value, null);
                    if (parsed != null) {
                        hoursEq = parsed;
                    }
                } else if ("Maximum Allowed Leave Days".equalsIgnoreCase(name)
                        || "MaxLeaveDays".equalsIgnoreCase(name)
                        || name.toLowerCase(Locale.ROOT).contains("maximum allowed leave")) {
                    maxLeaveDays = CalculatedLeaveMigrationMapper.parseDecimal(value, null);
                }
            }
            result.put(entry.getKey(), new OtCompanyParams(leaveMysqlId, hoursEq, maxLeaveDays));
        }
        return result;
    }

    private static LeaveTypeEntity resolveLeaveType(
            Long leaveMysqlId,
            Map<Long, LeaveTypeEntity> leaveTypeByMysqlId,
            BranchEntity primaryBranch,
            Map<UUID, List<BranchLeaveTypeEntity>> branchLeaveTypesByBranchId,
            LeaveTypeEntity fallbackCompensation,
            LeaveTypeEntity fallbackAny) {
        if (leaveMysqlId != null) {
            LeaveTypeEntity fromParam = leaveTypeByMysqlId.get(leaveMysqlId);
            if (fromParam != null) {
                return fromParam;
            }
        }
        if (primaryBranch != null) {
            List<BranchLeaveTypeEntity> branchTypes =
                    branchLeaveTypesByBranchId.getOrDefault(primaryBranch.getId(), List.of());
            for (BranchLeaveTypeEntity blt : branchTypes) {
                if (blt.getLeaveType() != null && Boolean.TRUE.equals(blt.getLeaveType().getCompensationType())) {
                    return blt.getLeaveType();
                }
            }
            if (!branchTypes.isEmpty() && branchTypes.getFirst().getLeaveType() != null) {
                return branchTypes.getFirst().getLeaveType();
            }
        }
        if (fallbackCompensation != null) {
            return fallbackCompensation;
        }
        return fallbackAny;
    }

    private static UUID resolveFirstEmployeeBranch(
            List<CalculatedOTLeaveBalance> group, Map<Long, EmployeeEntity> employeeByMysqlId) {
        for (CalculatedOTLeaveBalance row : group) {
            if (row.getEmployee() == null) {
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(row.getEmployee().getId());
            if (employee != null && employee.getBranchId() != null) {
                return employee.getBranchId();
            }
        }
        return null;
    }

    private static void seedRuleIfMissing(
            UUID branchId,
            UUID leaveTypeId,
            BigDecimal hoursEq,
            BigDecimal maxLeaveDays,
            Map<UUID, OtLeaveAccrualRuleEntity> ruleByBranchId,
            List<OtLeaveAccrualRuleEntity> rulesToSave) {
        if (branchId == null || ruleByBranchId.containsKey(branchId)) {
            return;
        }
        OtLeaveAccrualRuleEntity rule = OtLeaveAccrualRuleEntity.builder()
                .branchId(branchId)
                .enabled(true)
                .hoursEquivalentToOneDay(hoursEq)
                .leaveTypeId(leaveTypeId)
                .otSource(OtLeaveAccrualSourceEnum.ATTENDANCE_NET)
                .carryRemainderMinutes(true)
                .maxLeaveDays(maxLeaveDays)
                .remarks("Migrated from OvertimeAccLeaveParams")
                .build();
        rulesToSave.add(rule);
        ruleByBranchId.put(branchId, rule);
    }

    private static Long parseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private record OtCompanyParams(Long leaveMysqlId, BigDecimal hoursEquivalentToOneDay, BigDecimal maxLeaveDays) {
        static OtCompanyParams defaults() {
            return new OtCompanyParams(null, DEFAULT_HOURS_EQ, null);
        }
    }

    private static final class RunAgg {
        int employeeCount;
        int creditedEmployeeCount;
        BigDecimal totalLeaveDays = BigDecimal.ZERO;
    }
}
