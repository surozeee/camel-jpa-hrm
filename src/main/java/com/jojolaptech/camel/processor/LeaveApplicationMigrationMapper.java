package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.enums.LeaveAdjustmentType;
import com.jojolaptech.camel.model.mysql.enums.LeaveType;
import com.jojolaptech.camel.model.mysql.enums.RequestStatus;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveDurationEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveOpeningAdjustmentActionEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveStatusEnum;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

final class LeaveApplicationMigrationMapper {

    private LeaveApplicationMigrationMapper() {
    }

    static LeaveDurationEnum mapDuration(LeaveType leaveType) {
        if (leaveType == null) {
            return LeaveDurationEnum.FULL_DAY;
        }
        return switch (leaveType) {
            case Full -> LeaveDurationEnum.FULL_DAY;
            case Half, FirstHalf -> LeaveDurationEnum.FIRST_HALF;
            case SecondHalf -> LeaveDurationEnum.SECOND_HALF;
        };
    }

    static LeaveStatusEnum mapApplicationStatus(String status, boolean hadRecommendation) {
        if (status == null || status.isBlank()) {
            return LeaveStatusEnum.PENDING_SUPERVISOR_REVIEW;
        }
        String normalized = status.toLowerCase(Locale.ROOT);
        if (normalized.contains("cancel")) {
            return LeaveStatusEnum.CANCELLED;
        }
        if (normalized.contains("approve")) {
            return LeaveStatusEnum.APPROVED;
        }
        if (normalized.contains("deny") || normalized.contains("reject") || normalized.contains("discard")) {
            return hadRecommendation
                    ? LeaveStatusEnum.REJECTED_BY_FINAL_APPROVER
                    : LeaveStatusEnum.REJECTED_BY_SUPERVISOR;
        }
        if (normalized.contains("recommend")) {
            return LeaveStatusEnum.SUPERVISOR_RECOMMENDED;
        }
        if (normalized.contains("notchecked")
                || normalized.contains("not checked")
                || normalized.contains("pending")) {
            return LeaveStatusEnum.PENDING_SUPERVISOR_REVIEW;
        }
        return LeaveStatusEnum.PENDING_SUPERVISOR_REVIEW;
    }

    /**
     * @return mapped status, or null when cancellation should not change leave status (Denied).
     */
    static LeaveStatusEnum mapCancellationStatus(RequestStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case Approved -> LeaveStatusEnum.CANCELLED;
            case NotChecked, Recommended -> LeaveStatusEnum.WITHDRAWAL_REQUESTED;
            case Denied -> null;
        };
    }

    static LeaveOpeningAdjustmentActionEnum mapAdjustmentAction(LeaveAdjustmentType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case Add -> LeaveOpeningAdjustmentActionEnum.ADD;
            case Deduct -> LeaveOpeningAdjustmentActionEnum.DEDUCT;
        };
    }

    static String truncate(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() <= maxLen) {
            return trimmed;
        }
        return trimmed.substring(0, maxLen);
    }

    static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    static double coalesce(Double value, double fallback) {
        return value != null ? value : fallback;
    }

    static double maxZero(double value) {
        return Math.max(0.0, value);
    }
}
