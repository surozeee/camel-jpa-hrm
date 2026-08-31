package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.OvertimeAccLeaveParams;
import com.jojolaptech.camel.model.postgres.company.enums.AccumulationPeriodEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveAccumulationUnitEnum;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import lombok.Getter;

final class OvertimeAccLeaveMigrationMapper {

    /** Keeps OT rule mysql_id disjoint from autoLeaveAccParams rule ids. */
    static final long OT_RULE_MYSQL_ID_OFFSET = 8_000_000_000_000L;

    private OvertimeAccLeaveMigrationMapper() {}

    record OtBundleKey(long companyMysqlId, long paramDateEpochMs) {
        static OtBundleKey from(List<OvertimeAccLeaveParams> params) {
            OvertimeAccLeaveParams sample = params.getFirst();
            return new OtBundleKey(sample.getCompany().getId(), sample.getParamDate().getTime());
        }
    }

    @Getter
    static final class OtAccValues {
        private boolean enabled = true;
        private Long leaveMysqlId;
        private LeaveAccumulationUnitEnum unit = LeaveAccumulationUnitEnum.DAYS;
        private BigDecimal leaveDays = BigDecimal.ONE;
        private AccumulationPeriodEnum accumulationPeriod = AccumulationPeriodEnum.MONTH;
        private Boolean requireConfirmation = false;
        private LocalDate effectiveFrom;
    }

    static OtAccValues fromParams(List<OvertimeAccLeaveParams> params) {
        OtAccValues values = new OtAccValues();
        for (OvertimeAccLeaveParams param : params) {
            if (param.getParamName() == null || param.getParamValue() == null) {
                continue;
            }
            switch (param.getParamName().trim()) {
                case "LeaveId" -> values.leaveMysqlId = parseLong(param.getParamValue());
                case "DaysToAdd" -> {
                    values.unit = LeaveAccumulationUnitEnum.DAYS;
                    values.leaveDays = parseDecimal(param.getParamValue(), values.leaveDays);
                }
                case "MinutesToAdd" -> {
                    values.unit = LeaveAccumulationUnitEnum.HOURS;
                    values.leaveDays = minutesToHours(param.getParamValue(), values.leaveDays);
                }
                case "isEditable", "enabled", "isActive" ->
                        values.enabled = !"false".equalsIgnoreCase(param.getParamValue().trim());
                case "requireConfirmation" ->
                        values.requireConfirmation = Boolean.TRUE.equals(parseBoolean(param.getParamValue()));
                default -> applyNormalizedParam(values, param.getParamName(), param.getParamValue());
            }
        }
        values.effectiveFrom = resolveEffectiveFrom(params);
        return values;
    }

    static long bundleMysqlId(List<OvertimeAccLeaveParams> params) {
        return params.stream().mapToLong(OvertimeAccLeaveParams::getId).min().orElse(0L);
    }

    static long ruleMysqlId(long bundleMysqlId, long branchMysqlId) {
        return OT_RULE_MYSQL_ID_OFFSET + bundleMysqlId * 1_000_000L + branchMysqlId;
    }

    static boolean isOvertimeRuleMysqlId(Long mysqlId) {
        return mysqlId != null && mysqlId >= OT_RULE_MYSQL_ID_OFFSET;
    }

    private static void applyNormalizedParam(OtAccValues values, String paramName, String paramValue) {
        switch (FiscalMigrationMapper.normalizeParamName(paramName)) {
            case "days_to_add", "leave_days", "days", "accumulation_days" -> {
                values.unit = LeaveAccumulationUnitEnum.DAYS;
                values.leaveDays = parseDecimal(paramValue, values.leaveDays);
            }
            case "minutes_to_add" -> {
                values.unit = LeaveAccumulationUnitEnum.HOURS;
                values.leaveDays = minutesToHours(paramValue, values.leaveDays);
            }
            case "leave_id" -> values.leaveMysqlId = parseLong(paramValue);
            case "is_editable" -> values.requireConfirmation = !Boolean.TRUE.equals(parseBoolean(paramValue));
            default -> {
                // ignore unknown legacy params
            }
        }
    }

    private static LocalDate resolveEffectiveFrom(List<OvertimeAccLeaveParams> params) {
        Date paramDate = params.getFirst().getParamDate();
        return paramDate != null ? AttendanceMigrationMapper.toLocalDate(paramDate) : null;
    }

    private static BigDecimal minutesToHours(String value, BigDecimal fallback) {
        BigDecimal minutes = parseDecimalOrNull(value);
        if (minutes == null) {
            return fallback;
        }
        return minutes.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal parseDecimal(String value, BigDecimal fallback) {
        BigDecimal parsed = parseDecimalOrNull(value);
        return parsed != null ? parsed : fallback;
    }

    private static BigDecimal parseDecimalOrNull(String value) {
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Long parseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Boolean parseBoolean(String value) {
        String normalized = value.trim().toLowerCase();
        return switch (normalized) {
            case "true", "yes", "y", "1" -> true;
            case "false", "no", "n", "0" -> false;
            default -> null;
        };
    }
}
