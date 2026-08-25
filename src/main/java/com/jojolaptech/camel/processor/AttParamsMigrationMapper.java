package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.postgres.company.enums.FyClosingNonSelectedLeaveActionEnum;
import java.math.BigDecimal;
import java.util.List;

/**
 * Maps legacy {@code attParams} EAV keys to structured migration values.
 * Includes production param names from legacy Grails HRM deployments.
 */
public final class AttParamsMigrationMapper {

    private AttParamsMigrationMapper() {
    }

    public static AttParamValues fromAttParams(List<com.jojolaptech.camel.model.mysql.AttParams> rows) {
        AttParamValues values = new AttParamValues();
        for (var row : rows) {
            values.apply(row.getParamName(), row.getParamValue());
        }
        return values;
    }

    public static void applyTimeTableDefaults(AttParamValues values, Integer lateIn, Integer earlyOut) {
        if (values.lateInMinutes == null && lateIn != null) {
            values.lateInMinutes = lateIn;
        }
        if (values.earlyOutMinutes == null && earlyOut != null) {
            values.earlyOutMinutes = earlyOut;
        }
    }

    public static class AttParamValues {
        public Boolean verifyLeaveAccumulation;
        public Boolean enableRosterShift;
        public Boolean enableAutomaticAccrual;
        public Boolean enableCarryForward;
        public Boolean enableLeaveExpiry;
        public Boolean allowNegativeBalance;
        public Boolean enableNotifications;
        public Boolean rosterRequireApproval;
        public Boolean allowEmployeeShiftChangeRequest;
        public Boolean allowUpdateWhenAttendanceExists;
        public Integer rosterCaptureBeforeMinutes;
        public Integer rosterCaptureAfterMinutes;
        public Boolean rosterAllowPunchReuse;
        public Integer duplicatePunchIntervalSeconds;
        public Boolean singlePunchMode;
        public Boolean enableBreakTracking;
        public Boolean autoRecalculateAttendance;
        public Integer lateInMinutes;
        public Integer earlyOutMinutes;
        public Integer gracePeriodMinutes;
        public Integer minimumWorkingHours;
        public Boolean allowOvertime;
        public Boolean otBeforeShift;
        public Boolean otAfterShift;
        public Integer maxOvertimeHoursPerWeek;
        public Integer maxOtCheckInMinutes;
        public Integer maxOtCheckOutMinutes;
        public Integer minsPerBasicSalOt;
        public Boolean consecutiveLatePunchSalaryDeductionEnabled;
        public Integer consecutiveLatePunchCount;
        public Integer earlyExceedCount;
        public BigDecimal salaryDaysDeductedAfterLatePunches;
        public Integer lateEarlyDeductionBasisMinutes;
        public Boolean sandwichLeaveApplicable;
        public Boolean allowFlexibleTiming;
        public Integer flexibleTimingWindowMinutes;
        public Integer cumulativeHoursLimitMinutes;
        public Long cumulativeLeaveMysqlId;
        public Long nonAccumulatedLeaveMysqlId;
        public Boolean leaveOverwriteHoliday;
        public Boolean countPresentOnOffLeaveHoliday;
        public Boolean allowWebCheckInOut;
        public Boolean disableEmployeeCheckInOut;
        public Boolean disableLeaveApplicationNotification;
        public Boolean enableEncashmentAtClose;
        public Boolean settleLeaveOnTermination;
        public FyClosingNonSelectedLeaveActionEnum fyCloseNonSelectedAction =
                FyClosingNonSelectedLeaveActionEnum.SKIP;

        public boolean hasClosingPolicy() {
            return enableEncashmentAtClose != null
                    || settleLeaveOnTermination != null
                    || cumulativeLeaveMysqlId != null
                    || fyCloseNonSelectedAction != FyClosingNonSelectedLeaveActionEnum.SKIP;
        }

