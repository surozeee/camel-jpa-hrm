package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.enums.Type;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceStatusEnum;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceTimeRequestStatusEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LogTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.RosterShiftSlotEnum;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class AttendancePunchMigrationMapper {

    public static final long DEVICE_LOG_MYSQL_OFFSET = 12_000_000_000_000L;
    public static final long TEMP_DEVICE_LOG_MYSQL_OFFSET = 13_000_000_000_000L;
    public static final long OLD_ATTENDANCE_MYSQL_OFFSET = 14_000_000_000_000L;
    public static final long WORK_SHIFT_MYSQL_OFFSET = 15_000_000_000_000L;

    private static final DateTimeFormatter[] TIME_FORMATS = {
        DateTimeFormatter.ofPattern("H:mm:ss"),
        DateTimeFormatter.ofPattern("HH:mm:ss"),
        DateTimeFormatter.ofPattern("H:mm"),
        DateTimeFormatter.ofPattern("HH:mm"),
        DateTimeFormatter.ofPattern("HHmmss"),
        DateTimeFormatter.ofPattern("HHmm")
    };

    private AttendancePunchMigrationMapper() {
    }

    public static long deviceLogMysqlId(long sourceId) {
        return DEVICE_LOG_MYSQL_OFFSET + sourceId;
    }

    public static long tempDeviceLogMysqlId(long sourceId) {
        return TEMP_DEVICE_LOG_MYSQL_OFFSET + sourceId;
    }

    public static long oldAttendanceMysqlId(long sourceId) {
        return OLD_ATTENDANCE_MYSQL_OFFSET + sourceId;
    }

    public static long workShiftMysqlId(long workShiftId) {
        return WORK_SHIFT_MYSQL_OFFSET + workShiftId;
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalTime toLocalTimeFromDate(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalTime();
    }

    public static String buildDeviceLogInfo(String macId, Integer sensorId, Integer verifyCode, Integer workCode) {
        List<String> parts = new ArrayList<>();
        if (macId != null && !macId.isBlank()) {
            parts.add("mac=" + macId.trim());
        }
        if (sensorId != null) {
            parts.add("sensor=" + sensorId);
        }
        if (verifyCode != null) {
            parts.add("verify=" + verifyCode);
        }
        if (workCode != null) {
            parts.add("work=" + workCode);
        }
        return parts.isEmpty() ? null : String.join("; ", parts);
    }

    public static String enrollDateTimeKey(String enrollId, LocalDateTime logDateTime) {
        return enrollId + "|" + logDateTime;
    }

    public static RosterShiftSlotEnum mapWorkShiftName(String shiftName) {
        if (shiftName == null || shiftName.isBlank()) {
            return RosterShiftSlotEnum.DAY;
        }
        String normalized = shiftName.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("morning") || containsToken(normalized, "am")) {
            return RosterShiftSlotEnum.MORNING;
        }
        if (normalized.contains("evening") || containsToken(normalized, "pm")) {
            return RosterShiftSlotEnum.EVENING;
        }
        if (normalized.contains("night")) {
            return RosterShiftSlotEnum.NIGHT;
        }
        if (normalized.contains("off") || normalized.contains("holiday")) {
            return RosterShiftSlotEnum.OFF;
        }
        return RosterShiftSlotEnum.DAY;
    }

    private static boolean containsToken(String haystack, String token) {
        if (haystack.equals(token)) {
            return true;
        }
        return haystack.startsWith(token + " ")
                || haystack.endsWith(" " + token)
                || haystack.contains(" " + token + " ")
                || haystack.contains(token + "/")
                || haystack.contains("/" + token);
    }

    public static LocalTime parseLocalTime(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.isEmpty() || "-".equals(value) || "null".equalsIgnoreCase(value)) {
            return null;
        }
        for (DateTimeFormatter formatter : TIME_FORMATS) {
            try {
                return LocalTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // try next
            }
        }
        return null;
    }

    public static Integer parseOvertimeMinutes(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.isEmpty() || "-".equals(value) || "0".equals(value) || "00:00".equals(value)) {
            return null;
        }
        if (value.contains(":")) {
            String[] parts = value.split(":");
            try {
                int hours = Integer.parseInt(parts[0].trim());
                int minutes = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
                int total = hours * 60 + minutes;
                return total == 0 ? null : total;
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        try {
            int minutes = Integer.parseInt(value);
            return minutes == 0 ? null : minutes;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public static LogTypeEnum mapCheckType(String checkType) {
        if (checkType == null || checkType.isBlank()) {
            return LogTypeEnum.CHECK_IN;
        }
        String normalized = checkType.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "O", "OUT", "1", "CHECKOUT", "CHECK_OUT", "CHECK OUT" -> LogTypeEnum.CHECK_OUT;
            case "B", "BREAK", "BREAK_START", "BREAK START", "2" -> LogTypeEnum.BREAK_START;
            case "E", "BREAK_END", "BREAK END", "3" -> LogTypeEnum.BREAK_END;
            default -> LogTypeEnum.CHECK_IN;
        };
    }

    public static AttendanceTimeRequestStatusEnum mapForgotStatus(String status) {
        if (status == null || status.isBlank()) {
            return AttendanceTimeRequestStatusEnum.PENDING_SUPERVISOR_REVIEW;
        }
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("approve")) {
            return AttendanceTimeRequestStatusEnum.APPROVED;
        }
        if (normalized.contains("reject")) {
            return AttendanceTimeRequestStatusEnum.REJECTED;
        }
        if (normalized.contains("cancel")) {
            return AttendanceTimeRequestStatusEnum.CANCELLED;
        }
        if (normalized.contains("appl")) {
            return AttendanceTimeRequestStatusEnum.APPLIED;
        }
        return AttendanceTimeRequestStatusEnum.PENDING_SUPERVISOR_REVIEW;
    }

    public static AttendanceStatusEnum resolveTransactionStatus(
            LocalTime checkIn, LocalTime checkOut, String lateMinutes) {
        if (checkIn != null && checkOut == null) {
            return AttendanceStatusEnum.MISSING_CHECK_OUT;
        }
        if (checkIn == null && checkOut != null) {
            return AttendanceStatusEnum.MISSING_CHECK_IN;
        }
        Integer late = parseOvertimeMinutes(lateMinutes);
        if (late == null) {
            late = parsePositiveInt(lateMinutes);
        }
        if (late != null && late > 0) {
            return AttendanceStatusEnum.LATE;
        }
        return AttendanceStatusEnum.PRESENT;
    }

    public static String combineReason(String reason, String remark) {
        String r = reason == null ? "" : reason.trim();
        String m = remark == null ? "" : remark.trim();
        if (r.isEmpty() && m.isEmpty()) {
            return "Migrated from attendanceForgot";
        }
        if (r.isEmpty()) {
            return m;
        }
        if (m.isEmpty()) {
            return r;
        }
        return r + " | " + m;
    }

    public static boolean isCheckInType(Type type) {
        return type == null || type == Type.In;
    }

    private static Integer parsePositiveInt(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
