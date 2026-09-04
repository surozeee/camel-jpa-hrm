package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EditedOvertimeDetails;
import com.jojolaptech.camel.model.postgres.company.AttendanceEntity;
import com.jojolaptech.camel.repository.postgres.company.PgAttendanceRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Step 23v: editedOvertimeDetails → update attendance (by attendanceTransaction.id as mysql_id):
 * overtimeManuallyEdited=true, parse previousOverTime to minutes, append remarks.
 */
@Component
@RequiredArgsConstructor
public class EditedOvertimeDetailsProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EditedOvertimeDetailsProcessor.class);

    private final PgAttendanceRepository attendanceRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EditedOvertimeDetails> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> attendanceMysqlIds = batch.stream()
                .filter(s -> s.getAttendanceTransaction() != null)
                .map(s -> s.getAttendanceTransaction().getId())
                .collect(Collectors.toSet());
        Map<Long, AttendanceEntity> attendances =
                attendanceRepository.findByMysqlIdIn(attendanceMysqlIds).stream()
                        .collect(Collectors.toMap(AttendanceEntity::getMysqlId, Function.identity(), (a, b) -> a));

        Set<AttendanceEntity> toSave = new HashSet<>();
        int updated = 0;

        for (EditedOvertimeDetails source : batch) {
            if (source.getAttendanceTransaction() == null) {
                log.warn("Skipping editedOvertimeDetails id={}, missing attendanceTransaction", source.getId());
                continue;
            }
            AttendanceEntity attendance = attendances.get(source.getAttendanceTransaction().getId());
            if (attendance == null) {
                log.warn(
                        "Skipping editedOvertimeDetails id={}, attendance mysqlId={} not migrated",
                        source.getId(),
                        source.getAttendanceTransaction().getId());
                continue;
            }

            attendance.setOvertimeManuallyEdited(true);
            Integer minutes = PayrollCatalogLeftoversMigrationMapper.parseOvertimeToMinutes(
                    source.getPreviousOverTime());
            if (minutes != null) {
                attendance.setOvertimeOverrideMinutes(minutes);
            }

            StringBuilder remarks = new StringBuilder(
                    attendance.getRemarks() != null ? attendance.getRemarks() : "");
            String marker = "[migrated-edited-ot:" + source.getId() + "]";
            if (!remarks.toString().contains(marker)) {
                if (!remarks.isEmpty()) {
                    remarks.append("; ");
                }
                remarks.append(marker);
                if (source.getRemarks() != null && !source.getRemarks().isBlank()) {
                    remarks.append(' ').append(source.getRemarks().trim());
                }
                if (source.getPreviousOverTime() != null && !source.getPreviousOverTime().isBlank()) {
                    remarks.append(" previousOverTime=").append(source.getPreviousOverTime().trim());
                }
                attendance.setRemarks(PayrollCatalogLeftoversMigrationMapper.truncate(remarks.toString(), 1000));
            }

            toSave.add(attendance);
            updated++;
        }

        if (!toSave.isEmpty()) {
            attendanceRepository.saveAll(new ArrayList<>(toSave));
        }
        exchange.setProperty("batchImported", updated);
    }
}
