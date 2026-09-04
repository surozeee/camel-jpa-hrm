package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.OldAttendanceTransaction;
import com.jojolaptech.camel.model.postgres.company.AttendanceEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceApprovalStatusEnum;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceCalculationModeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceSourceEnum;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceStatusEnum;
import com.jojolaptech.camel.repository.postgres.company.PgAttendanceRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import java.time.LocalDate;
import java.time.LocalTime;
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
public class OldAttendanceTransactionProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(OldAttendanceTransactionProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgAttendanceRepository attendanceRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<OldAttendanceTransaction> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> offsetMysqlIds = batch.stream()
                .map(row -> AttendancePunchMigrationMapper.oldAttendanceMysqlId(row.getId()))
                .collect(Collectors.toSet());
        Set<Long> existingByMysqlId = new HashSet<>(attendanceRepository.findMysqlIdsByMysqlIdIn(offsetMysqlIds));

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
                .map(OldAttendanceTransaction::getLogDate)
                .filter(Objects::nonNull)
                .map(date -> AttendancePunchMigrationMapper.toLocalDateTime(date).toLocalDate())
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

        for (OldAttendanceTransaction source : batch) {
            long mysqlId = AttendancePunchMigrationMapper.oldAttendanceMysqlId(source.getId());
            if (pendingMysqlIds.contains(mysqlId)) {
                continue;
            }
            if (source.getEmployee() == null || source.getLogDate() == null) {
                log.warn("Skipping oldAttendanceTransaction id={}, missing employee/logDate", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping oldAttendanceTransaction id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }

            LocalDate attendanceDate =
                    AttendancePunchMigrationMapper.toLocalDateTime(source.getLogDate()).toLocalDate();
            String key = employee.getId() + ":" + attendanceDate;
            if (existingByEmployeeDate.containsKey(key)) {
                // Fill-only: live 23i / punch day shells win — do not overwrite.
                continue;
            }

            LocalTime checkIn = AttendancePunchMigrationMapper.parseLocalTime(source.getLogInTime());
            LocalTime checkOut = AttendancePunchMigrationMapper.parseLocalTime(source.getLogOutTime());
            Integer overtimeMinutes = AttendancePunchMigrationMapper.parseOvertimeMinutes(source.getOverTime());
            AttendanceStatusEnum status =
                    AttendancePunchMigrationMapper.resolveTransactionStatus(checkIn, checkOut, source.getLateMinutes());

            AttendanceEntity entity = AttendanceEntity.builder()
                    .mysqlId(mysqlId)
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
            toSave.add(entity);
            pendingMysqlIds.add(mysqlId);
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
