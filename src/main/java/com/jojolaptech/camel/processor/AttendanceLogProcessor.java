package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.AttLogs;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceLogProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(AttendanceLogProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgAttendanceRepository attendanceRepository;
    private final PgAttendanceLogRepository attendanceLogRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<AttLogs> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> logMysqlIds = batch.stream().map(AttLogs::getId).collect(Collectors.toSet());
        Set<Long> existingLogIds = new HashSet<>(attendanceLogRepository.findMysqlIdsByMysqlIdIn(logMysqlIds));

        Set<String> enrollIds = batch.stream()
                .map(AttLogs::getEnrollId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.toSet());
        Map<String, List<EmployeeEntity>> employeesByEnroll = enrollIds.isEmpty()
                ? Map.of()
                : employeeRepository.findByEnrollIdIn(enrollIds).stream()
                        .collect(Collectors.groupingBy(EmployeeEntity::getEnrollId));

        List<AttendanceLogEntity> logsToSave = new ArrayList<>();
        Map<String, AttendanceEntity> attendanceByKey = new HashMap<>();
        Map<String, LocalTime> minPunchByKey = new HashMap<>();
        Map<String, LocalTime> maxPunchByKey = new HashMap<>();
        List<AttendanceEntity> newAttendances = new ArrayList<>();

        for (AttLogs source : batch) {
            if (existingLogIds.contains(source.getId()) || Boolean.TRUE.equals(source.getIsDeleted())) {
                continue;
            }
            if (source.getEnrollId() == null) {
                log.warn("Skipping attLogs id={}, enrollId null", source.getId());
                continue;
            }
            if (source.getCheckTime() == null) {
                log.warn("Skipping attLogs id={}, checkTime null", source.getId());
                continue;
            }

            String enrollKey = String.valueOf(source.getEnrollId());
            EmployeeEntity employee = resolveEmployee(employeesByEnroll.getOrDefault(enrollKey, List.of()));
            if (employee == null) {
                log.warn(
                        "Skipping attLogs id={}, no migrated employee for enrollId={}",
                        source.getId(),
                        enrollKey);
                continue;
            }

            LocalDateTime logDateTime = Instant.ofEpochMilli(source.getCheckTime().getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            LocalDate attendanceDate = logDateTime.toLocalDate();
            LocalTime punchTime = logDateTime.toLocalTime();
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
                            .deviceMacAddress(source.getMacId())
                            .build();
                    newAttendances.add(attendance);
                }
                attendanceByKey.put(attendanceKey, attendance);
            }

            LogTypeEnum logType = AttendancePunchMigrationMapper.mapCheckType(source.getCheckType());
            logsToSave.add(AttendanceLogEntity.builder()
                    .mysqlId(source.getId())
                    .employeeId(employee.getId())
                    .enrollId(enrollKey)
                    .logDateTime(logDateTime)
                    .logType(logType)
                    .deviceInfo(buildDeviceInfo(source))
                    .attendance(attendance)
                    .consumed(false)
                    .duplicate(false)
                    .outlier(false)
                    .build());
            existingLogIds.add(source.getId());

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

        exchange.setProperty("batchImported", logsToSave.size());
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

    private static String buildDeviceInfo(AttLogs source) {
        List<String> parts = new ArrayList<>();
        if (source.getMacId() != null && !source.getMacId().isBlank()) {
            parts.add("mac=" + source.getMacId());
        }
        if (source.getIp() != null && !source.getIp().isBlank()) {
            parts.add("ip=" + source.getIp());
        }
        if (source.getBrowserName() != null && !source.getBrowserName().isBlank()) {
            parts.add("browser=" + source.getBrowserName());
        }
        if (source.getOs() != null && !source.getOs().isBlank()) {
            parts.add("os=" + source.getOs());
        }
        return parts.isEmpty() ? null : String.join("; ", parts);
    }
}
