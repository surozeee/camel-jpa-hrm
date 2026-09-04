package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.postgres.company.AttendanceEntity;
import com.jojolaptech.camel.model.postgres.company.AttendanceLogEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceApprovalStatusEnum;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceCalculationModeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceSourceEnum;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceStatusEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LogTypeEnum;
import com.jojolaptech.camel.repository.postgres.company.PgAttendanceLogRepository;
import com.jojolaptech.camel.repository.postgres.company.PgAttendanceRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import org.slf4j.Logger;

/**
 * Shared residual punch import for deviceLogs / tempDeviceLogs into {@code hrm_attendance_log}.
 * AttLogs (and earlier residual steps) win on enrollId+logDateTime collisions.
 */
final class ResidualDeviceAttendanceLogMigrator {

    private ResidualDeviceAttendanceLogMigrator() {
    }

    record PunchSource(
            long sourceId,
            Integer enrollId,
            LocalDateTime logDateTime,
            String checkType,
            String macId,
            Integer sensorId,
            Integer verifyCode,
            Integer workCode) {
    }

    static <T> int migrate(
            List<T> batch,
            ToLongFunction<T> idFn,
            Function<T, Integer> enrollFn,
            Function<T, java.util.Date> checkTimeFn,
            Function<T, String> checkTypeFn,
            Function<T, String> macFn,
            Function<T, Integer> sensorFn,
            Function<T, Integer> verifyFn,
            Function<T, Integer> workFn,
            Function<Long, Long> mysqlIdFn,
            String remarks,
            String sourceLabel,
            PgEmployeeRepository employeeRepository,
            PgAttendanceRepository attendanceRepository,
            PgAttendanceLogRepository attendanceLogRepository,
            Logger log) {

        List<PunchSource> punches = new ArrayList<>();
        for (T row : batch) {
            long sourceId = idFn.applyAsLong(row);
            Integer enrollId = enrollFn.apply(row);
            java.util.Date checkTime = checkTimeFn.apply(row);
            if (enrollId == null) {
                log.warn("Skipping {} id={}, enrollId null", sourceLabel, sourceId);
                continue;
            }
            if (checkTime == null) {
                log.warn("Skipping {} id={}, checkTime null", sourceLabel, sourceId);
                continue;
            }
            LocalDateTime logDateTime = AttendancePunchMigrationMapper.toLocalDateTime(checkTime);
            punches.add(new PunchSource(
                    sourceId,
                    enrollId,
                    logDateTime,
                    checkTypeFn.apply(row),
                    macFn.apply(row),
                    sensorFn.apply(row),
                    verifyFn.apply(row),
                    workFn.apply(row)));
        }
        if (punches.isEmpty()) {
            return 0;
        }

        Set<Long> offsetMysqlIds = punches.stream()
                .map(p -> mysqlIdFn.apply(p.sourceId()))
                .collect(Collectors.toSet());
        Set<Long> existingMysqlIds = new HashSet<>(attendanceLogRepository.findMysqlIdsByMysqlIdIn(offsetMysqlIds));

        Set<String> enrollIds = punches.stream()
                .map(p -> String.valueOf(p.enrollId()))
                .collect(Collectors.toSet());
        Set<LocalDateTime> dateTimes = punches.stream()
                .map(PunchSource::logDateTime)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> existingEnrollDateTimes = new HashSet<>();
        if (!enrollIds.isEmpty() && !dateTimes.isEmpty()) {
            for (AttendanceLogEntity existing :
                    attendanceLogRepository.findByEnrollIdInAndLogDateTimeIn(enrollIds, dateTimes)) {
                if (existing.getEnrollId() != null && existing.getLogDateTime() != null) {
                    existingEnrollDateTimes.add(AttendancePunchMigrationMapper.enrollDateTimeKey(
                            existing.getEnrollId(), existing.getLogDateTime()));
                }
            }
        }

        Map<String, List<EmployeeEntity>> employeesByEnroll = enrollIds.isEmpty()
                ? Map.of()
                : employeeRepository.findByEnrollIdIn(enrollIds).stream()
                        .collect(Collectors.groupingBy(EmployeeEntity::getEnrollId));

        List<AttendanceLogEntity> logsToSave = new ArrayList<>();
        Map<String, AttendanceEntity> attendanceByKey = new HashMap<>();
        Map<String, LocalTime> minPunchByKey = new HashMap<>();
        Map<String, LocalTime> maxPunchByKey = new HashMap<>();
        List<AttendanceEntity> newAttendances = new ArrayList<>();

        for (PunchSource source : punches) {
            long mysqlId = mysqlIdFn.apply(source.sourceId());
            if (existingMysqlIds.contains(mysqlId)) {
                continue;
            }
            String enrollKey = String.valueOf(source.enrollId());
            String pairKey = AttendancePunchMigrationMapper.enrollDateTimeKey(enrollKey, source.logDateTime());
            if (existingEnrollDateTimes.contains(pairKey)) {
                continue;
            }

            EmployeeEntity employee = resolveEmployee(employeesByEnroll.getOrDefault(enrollKey, List.of()));
            if (employee == null) {
                log.warn(
                        "Skipping {} id={}, no migrated employee for enrollId={}",
                        sourceLabel,
                        source.sourceId(),
                        enrollKey);
                continue;
            }

            LocalDate attendanceDate = source.logDateTime().toLocalDate();
            LocalTime punchTime = source.logDateTime().toLocalTime();
            String attendanceKey = employee.getId() + ":" + attendanceDate;

            AttendanceEntity attendance = attendanceByKey.get(attendanceKey);
            if (attendance == null) {
                attendance = attendanceRepository
                        .findByEmployeeIdAndAttendanceDate(employee.getId(), attendanceDate)
                        .orElse(null);
                if (attendance == null) {
                    attendance = AttendanceEntity.builder()
                            .employeeId(employee.getId())
                            .attendanceDate(attendanceDate)
                            .source(AttendanceSourceEnum.DEVICE)
                            .approvalStatus(AttendanceApprovalStatusEnum.AUTO_APPROVED)
                            .calculationMode(AttendanceCalculationModeEnum.NORMAL)
                            .attendanceStatus(AttendanceStatusEnum.PRESENT)
                            .deviceMacAddress(source.macId())
                            .build();
                    newAttendances.add(attendance);
                }
                attendanceByKey.put(attendanceKey, attendance);
            }

            LogTypeEnum logType = AttendancePunchMigrationMapper.mapCheckType(source.checkType());
            logsToSave.add(AttendanceLogEntity.builder()
                    .mysqlId(mysqlId)
                    .employeeId(employee.getId())
                    .enrollId(enrollKey)
                    .logDateTime(source.logDateTime())
                    .logType(logType)
                    .deviceInfo(AttendancePunchMigrationMapper.buildDeviceLogInfo(
                            source.macId(), source.sensorId(), source.verifyCode(), source.workCode()))
                    .remarks(remarks)
                    .attendance(attendance)
                    .consumed(false)
                    .duplicate(false)
                    .outlier(false)
                    .build());
            existingMysqlIds.add(mysqlId);
            existingEnrollDateTimes.add(pairKey);

            minPunchByKey.merge(attendanceKey, punchTime, (a, b) -> a.isBefore(b) ? a : b);
            maxPunchByKey.merge(attendanceKey, punchTime, (a, b) -> a.isAfter(b) ? a : b);
        }

        if (!newAttendances.isEmpty()) {
            attendanceRepository.saveAll(newAttendances);
        }
        if (!logsToSave.isEmpty()) {
            attendanceLogRepository.saveAll(logsToSave);
        }

        List<AttendanceEntity> toUpdate = new ArrayList<>();
        for (Map.Entry<String, AttendanceEntity> entry : attendanceByKey.entrySet()) {
            AttendanceEntity attendance = entry.getValue();
            LocalTime min = minPunchByKey.get(entry.getKey());
            LocalTime max = maxPunchByKey.get(entry.getKey());
            boolean changed = false;
            if (min != null && (attendance.getCheckInTime() == null || min.isBefore(attendance.getCheckInTime()))) {
                attendance.setCheckInTime(min);
                changed = true;
            }
            if (max != null
                    && (attendance.getCheckOutTime() == null || max.isAfter(attendance.getCheckOutTime()))
                    && (attendance.getCheckInTime() == null || !max.equals(attendance.getCheckInTime()))) {
                attendance.setCheckOutTime(max);
                changed = true;
            }
            if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() == null) {
                attendance.setAttendanceStatus(AttendanceStatusEnum.MISSING_CHECK_OUT);
                changed = true;
            } else if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
                if (attendance.getAttendanceStatus() == null
                        || attendance.getAttendanceStatus() == AttendanceStatusEnum.MISSING_CHECK_OUT) {
                    attendance.setAttendanceStatus(AttendanceStatusEnum.PRESENT);
                    changed = true;
                }
            }
            if (changed) {
                toUpdate.add(attendance);
            }
        }
        if (!toUpdate.isEmpty()) {
            attendanceRepository.saveAll(toUpdate);
        }

        return logsToSave.size();
    }

    private static EmployeeEntity resolveEmployee(List<EmployeeEntity> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        return candidates.stream()
                .sorted((a, b) -> Long.compare(
                        a.getMysqlId() == null ? Long.MAX_VALUE : a.getMysqlId(),
                        b.getMysqlId() == null ? Long.MAX_VALUE : b.getMysqlId()))
                .findFirst()
                .orElse(candidates.get(0));
    }
}