        public boolean hasShiftRules() {
            return lateInMinutes != null
                    || earlyOutMinutes != null
                    || gracePeriodMinutes != null
                    || allowOvertime != null
                    || consecutiveLatePunchSalaryDeductionEnabled != null
                    || sandwichLeaveApplicable != null
                    || maxOtCheckInMinutes != null
                    || maxOtCheckOutMinutes != null;
        }

        public Boolean resolvedAllowOvertime() {
            if (allowOvertime != null) {
                return allowOvertime;
            }
            if (otBeforeShift != null || otAfterShift != null) {
                return Boolean.TRUE.equals(otBeforeShift) || Boolean.TRUE.equals(otAfterShift);
            }
            return null;
        }

        public Boolean resolvedEnableNotifications() {
            if (disableLeaveApplicationNotification != null) {
                return !disableLeaveApplicationNotification;
            }
            return enableNotifications;
        }

        public void apply(String paramName, String paramValue) {
            switch (FiscalMigrationMapper.normalizeParamName(paramName)) {
                case "verifyleaveaccumulation",
                        "leave_accumulation_verification",
                        "verify_leave_accumulation",
                        "leaveaccumulationverification" ->
                        verifyLeaveAccumulation = FiscalMigrationMapper.parseBoolean(paramValue);
                case "enablerostershift",
                        "roster_shift_enabled",
                        "enable_roster_shift",
                        "rostershiftenabled",
                        "roster_shift" ->
                        enableRosterShift = FiscalMigrationMapper.parseBoolean(paramValue);
                case "enableautomaticaccrual", "automatic_accrual", "automaticleaveaccrual", "auto_leave_accrual" ->
                        enableAutomaticAccrual = FiscalMigrationMapper.parseBoolean(paramValue);
                case "non_accumulated_leaves" -> applyNonAccumulatedLeaves(paramValue);
                case "enablecarryforward", "carry_forward", "leavecarryforward", "leave_carry_forward" ->
                        enableCarryForward = FiscalMigrationMapper.parseBoolean(paramValue);
                case "enableleaveexpiry", "leave_expiry", "leaveexpiryenabled", "leave_expiry_enabled" ->
                        enableLeaveExpiry = FiscalMigrationMapper.parseBoolean(paramValue);
                case "leave_overwrite_holiday" ->
                        leaveOverwriteHoliday = FiscalMigrationMapper.parseBoolean(paramValue);
                case "allownegativebalance",
                        "negative_leave_balance",
                        "negativeleavebalance",
                        "allow_negative_leave" ->
                        allowNegativeBalance = FiscalMigrationMapper.parseBoolean(paramValue);
                case "enablenotifications", "leave_notifications", "leavenotification", "leave_notification" ->
                        enableNotifications = FiscalMigrationMapper.parseBoolean(paramValue);
                case "disable_leave_application_sent_from_employee" ->
                        disableLeaveApplicationNotification = FiscalMigrationMapper.parseBoolean(paramValue);
                case "disable_log_in_out_notification", "disable_absent_notification" -> {
                    // Legacy notification toggles — treat as leave/attendance notifications off when disabled.
                    if (FiscalMigrationMapper.parseBoolean(paramValue)) {
                        enableNotifications = false;
                    }
                }
                case "rosterrequireapproval", "roster_approval", "rosterapprovalrequired", "roster_approval_required" ->
                        rosterRequireApproval = FiscalMigrationMapper.parseBoolean(paramValue);
                case "allowemployeeshiftchangerequest",
                        "employee_shift_change",
                        "employee_shift_change_request",
                        "allowshiftchangerequest" ->
                        allowEmployeeShiftChangeRequest = FiscalMigrationMapper.parseBoolean(paramValue);
                case "disable_check_in_out_from_employee" ->
                        disableEmployeeCheckInOut = FiscalMigrationMapper.parseBoolean(paramValue);
                case "check_in_out_computer" ->
                        allowWebCheckInOut = FiscalMigrationMapper.parseBoolean(paramValue);
                case "allowupdatewhenattendanceexists", "allow_update_when_attendance_exists", "update_roster_with_att" ->
                        allowUpdateWhenAttendanceExists = FiscalMigrationMapper.parseBoolean(paramValue);
                case "rostercapturebeforeminutes",
                        "roster_capture_before",
                        "roster_capture_before_minutes",
                        "capture_before_minutes",
                        "max_ot_check_in" ->
                        maxOtCheckInMinutes = firstInt(maxOtCheckInMinutes, paramValue);
                case "rostercaptureafterminutes",
                        "roster_capture_after",
                        "roster_capture_after_minutes",
                        "capture_after_minutes",
                        "max_ot_check_out" ->
                        maxOtCheckOutMinutes = firstInt(maxOtCheckOutMinutes, paramValue);
                case "rosterallowpunchreuse", "punch_reuse", "allow_punch_reuse", "rostercapturepunchreuse" ->
                        rosterAllowPunchReuse = FiscalMigrationMapper.parseBoolean(paramValue);
                case "duplicatepunchintervalseconds",
                        "duplicate_punch_interval",
                        "duplicatepunchtimeinterval",
                        "duplicate_punch_time_interval" ->
                        duplicatePunchIntervalSeconds = FiscalMigrationMapper.parseInteger(paramValue);
                case "singlepunchmode", "single_punch", "singlepunchattendance", "single_punch_attendance" ->
                        singlePunchMode = FiscalMigrationMapper.parseBoolean(paramValue);
                case "count_present_if_present_on_off_leaves_holiday" ->
                        countPresentOnOffLeaveHoliday = FiscalMigrationMapper.parseBoolean(paramValue);
                case "enablebreaktracking", "break_tracking", "breaktrackingenabled", "break_tracking_enabled" ->
                        enableBreakTracking = FiscalMigrationMapper.parseBoolean(paramValue);
                case "autorecalculateattendance",
                        "auto_recalculate",
                        "autorecalculateattendanceonpunch",
                        "auto_recalculate_attendance" ->
                        autoRecalculateAttendance = FiscalMigrationMapper.parseBoolean(paramValue);
                case "latein",
                        "late_in",
                        "lateinminutes",
                        "late_in_minutes",
                        "lateingraceperiod",
                        "late_in_grace_period",
                        "lateingracetime",
                        "late_mins" ->
                        lateInMinutes = FiscalMigrationMapper.parseInteger(paramValue);
                case "earlyout",
                        "early_out",
                        "earlyoutminutes",
                        "early_out_minutes",
                        "earlyoutgraceperiod",
                        "early_out_grace_period",
                        "earlyoutgracetime",
                        "early_mins" ->
                        earlyOutMinutes = FiscalMigrationMapper.parseInteger(paramValue);
                case "late_exceed" ->
                        consecutiveLatePunchCount = FiscalMigrationMapper.parseInteger(paramValue);
                case "early_exceed" ->
                        earlyExceedCount = FiscalMigrationMapper.parseInteger(paramValue);
                case "graceperiodminutes", "grace_period", "graceperiod", "grace_period_minutes" ->
                        gracePeriodMinutes = FiscalMigrationMapper.parseInteger(paramValue);
                case "minimumworkinghours", "minimum_working_hours", "minworkinghours" ->
                        minimumWorkingHours = FiscalMigrationMapper.parseInteger(paramValue);
                case "allowovertime",
                        "overtime_enabled",
                        "overtimeallowed",
                        "overtime_allowed",
                        "enable_ot_calculation" ->
                        allowOvertime = FiscalMigrationMapper.parseBoolean(paramValue);
                case "ot_before_shift" -> otBeforeShift = FiscalMigrationMapper.parseBoolean(paramValue);
                case "ot_after_shift" -> otAfterShift = FiscalMigrationMapper.parseBoolean(paramValue);
                case "weekend_ot_count" ->
                        maxOvertimeHoursPerWeek = FiscalMigrationMapper.parseInteger(paramValue);
                case "mins_per_basic_sal_ot" ->
                        minsPerBasicSalOt = FiscalMigrationMapper.parseInteger(paramValue);
                case "consecutivelatepunchsalarydeductionenabled",
                        "late_punch_deduction",
                        "deductsalaryonconsecutivelate",
                        "consecutive_late_punch_deduction",
                        "enable_late_early_deduction" ->
                        consecutiveLatePunchSalaryDeductionEnabled = FiscalMigrationMapper.parseBoolean(paramValue);
                case "consecutivelatepunchcount", "late_punch_count", "consecutivelatecount", "consecutive_late_count" ->
                        consecutiveLatePunchCount = FiscalMigrationMapper.parseInteger(paramValue);
                case "salarydaysdeductedafterconsecutivelatepunches",
                        "late_punch_deduction_days",
                        "salarydeductiondaysonlate",
                        "salary_deduction_days_on_late" ->
                        salaryDaysDeductedAfterLatePunches = FiscalMigrationMapper.parseDecimal(paramValue);
                case "count_mins_days_per_basic_sal_late_early_deduction" ->
                        lateEarlyDeductionBasisMinutes = FiscalMigrationMapper.parseInteger(paramValue);
                case "sandwichleaveapplicable", "sandwich_leave", "sandwichleave", "sandwich_leave_applicable" ->
                        sandwichLeaveApplicable = FiscalMigrationMapper.parseBoolean(paramValue);
                case "allowflexibletiming", "flexible_timing", "flexibleshifttiming", "flexible_shift_timing" ->
                        allowFlexibleTiming = FiscalMigrationMapper.parseBoolean(paramValue);
                case "flexibletimingwindowminutes", "flexible_window", "flexibletimingwindow", "flexible_window_minutes" ->
                        flexibleTimingWindowMinutes = FiscalMigrationMapper.parseInteger(paramValue);
                case "minimummonthlyworkingminutes", "minimum_monthly_working_minutes", "minmonthlyworkingminutes",
                        "cumulative_hours_limit" ->
                        cumulativeHoursLimitMinutes = FiscalMigrationMapper.parseInteger(paramValue);
                case "cumulative_leaveid" ->
                        cumulativeLeaveMysqlId = FiscalMigrationMapper.parseLongId(paramValue);
                case "fiscal_year_termination_leave_balance",
                        "fy_termination_leave_balance",
                        "fiscal_year_termination_leave" -> {
                    enableEncashmentAtClose = FiscalMigrationMapper.parseBoolean(paramValue);
                    fyCloseNonSelectedAction = FiscalMigrationMapper.parseBoolean(paramValue)
                            ? FyClosingNonSelectedLeaveActionEnum.LAPSE
                            : FyClosingNonSelectedLeaveActionEnum.SKIP;
                }
                case "enableencashmentatclose", "leave_encashment_at_close", "encashmentonfyclose", "leave_encashment" ->
                        enableEncashmentAtClose = FiscalMigrationMapper.parseBoolean(paramValue);
                case "settleleaveontermination", "termination_leave_settlement", "settle_leave_on_termination" ->
                        settleLeaveOnTermination = FiscalMigrationMapper.parseBoolean(paramValue);
                case "disable_remarks_sent_from_employee" -> {
                    // No dedicated ERP field yet — preserved via migration note only.
                }
                default -> {
                    // Unknown legacy AttParams key — ignored
                }
            }
        }

        private void applyNonAccumulatedLeaves(String paramValue) {
            Long leaveId = FiscalMigrationMapper.parseLongId(paramValue);
            if (leaveId != null) {
                nonAccumulatedLeaveMysqlId = leaveId;
                return;
            }
            if (FiscalMigrationMapper.parseBoolean(paramValue)) {
                enableAutomaticAccrual = false;
            }
        }

        private static Integer firstInt(Integer current, String paramValue) {
            Integer parsed = FiscalMigrationMapper.parseInteger(paramValue);
            return current != null ? current : parsed;
        }
    }
}
