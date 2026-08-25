package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.AutoLeaveAccParams;
import com.jojolaptech.camel.model.postgres.company.enums.AccumulationPeriodEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveAccumulationUnitEnum;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import lombok.Getter;

final class AutoLeaveAccMigrationMapper {

    private AutoLeaveAccMigrationMapper() {
    }

    record ConfigBundleKey(long companyMysqlId, long paramDateEpochMs, long leaveMysqlId) {
        static ConfigBundleKey from(List<AutoLeaveAccParams> params) {
            AutoLeaveAccParams sample = params.getFirst();
            return new ConfigBundleKey(
                    sample.getCompany().getId(),
                    sample.getParamDate().getTime(),
                    resolveLeaveMysqlId(params));
        }
    }

    @Getter
    static final class AccValues {
        Boolean accumulationEnabled = true;
        LeaveAccumulationUnitEnum unit = LeaveAccumulationUnitEnum.DAYS;
        BigDecimal leaveDays = BigDecimal.ONE;
        AccumulationPeriodEnum accumulationPeriod = AccumulationPeriodEnum.MONTH;
        Boolean requireConfirmation;
        LocalDate effectiveFrom;
        String calendarType;
        Integer openingYear;
        Integer openingMonth;
        String description;
    }

    static AccValues fromParams(List<AutoLeaveAccParams> params) {
        AccValues values = new AccValues();
        for (AutoLeaveAccParams param : params) {
            if (Boolean.TRUE.equals(param.getAccType())) {
                values.unit = LeaveAccumulationUnitEnum.HOURS;
            }
            if (Boolean.FALSE.equals(param.getIsActive()) || Boolean.TRUE.equals(param.getIsDeleted())) {
                values.accumulationEnabled = false;
            }
            applyParam(values, param.getParamName(), param.getParamValue());
        }
        values.effectiveFrom = resolveEffectiveFrom(values, params);
        return values;
    }

    static long resolveLeaveMysqlId(List<AutoLeaveAccParams> params) {
        for (AutoLeaveAccParams param : params) {
            if (param.getLeave() != null && param.getLeave().getId() != null && param.getLeave().getId() > 0) {
                return param.getLeave().getId();
            }
        }
        for (AutoLeaveAccParams param : params) {
            if ("LeaveId".equalsIgnoreCase(param.getParamName())) {
                Long parsed = parseLongOrNull(param.getParamValue());
                if (parsed != null && parsed > 0) {
                    return parsed;
                }
            }
        }
        return 0L;
    }

    static long bundleMysqlId(List<AutoLeaveAccParams> params) {
        return params.stream().mapToLong(AutoLeaveAccParams::getId).min().orElse(0L);
    }

    static long ruleMysqlId(long bundleMysqlId, long branchMysqlId) {
        return bundleMysqlId * 1_000_000L + branchMysqlId;
    }

    private static void applyParam(AccValues values, String paramName, String paramValue) {
        if (paramName == null || paramValue == null || paramValue.isBlank()) {
            return;
        }
        switch (paramName.trim()) {
            case "DaysToAdd" -> {
                values.unit = LeaveAccumulationUnitEnum.DAYS;
                values.leaveDays = parseDecimal(paramValue, values.leaveDays);
            }
            case "MinutesToAdd" -> {
                values.unit = LeaveAccumulationUnitEnum.HOURS;
                values.leaveDays = minutesToHours(paramValue, values.leaveDays);
            }
            case "LeaveId" -> {
                // leave is resolved separately from the bundle
            }
            case "isEditable" -> values.requireConfirmation = !Boolean.TRUE.equals(parseBoolean(paramValue));
            case "DateType" -> values.calendarType = paramValue.trim().toLowerCase();
            case "OpeningYear" -> values.openingYear = parseInt(paramValue);
            case "OpeningMonth" -> values.openingMonth = parseInt(paramValue);
            default -> applyNormalizedParam(values, paramName, paramValue);
        }
    }

    private static void applyNormalizedParam(AccValues values, String paramName, String paramValue) {
        switch (FiscalMigrationMapper.normalizeParamName(paramName)) {
            case "days_to_add", "leave_days", "days", "accumulation_days" -> {
                values.unit = LeaveAccumulationUnitEnum.DAYS;
                values.leaveDays = parseDecimal(paramValue, values.leaveDays);
            }
            case "minutes_to_add" -> {
                values.unit = LeaveAccumulationUnitEnum.HOURS;
                values.leaveDays = minutesToHours(paramValue, values.leaveDays);
            }
            case "leave_id" -> {
                // resolved separately
            }
            case "is_editable" -> values.requireConfirmation = !Boolean.TRUE.equals(parseBoolean(paramValue));
            case "date_type" -> values.calendarType = paramValue.trim().toLowerCase();
            case "opening_year" -> values.openingYear = parseInt(paramValue);
            case "opening_month" -> values.openingMonth = parseInt(paramValue);
            default -> {
                // ignore unknown legacy params
            }
        }
    }

    private static LocalDate resolveEffectiveFrom(AccValues values, List<AutoLeaveAccParams> params) {
        if (values.openingYear != null && values.openingMonth != null && values.openingMonth >= 1) {
            if ("e".equalsIgnoreCase(values.calendarType)) {
                int month = Math.min(values.openingMonth, 12);
                int year = values.openingYear;
                if (year >= 1900 && year <= 2100) {
                    return LocalDate.of(year, month, 1);
                }
            } else if ("n".equalsIgnoreCase(values.calendarType)) {
                values.description = appendDescription(
                        values.description,
                        "Opening BS year=%d month=%d".formatted(values.openingYear, values.openingMonth));
            }
        }
        Date paramDate = params.getFirst().getParamDate();
        return paramDate != null ? AttendanceMigrationMapper.toLocalDate(paramDate) : null;
    }

    private static String appendDescription(String existing, String addition) {
        if (existing == null || existing.isBlank()) {
            return addition;
        }
        return existing + "; " + addition;
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

    private static Integer parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Long parseLongOrNull(String value) {
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
