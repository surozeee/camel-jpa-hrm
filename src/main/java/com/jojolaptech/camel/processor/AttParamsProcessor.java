package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.AttTimeTable;
import com.jojolaptech.camel.model.mysql.Company;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.BranchRosterSettingsEntity;
import com.jojolaptech.camel.model.postgres.company.BranchShiftEntity;
import com.jojolaptech.camel.model.postgres.company.BranchShiftRuleEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyFiscalYearClosingPolicyEntity;
import com.jojolaptech.camel.model.postgres.company.LeavePolicyEntity;
import com.jojolaptech.camel.processor.AttParamsMigrationMapper.AttParamValues;
import com.jojolaptech.camel.repository.mysql.AttParamsRepository;
import com.jojolaptech.camel.repository.mysql.AttTimeTableRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRosterSettingsRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchShiftRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchShiftRuleRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyFiscalYearClosingPolicyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLeavePolicyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLeaveTypeRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
public class AttParamsProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(AttParamsProcessor.class);

    private final AttParamsRepository attParamsRepository;
    private final AttTimeTableRepository attTimeTableRepository;
    private final PgCompanyRepository companyRepository;
    private final PgBranchRepository branchRepository;
    private final PgLeavePolicyRepository leavePolicyRepository;
    private final PgLeaveTypeRepository leaveTypeRepository;
    private final PgBranchRosterSettingsRepository rosterSettingsRepository;
    private final PgBranchShiftRepository branchShiftRepository;
    private final PgBranchShiftRuleRepository branchShiftRuleRepository;
    private final PgCompanyFiscalYearClosingPolicyRepository closingPolicyRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Company> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> companyMysqlIds = batch.stream().map(Company::getId).collect(Collectors.toSet());
        Map<Long, CompanyEntity> companies = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, Function.identity()));
        Map<Long, List<com.jojolaptech.camel.model.mysql.AttParams>> attParamsByCompany =
                attParamsRepository.findByCompanyIdIn(companyMysqlIds).stream()
                        .collect(Collectors.groupingBy(row -> row.getCompany().getId()));
        Map<Long, AttTimeTable> timeTableByCompany = attTimeTableRepository.findByCompanyIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(
                        row -> row.getCompany().getId(),
                        Function.identity(),
                        (left, right) -> left.getId() <= right.getId() ? left : right));

        List<LeavePolicyEntity> leavePolicies = new ArrayList<>();
        List<BranchRosterSettingsEntity> rosterSettings = new ArrayList<>();
        List<BranchShiftEntity> branchShifts = new ArrayList<>();
        List<PendingShiftRule> pendingShiftRules = new ArrayList<>();
        List<CompanyFiscalYearClosingPolicyEntity> closingPolicies = new ArrayList<>();
        List<CompanyEntity> companiesToUpdate = new ArrayList<>();

        Set<UUID> existingPolicyCompanies = closingPolicyRepository.findExistingCompanyIds(companies.values().stream()
                .map(CompanyEntity::getId)
                .toList());

        for (Company mysqlCompany : batch) {
            CompanyEntity company = companies.get(mysqlCompany.getId());
            if (company == null) {
                log.warn("Skipping company defaults for mysqlId={}, not migrated to postgres", mysqlCompany.getId());
                continue;
            }

            AttParamValues values = AttParamsMigrationMapper.fromAttParams(
                    attParamsByCompany.getOrDefault(mysqlCompany.getId(), List.of()));
            AttTimeTable timeTable = timeTableByCompany.get(mysqlCompany.getId());
            if (timeTable != null) {
                AttParamsMigrationMapper.applyTimeTableDefaults(values, timeTable.getLateIn(), timeTable.getEarlyOut());
            }

            applyCompanyFlags(company, mysqlCompany, values);
            companiesToUpdate.add(company);

            if (!existingPolicyCompanies.contains(company.getId()) && values.hasClosingPolicy()) {
                closingPolicies.add(buildClosingPolicy(company.getId(), values));
                existingPolicyCompanies.add(company.getId());
            }

            List<BranchEntity> branches =
                    branchRepository.findByCompanyMysqlIdOrderByMysqlIdAsc(mysqlCompany.getId());
            Set<Long> branchMysqlIds = branches.stream().map(BranchEntity::getMysqlId).collect(Collectors.toSet());
            Set<Long> existingLeavePolicyBranches =
                    leavePolicyRepository.findMysqlBranchIdsByMysqlBranchIdIn(branchMysqlIds);
            Set<UUID> branchIds = branches.stream().map(BranchEntity::getId).collect(Collectors.toSet());
            Set<UUID> existingRosterBranches = rosterSettingsRepository.findExistingBranchIds(branchIds);
            Set<Long> existingShiftBranches =
                    branchShiftRepository.findMysqlBranchIdsByMysqlBranchIdIn(branchMysqlIds);
            Map<Long, List<BranchShiftEntity>> shiftsByBranchMysqlId =
                    branchShiftRepository.findByMysqlBranchIdIn(branchMysqlIds).stream()
                            .collect(Collectors.groupingBy(BranchShiftEntity::getMysqlBranchId));

            for (BranchEntity branch : branches) {
                if (!existingLeavePolicyBranches.contains(branch.getMysqlId())) {
                    leavePolicies.add(buildLeavePolicy(branch, mysqlCompany, values));
                    existingLeavePolicyBranches.add(branch.getMysqlId());
                }
                if (!existingRosterBranches.contains(branch.getId())) {
                    rosterSettings.add(buildRosterSettings(branch.getId(), values));
                    existingRosterBranches.add(branch.getId());
                }

                List<BranchShiftEntity> shifts =
                        new ArrayList<>(shiftsByBranchMysqlId.getOrDefault(branch.getMysqlId(), List.of()));
                if (shifts.isEmpty() && !existingShiftBranches.contains(branch.getMysqlId())) {
                    BranchShiftEntity shift = buildDefaultShift(branch);
                    branchShifts.add(shift);
                    shifts.add(shift);
                    existingShiftBranches.add(branch.getMysqlId());
                }
                shiftsByBranchMysqlId.put(branch.getMysqlId(), shifts);
                for (BranchShiftEntity shift : shifts) {
                    pendingShiftRules.add(new PendingShiftRule(branch, shift, values));
                }
            }
        }

        int imported = persist(
                companiesToUpdate,
                closingPolicies,
                leavePolicies,
                rosterSettings,
                branchShifts,
                pendingShiftRules);
        exchange.setProperty("batchImported", imported);
    }

    private int persist(
            List<CompanyEntity> companiesToUpdate,
            List<CompanyFiscalYearClosingPolicyEntity> closingPolicies,
            List<LeavePolicyEntity> leavePolicies,
            List<BranchRosterSettingsEntity> rosterSettings,
            List<BranchShiftEntity> branchShifts,
            List<PendingShiftRule> pendingShiftRules) {
        int imported = 0;
        if (!companiesToUpdate.isEmpty()) {
            companyRepository.saveAll(new ArrayList<>(new HashSet<>(companiesToUpdate)));
            imported += companiesToUpdate.size();
        }
        if (!closingPolicies.isEmpty()) {
            closingPolicyRepository.saveAll(closingPolicies);
            imported += closingPolicies.size();
        }
        if (!leavePolicies.isEmpty()) {
            leavePolicyRepository.saveAll(leavePolicies);
            imported += leavePolicies.size();
        }
        if (!rosterSettings.isEmpty()) {
            rosterSettingsRepository.saveAll(rosterSettings);
            imported += rosterSettings.size();
        }
        if (!branchShifts.isEmpty()) {
            branchShiftRepository.saveAll(branchShifts);
            branchShiftRepository.flush();
            imported += branchShifts.size();
        }
        if (!pendingShiftRules.isEmpty()) {
            Set<UUID> shiftIds = pendingShiftRules.stream()
                    .map(rule -> rule.shift().getId())
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
            Set<UUID> existingRuleShiftIds = branchShiftRuleRepository.findExistingBranchShiftIds(shiftIds);
            List<BranchShiftRuleEntity> newRules = new ArrayList<>();
            for (PendingShiftRule pending : pendingShiftRules) {
                BranchShiftEntity shift = pending.shift();
                if (shift.getId() == null || existingRuleShiftIds.contains(shift.getId())) {
                    continue;
                }
                newRules.add(buildShiftRule(pending.branch(), shift, pending.values()));
                existingRuleShiftIds.add(shift.getId());
            }
            if (!newRules.isEmpty()) {
                branchShiftRuleRepository.saveAll(newRules);
                imported += newRules.size();
            }
        }
        return imported;
    }

    private void applyCompanyFlags(CompanyEntity company, Company mysqlCompany, AttParamValues values) {
        if (mysqlCompany.getVerifyLeaveAccumulation() != null) {
            company.setEnableLeaveAccumulation(mysqlCompany.getVerifyLeaveAccumulation());
        } else if (values.verifyLeaveAccumulation != null) {
            company.setEnableLeaveAccumulation(values.verifyLeaveAccumulation);
        }
        if (values.enableRosterShift != null) {
            company.setEnableRosterShift(values.enableRosterShift);
        }
        if (Boolean.TRUE.equals(values.resolvedAllowOvertime())) {
            company.setEnableTimeOvertime(true);
        }
    }

    private CompanyFiscalYearClosingPolicyEntity buildClosingPolicy(UUID companyId, AttParamValues values) {
        Set<UUID> carryForwardLeaveTypeIds = new HashSet<>();
        if (values.cumulativeLeaveMysqlId != null) {
            leaveTypeRepository.findByMysqlId(values.cumulativeLeaveMysqlId)
                    .ifPresent(leaveType -> carryForwardLeaveTypeIds.add(leaveType.getId()));
        }
        return CompanyFiscalYearClosingPolicyEntity.builder()
                .companyId(companyId)
                .carryForwardLeaveTypeIds(carryForwardLeaveTypeIds)
                .nonSelectedLeaveAction(values.fyCloseNonSelectedAction)
                .enableEncashmentAtClose(Boolean.TRUE.equals(values.enableEncashmentAtClose))
                .settleLeaveOnTermination(Boolean.TRUE.equals(values.settleLeaveOnTermination))
                .autoSeedChecklist(true)
                .remarks("Migrated from legacy AttParams")
                .build();
    }

    private LeavePolicyEntity buildLeavePolicy(BranchEntity branch, Company mysqlCompany, AttParamValues values) {
        Boolean enableAccumulation = values.verifyLeaveAccumulation != null
                ? values.verifyLeaveAccumulation
                : mysqlCompany.getVerifyLeaveAccumulation();
        String remarks = buildLeavePolicyRemarks(values);
        return LeavePolicyEntity.builder()
                .mysqlBranchId(branch.getMysqlId())
                .branchId(branch.getId())
                .policyName("Default Leave Policy")
                .enableLeaveAccumulation(enableAccumulation)
                .enableAutomaticAccrual(values.enableAutomaticAccrual)
                .enableCarryForward(values.enableCarryForward)
                .enableLeaveExpiry(values.leaveOverwriteHoliday != null
                        ? values.leaveOverwriteHoliday
                        : values.enableLeaveExpiry)
                .allowNegativeBalance(values.allowNegativeBalance)
                .enableNotifications(values.resolvedEnableNotifications())
                .remarks(remarks)
                .build();
    }

    private static String buildLeavePolicyRemarks(AttParamValues values) {
        StringBuilder remarks = new StringBuilder("Migrated from legacy AttParams / company defaults");
        if (values.nonAccumulatedLeaveMysqlId != null) {
            remarks.append("; nonAccumulatedLeaveId=").append(values.nonAccumulatedLeaveMysqlId);
        }
        if (Boolean.TRUE.equals(values.leaveOverwriteHoliday)) {
            remarks.append("; leaveOverwriteHoliday=true");
        }
        if (Boolean.TRUE.equals(values.countPresentOnOffLeaveHoliday)) {
            remarks.append("; countPresentOnOffLeaveHoliday=true");
        }
        return remarks.toString();
    }

    private BranchRosterSettingsEntity buildRosterSettings(UUID branchId, AttParamValues values) {
        Boolean allowShiftChange = values.allowEmployeeShiftChangeRequest;
        if (values.disableEmployeeCheckInOut != null) {
            allowShiftChange = !values.disableEmployeeCheckInOut;
        }
        int captureBefore = defaultInt(
                values.rosterCaptureBeforeMinutes != null
                        ? values.rosterCaptureBeforeMinutes
                        : values.maxOtCheckInMinutes,
                120);
        int captureAfter = defaultInt(
                values.rosterCaptureAfterMinutes != null
                        ? values.rosterCaptureAfterMinutes
                        : values.maxOtCheckOutMinutes,
                120);
        String remarks = "Migrated from legacy AttParams / company defaults";
        if (Boolean.TRUE.equals(values.allowWebCheckInOut)) {
            remarks += "; webCheckInOut=true";
        }
        return BranchRosterSettingsEntity.builder()
                .branchId(branchId)
                .requireApproval(values.rosterRequireApproval)
                .allowEmployeeShiftChangeRequest(allowShiftChange)
                .allowUpdateWhenAttendanceExists(values.allowUpdateWhenAttendanceExists)
                .rosterCaptureBeforeMinutes(captureBefore)
                .rosterCaptureAfterMinutes(captureAfter)
                .rosterAllowPunchReuse(values.rosterAllowPunchReuse)
                .duplicatePunchIntervalSeconds(defaultInt(values.duplicatePunchIntervalSeconds, 60))
                .singlePunchMode(values.countPresentOnOffLeaveHoliday != null
                        ? values.countPresentOnOffLeaveHoliday
                        : values.singlePunchMode)
                .enableBreakTracking(values.enableBreakTracking)
                .autoRecalculateAttendance(values.autoRecalculateAttendance)
                .remarks(remarks)
                .build();
    }

    private BranchShiftEntity buildDefaultShift(BranchEntity branch) {
        return BranchShiftEntity.builder()
                .mysqlBranchId(branch.getMysqlId())
                .branchId(branch.getId())
                .name("Default Shift")
                .code("DEFAULT-" + branch.getMysqlId())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .workingHours(8)
                .isFlexible(false)
                .isNightShift(false)
                .description("Default shift created during AttParams migration")
                .build();
    }

    private BranchShiftRuleEntity buildShiftRule(
            BranchEntity branch, BranchShiftEntity shift, AttParamValues values) {
        Integer latePunchThreshold = values.consecutiveLatePunchCount;
        if (latePunchThreshold == null && values.earlyExceedCount != null) {
            latePunchThreshold = values.earlyExceedCount;
        }
        BigDecimal otMultiplier = null;
        if (values.minsPerBasicSalOt != null && values.minsPerBasicSalOt > 0) {
            otMultiplier = BigDecimal.valueOf(values.minsPerBasicSalOt);
        }
        return BranchShiftRuleEntity.builder()
                .branchId(branch.getId())
                .branchShiftId(shift.getId())
                .lateArrivalToleranceMinutes(values.lateInMinutes)
                .earlyDepartureToleranceMinutes(values.earlyOutMinutes)
                .gracePeriodMinutes(values.gracePeriodMinutes != null
                        ? values.gracePeriodMinutes
                        : values.lateEarlyDeductionBasisMinutes)
                .minimumWorkingHours(values.minimumWorkingHours)
                .allowOvertime(values.resolvedAllowOvertime())
                .maxOvertimeHoursPerWeek(values.maxOvertimeHoursPerWeek)
                .maxOvertimeHoursPerDay(firstNonNull(values.maxOtCheckInMinutes, values.maxOtCheckOutMinutes))
                .overtimeRateMultiplier(otMultiplier)
                .consecutiveLatePunchSalaryDeductionEnabled(values.consecutiveLatePunchSalaryDeductionEnabled)
                .consecutiveLatePunchCountForSalaryDeduction(latePunchThreshold)
                .salaryDaysDeductedAfterConsecutiveLatePunches(values.salaryDaysDeductedAfterLatePunches)
                .minimumMonthlyWorkingMinutes(values.cumulativeHoursLimitMinutes)
                .deductSalaryIfBelowMinimumMonthlyMinutes(values.consecutiveLatePunchSalaryDeductionEnabled)
                .sandwichLeaveApplicable(values.sandwichLeaveApplicable)
                .allowFlexibleTiming(values.allowFlexibleTiming)
                .flexibleTimingWindowMinutes(values.flexibleTimingWindowMinutes)
                .build();
    }

    private static Integer firstNonNull(Integer first, Integer second) {
        return first != null ? first : second;
    }

    private static Integer defaultInt(Integer value, int fallback) {
        return value != null ? value : fallback;
    }

    private record PendingShiftRule(BranchEntity branch, BranchShiftEntity shift, AttParamValues values) {
    }
}
