package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.AttTimeTable;
import com.jojolaptech.camel.model.postgres.company.BranchShiftEntity;
import com.jojolaptech.camel.model.postgres.company.enums.AccumulationPeriodEnum;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

final class AttendanceMigrationMapper {

    private AttendanceMigrationMapper() {
    }

    static String shiftKey(Long timeTableMysqlId, Long branchMysqlId) {
        return timeTableMysqlId + ":" + branchMysqlId;
    }

    static String shiftCode(Long timeTableMysqlId, Long branchMysqlId) {
        return "TT-" + timeTableMysqlId + "-B-" + branchMysqlId;
    }

    static BranchShiftEntity toBranchShift(
            AttTimeTable source, com.jojolaptech.camel.model.postgres.company.BranchEntity branch) {
        LocalTime start = toLocalTime(firstTime(source.getOnTime(), source.getStartIn()), LocalTime.of(9, 0));
        LocalTime end = toLocalTime(firstTime(source.getOffTime(), source.getStopOut()), LocalTime.of(17, 0));
        int workingHours = Math.max(1, end.getHour() - start.getHour());

        return BranchShiftEntity.builder()
                .mysqlId(source.getId())
                .mysqlBranchId(branch.getMysqlId())
                .branchId(branch.getId())
                .name(FiscalMigrationMapper.trimToNull(source.getName()))
                .code(shiftCode(source.getId(), branch.getMysqlId()))
                .startTime(start)
                .endTime(end)
                .workingHours(workingHours)
                .isFlexible(false)
                .isNightShift(end.isBefore(start))
                .description("Migrated from attTimeTable id=" + source.getId())
                .build();
    }

    /**
     * Legacy HRM {@code attShiftDetails.days} uses {@link java.util.Calendar} numbering (1 = Sunday).
     */
    static DayOfWeek legacyDayOfWeek(int days) {
        return switch (days) {
            case 1 -> DayOfWeek.SUNDAY;
            case 2 -> DayOfWeek.MONDAY;
            case 3 -> DayOfWeek.TUESDAY;
            case 4 -> DayOfWeek.WEDNESDAY;
            case 5 -> DayOfWeek.THURSDAY;
            case 6 -> DayOfWeek.FRIDAY;
            case 7 -> DayOfWeek.SATURDAY;
            default -> DayOfWeek.MONDAY;
        };
    }

    static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    static AccumulationPeriodEnum accumulationPeriod(String paramName) {
        if (paramName == null) {
            return AccumulationPeriodEnum.MONTH;
        }
        return switch (FiscalMigrationMapper.normalizeParamName(paramName)) {
            case "daily", "day" -> AccumulationPeriodEnum.DAILY;
            case "week", "weekly" -> AccumulationPeriodEnum.WEEK;
            case "bi_weekly", "biweekly", "fortnight" -> AccumulationPeriodEnum.BI_WEEKLY;
            case "quarter", "quarterly" -> AccumulationPeriodEnum.QUARTER;
            case "semi_annually", "semiannually", "halfyearly" -> AccumulationPeriodEnum.SEMI_ANNUALLY;
            case "year", "yearly", "annual" -> AccumulationPeriodEnum.YEARLY;
            default -> AccumulationPeriodEnum.MONTH;
        };
    }

    private static Time firstTime(Time primary, Time fallback) {
        return primary != null ? primary : fallback;
    }

    private static LocalTime toLocalTime(Time time, LocalTime defaultTime) {
        if (time == null) {
            return defaultTime;
        }
        return time.toLocalTime();
    }
}
