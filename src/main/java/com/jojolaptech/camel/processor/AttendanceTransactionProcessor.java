package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.AttendanceTransaction;
import com.jojolaptech.camel.model.postgres.company.AttendanceEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceApprovalStatusEnum;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceCalculationModeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceSourceEnum;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceStatusEnum;
import com.jojolaptech.camel.repository.postgres.company.PgAttendanceRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceTransactionProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(AttendanceTransactionProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgAttendanceRepository attendanceRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<AttendanceTransaction> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> txMysqlIds = batch.stream().map(AttendanceTransaction::getId).collect(Collectors.toSet());
        Set<Long> existingByMysqlId = new HashSet<>(attendanceRepository.findMysqlIdsByMysqlIdIn(txMysqlIds));

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<UUID> employeeIds = employeeByMysqlId.values().stream()
                .map(EmployeeEntity::getId)
                .collect(Collectors.toSet());
        Set<LocalDate> dates = batch.stream()
                .map(AttendanceTransaction::getLogDate)
                .filter(Objects::nonNull)
                .map(date -> Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate())
                .collect(Collectors.toSet());
        Map<String, AttendanceEntity> existingByEmployeeDate = new HashMap<>();
        if (!employeeIds.isEmpty() && !dates.isEmpty()) {
            for (AttendanceEntity row :
                    attendanceRepository.findByEmployeeIdInAndAttendanceDateIn(employeeIds, dates)) {
                existingByEmployeeDate.put(row.getEmployeeId() + ":" + row.getAttendanceDate(), row);
            }
        }

        List<AttendanceEntity> toSave = new ArrayList<>();
        Set<Long> pendingMysqlIds = new HashSet<>(existingByMysqlId);
        int imported = 0;

        for (AttendanceTransaction source : batch) {
            if (pendingMysqlIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployee() == null || source.getLogDate() == null) {
                log.warn("Skipping attendanceTransaction id={}, missing employee/logDate", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping attendanceTransaction id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }

            LocalDate attendanceDate = Instant.ofEpochMilli(source.getLogDate().getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalTime checkIn = AttendancePunchMigrationMapper.parseLocalTime(source.getLogInTime());
            LocalTime checkOut = AttendancePunchMigrationMapper.parseLocalTime(source.getLogOutTime());
            Integer overtimeMinutes = AttendancePunchMigrationMapper.parseOvertimeMinutes(source.getOverTime());
            AttendanceStatusEnum status =
                    AttendancePunchMigrationMapper.resolveTransactionStatus(checkIn, checkOut, source.getLateMinutes());

            String key = employee.getId() + ":" + attendanceDate;
            AttendanceEntity entity = existingByEmployeeDate.get(key);
            if (entity == null) {
                entity = AttendanceEntity.builder()
                        .mysqlId(source.getId())
                        .employeeId(employee.getId())
                        .attendanceDate(attendanceDate)
                        .checkInTime(checkIn)
                        .checkOutTime(checkOut)
                        .attendanceStatus(status)
                        .source(AttendanceSourceEnum.DEVICE)
                        .approvalStatus(AttendanceApprovalStatusEnum.AUTO_APPROVED)
                        .calculationMode(AttendanceCalculationModeEnum.NORMAL)
                        .remarks(blankToNull(source.getRemarks()))
                        .overtimeOverrideMinutes(overtimeMinutes)
                        .overtimeManuallyEdited(overtimeMinutes != null)
                        .build();
                existingByEmployeeDate.put(key, entity);
            } else {
                if (entity.getMysqlId() == null) {
                    entity.setMysqlId(source.getId());
                }
                entity.setCheckInTime(checkIn != null ? checkIn : entity.getCheckInTime());
                entity.setCheckOutTime(checkOut != null ? checkOut : entity.getCheckOutTime());
                entity.setAttendanceStatus(status);
                entity.setSource(AttendanceSourceEnum.DEVICE);
                entity.setApprovalStatus(AttendanceApprovalStatusEnum.AUTO_APPROVED);
                entity.setCalculationMode(AttendanceCalculationModeEnum.NORMAL);
                if (blankToNull(source.getRemarks()) != null) {
                    entity.setRemarks(source.getRemarks().trim());
                }
                if (overtimeMinutes != null) {
                    entity.setOvertimeOverrideMinutes(overtimeMinutes);
                    entity.setOvertimeManuallyEdited(true);
                }
            }
            toSave.add(entity);
            pendingMysqlIds.add(source.getId());
            imported++;
        }

        if (!toSave.isEmpty()) {
            attendanceRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", imported);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
