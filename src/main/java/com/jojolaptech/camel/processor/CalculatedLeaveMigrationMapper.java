package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.postgres.company.enums.LeaveTypeEnum;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Date;
import java.util.Locale;

final class CalculatedLeaveMigrationMapper {

    static final long OT_RUN_OFFSET = 10_000_000_000_000L;
    static final long OT_LEAVE_CREDIT_OFFSET = 11_000_000_000_000L;

    private CalculatedLeaveMigrationMapper() {}

    record OtValueParts(BigDecimal leaveDays, int remainderMinutes, int otMinutes) {}

    static LeaveTypeEnum mapLeaveTypeEnum(String leaveName) {
        if (leaveName == null || leaveName.isBlank()) {
            return LeaveTypeEnum.OTHER;
        }
        String n = leaveName.toLowerCase(Locale.ROOT);
        if (n.contains("sick")) {
            return LeaveTypeEnum.SICK;
        }
        if (n.contains("casual")) {
            return LeaveTypeEnum.CASUAL;
        }
        if (n.contains("annual") || n.contains("earned") || n.contains("privilege")) {
            return LeaveTypeEnum.ANNUAL;
        }
        if (n.contains("maternity")) {
            return LeaveTypeEnum.MATERNITY;
        }
        if (n.contains("paternity")) {
            return LeaveTypeEnum.PATERNITY;
        }
        if (n.contains("compensat") || n.contains("floating") || n.contains("ot")) {
            return LeaveTypeEnum.COMPENSATORY;
        }
        if (n.contains("unpaid")) {
            return LeaveTypeEnum.UNPAID;
        }
        return LeaveTypeEnum.OTHER;
    }

    static LocalDate monthStart(Integer year, Integer month) {
        if (year == null || month == null || month < 1 || month > 12) {
            return null;
        }
        return LocalDate.of(year, month, 1);
    }

    static LocalDate monthEnd(Integer year, Integer month) {
        if (year == null || month == null || month < 1 || month > 12) {
            return null;
        }
        return YearMonth.of(year, month).atEndOfMonth();
    }

    static Integer accumulationMonth(Integer year, Integer month) {
        if (year == null || month == null) {
            return null;
        }
        return year * 100 + month;
    }

    static BigDecimal toBigDecimal(Double value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(value);
    }

    static String truncate(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen);
    }

    static LocalDate toLocalDate(Date date) {
        return LeaveApplicationMigrationMapper.toLocalDate(date);
    }

    static LocalDateTime toLocalDateTime(Date date) {
        return LeaveApplicationMigrationMapper.toLocalDateTime(date);
    }

    static long otRunMysqlId(long companyMysqlId, LocalDate tillDate) {
        int yyyymmdd = tillDate.getYear() * 10_000 + tillDate.getMonthValue() * 100 + tillDate.getDayOfMonth();
        return OT_RUN_OFFSET + companyMysqlId * 1_000_000L + yyyymmdd;
    }

    static long otLeaveCreditMysqlId(long otBalanceId) {
        return OT_LEAVE_CREDIT_OFFSET + otBalanceId;
    }

    static OtValueParts parseOtValue(String otValue, BigDecimal hoursEqToOneDay) {
        BigDecimal hoursEq = hoursEqToOneDay != null ? hoursEqToOneDay : BigDecimal.valueOf(8);
        if (otValue == null || otValue.isBlank()) {
            return new OtValueParts(BigDecimal.ZERO, 0, 0);
        }
        String trimmed = otValue.trim();
        BigDecimal days;
        BigDecimal hours = BigDecimal.ZERO;
        int colon = trimmed.indexOf(':');
        if (colon >= 0) {
            days = parseDecimal(trimmed.substring(0, colon), BigDecimal.ZERO);
            String hoursPart = trimmed.substring(colon + 1).trim();
            if (!hoursPart.isEmpty()) {
                hours = parseDecimal(hoursPart, BigDecimal.ZERO);
            }
        } else {
            days = parseDecimal(trimmed, BigDecimal.ZERO);
        }
        int remainderMinutes = hours.multiply(BigDecimal.valueOf(60)).setScale(0, RoundingMode.HALF_UP).intValue();
        int otMinutes = days.multiply(hoursEq)
                .multiply(BigDecimal.valueOf(60))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue()
                + remainderMinutes;
        return new OtValueParts(days, remainderMinutes, otMinutes);
    }

    static BigDecimal parseDecimal(String value, BigDecimal fallback) {
        try {
            return new BigDecimal(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    static String employeeDisplayName(String firstName, String middleName, String lastName) {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isBlank()) {
            sb.append(firstName.trim());
        }
        if (middleName != null && !middleName.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(middleName.trim());
        }
        if (lastName != null && !lastName.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(lastName.trim());
        }
        String name = sb.toString();
        return name.isEmpty() ? null : truncate(name, 255);
    }
}
