package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.AttendanceRemark;
import com.jojolaptech.camel.model.postgres.company.AttendanceEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgAttendanceRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
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
public class AttendanceRemarkProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(AttendanceRemarkProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgAttendanceRepository attendanceRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<AttendanceRemark> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

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
                .map(AttendanceRemark::getRemarkDate)
                .filter(Objects::nonNull)
                .map(date -> Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate())
                .collect(Collectors.toSet());

        Map<String, AttendanceEntity> attendanceByKey = new HashMap<>();
        if (!employeeIds.isEmpty() && !dates.isEmpty()) {
            for (AttendanceEntity row :
                    attendanceRepository.findByEmployeeIdInAndAttendanceDateIn(employeeIds, dates)) {
                attendanceByKey.put(row.getEmployeeId() + ":" + row.getAttendanceDate(), row);
            }
        }

        List<AttendanceEntity> toSave = new ArrayList<>();
        int imported = 0;
        for (AttendanceRemark source : batch) {
            if (source.getEmployee() == null || source.getRemarkDate() == null) {
                log.warn("Skipping attendanceRemark id={}, missing employee/remarkDate", source.getId());
                continue;
            }
            if (source.getRemark() == null || source.getRemark().isBlank()) {
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping attendanceRemark id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }
            LocalDate remarkDate = Instant.ofEpochMilli(source.getRemarkDate().getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            AttendanceEntity attendance = attendanceByKey.get(employee.getId() + ":" + remarkDate);
            if (attendance == null) {
                log.warn(
                        "Skipping attendanceRemark id={}, no attendance row for employee+date",
                        source.getId());
                continue;
            }
            String remark = source.getRemark().trim();
            String existing = attendance.getRemarks();
            if (existing != null && existing.contains(remark)) {
                continue;
            }
            attendance.setRemarks(existing == null || existing.isBlank() ? remark : existing + " | " + remark);
            toSave.add(attendance);
            imported++;
        }

        if (!toSave.isEmpty()) {
            attendanceRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", imported);
    }
}
