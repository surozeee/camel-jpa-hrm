package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.LeaveCancellation;
import com.jojolaptech.camel.model.postgres.company.LeaveRequestEntity;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveStatusEnum;
import com.jojolaptech.camel.repository.postgres.company.PgLeaveRequestRepository;
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
public class LeaveCancellationProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(LeaveCancellationProcessor.class);

    private final PgLeaveRequestRepository leaveRequestRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<LeaveCancellation> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> cancellationMysqlIds = batch.stream().map(LeaveCancellation::getId).collect(Collectors.toSet());
        Set<Long> alreadyApplied =
                new HashSet<>(leaveRequestRepository.findMysqlCancellationIdsByMysqlCancellationIdIn(cancellationMysqlIds));

        Set<Long> applicationMysqlIds = batch.stream()
                .filter(row -> row.getLeaveApplication() != null)
                .map(row -> row.getLeaveApplication().getId())
                .collect(Collectors.toSet());
        Map<Long, LeaveRequestEntity> leaveByMysqlId = new HashMap<>();
        if (!applicationMysqlIds.isEmpty()) {
            for (LeaveRequestEntity row : leaveRequestRepository.findByMysqlIdIn(applicationMysqlIds)) {
                leaveByMysqlId.put(row.getMysqlId(), row);
            }
        }

        List<LeaveRequestEntity> toSave = new ArrayList<>();
        int imported = 0;

        for (LeaveCancellation source : batch) {
            if (alreadyApplied.contains(source.getId())) {
                continue;
            }
            if (source.getLeaveApplication() == null) {
                log.warn("Skipping leaveCancellation id={}, missing leaveApplication", source.getId());
                continue;
            }

            LeaveStatusEnum mappedStatus = LeaveApplicationMigrationMapper.mapCancellationStatus(source.getStatus());
            if (mappedStatus == null) {
                continue;
            }

            LeaveRequestEntity leave = leaveByMysqlId.get(source.getLeaveApplication().getId());
            if (leave == null) {
                log.warn(
                        "Skipping leaveCancellation id={}, leaveApplication mysqlId={} not migrated",
                        source.getId(),
                        source.getLeaveApplication().getId());
                continue;
            }

            if (leave.getLeaveStatus() == LeaveStatusEnum.CANCELLED
                    && Objects.equals(leave.getMysqlCancellationId(), source.getId())) {
                continue;
            }
            if (leave.getLeaveStatus() == LeaveStatusEnum.CANCELLED
                    && mappedStatus == LeaveStatusEnum.CANCELLED
                    && leave.getMysqlCancellationId() != null) {
                continue;
            }

            leave.setLeaveStatus(mappedStatus);
            leave.setMysqlCancellationId(source.getId());

            String cancelNote = source.getReason() != null && !source.getReason().isBlank()
                    ? "Cancel: " + source.getReason().trim()
                    : "Cancel: migrated leaveCancellation id=" + source.getId();
            if (leave.getRemarks() == null || leave.getRemarks().isBlank()) {
                leave.setRemarks(LeaveApplicationMigrationMapper.truncate(cancelNote, 500));
            } else {
                leave.setRemarks(LeaveApplicationMigrationMapper.truncate(
                        leave.getRemarks() + " | " + cancelNote, 500));
            }

            toSave.add(leave);
            alreadyApplied.add(source.getId());
            imported++;
        }

        if (!toSave.isEmpty()) {
            leaveRequestRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", imported);
    }
}
