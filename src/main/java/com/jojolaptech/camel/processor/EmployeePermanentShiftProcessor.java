package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.AttEmpShift;
import com.jojolaptech.camel.model.mysql.AttShiftDetails;
import com.jojolaptech.camel.model.mysql.AttTimeTable;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.BranchShiftEntity;
import com.jojolaptech.camel.repository.mysql.AttShiftDetailsRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchShiftRepository;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class EmployeePermanentShiftProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeePermanentShiftProcessor.class);

    private static final String UPDATE_BRANCH_SHIFT_SQL =
            """
            UPDATE employee
               SET branch_shift_id = ?,
                   last_modified_at = ?,
                   version = COALESCE(version, 0) + 1
             WHERE id = ?
               AND (branch_shift_id IS DISTINCT FROM ?)
            """;

    private final AttShiftDetailsRepository attShiftDetailsRepository;
    private final PgBranchRepository branchRepository;
    private final PgBranchShiftRepository branchShiftRepository;
    private final JdbcTemplate postgresJdbcTemplate;

    public EmployeePermanentShiftProcessor(
            AttShiftDetailsRepository attShiftDetailsRepository,
            PgBranchRepository branchRepository,
            PgBranchShiftRepository branchShiftRepository,
            @Qualifier("postgresJdbcTemplate") JdbcTemplate postgresJdbcTemplate) {
        this.attShiftDetailsRepository = attShiftDetailsRepository;
        this.branchRepository = branchRepository;
        this.branchShiftRepository = branchShiftRepository;
        this.postgresJdbcTemplate = postgresJdbcTemplate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<AttEmpShift> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Map<Long, AttEmpShift> latestByEmployee = new HashMap<>();
        for (AttEmpShift row : batch) {
            if (row.getEmployee() == null || row.getAttShift() == null || row.getShiftDate() == null) {
                continue;
            }
            Long employeeId = row.getEmployee().getId();
            AttEmpShift existing = latestByEmployee.get(employeeId);
            if (existing == null || row.getShiftDate().after(existing.getShiftDate())) {
                latestByEmployee.put(employeeId, row);
            }
        }
        if (latestByEmployee.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> employeeMysqlIds = latestByEmployee.keySet();
        Map<Long, EmployeeRow> employeeByMysqlId = loadEmployeesByMysqlId(employeeMysqlIds);

        Set<Long> shiftIds = latestByEmployee.values().stream()
                .map(row -> row.getAttShift().getId())
                .collect(Collectors.toSet());
        Map<Long, List<AttShiftDetails>> detailsByShift =
                attShiftDetailsRepository.findActiveByAttShiftIdIn(shiftIds).stream()
                        .collect(Collectors.groupingBy(d -> d.getAttShift().getId()));

        Set<Long> timeTableIds = detailsByShift.values().stream()
                .flatMap(List::stream)
                .map(AttShiftDetails::getAttTimeTable)
                .filter(Objects::nonNull)
                .map(AttTimeTable::getId)
                .collect(Collectors.toSet());

        Set<UUID> branchUuids = employeeByMysqlId.values().stream()
                .map(EmployeeRow::branchId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, Long> mysqlBranchByUuid = branchUuids.isEmpty()
                ? Map.of()
                : branchRepository.findAllById(branchUuids).stream()
                        .filter(b -> b.getMysqlId() != null)
                        .collect(Collectors.toMap(BranchEntity::getId, BranchEntity::getMysqlId, (a, b) -> a));

        Set<Long> mysqlBranchIds = mysqlBranchByUuid.values().stream().collect(Collectors.toSet());
        Map<Long, List<BranchShiftEntity>> shiftsByMysqlId = Map.of();
        if (!timeTableIds.isEmpty()) {
            List<BranchShiftEntity> matchedShifts = mysqlBranchIds.isEmpty()
                    ? branchShiftRepository.findByMysqlIdIn(timeTableIds)
                    : branchShiftRepository.findByMysqlIdInAndMysqlBranchIdIn(timeTableIds, mysqlBranchIds);
            shiftsByMysqlId = matchedShifts.stream()
                    .collect(Collectors.groupingBy(BranchShiftEntity::getMysqlId));
        }

        record ShiftUpdate(UUID employeeId, UUID branchShiftId) {}
        List<ShiftUpdate> toUpdate = new ArrayList<>();
        int skippedNoEmployee = 0;
        int skippedNoTimeTable = 0;
        int skippedNoBranchShift = 0;
        for (Map.Entry<Long, AttEmpShift> entry : latestByEmployee.entrySet()) {
            EmployeeRow employee = employeeByMysqlId.get(entry.getKey());
            if (employee == null) {
                skippedNoEmployee++;
                continue;
            }
            List<AttShiftDetails> details =
                    detailsByShift.getOrDefault(entry.getValue().getAttShift().getId(), List.of());
            AttTimeTable timeTable = details.stream()
                    .map(AttShiftDetails::getAttTimeTable)
                    .filter(Objects::nonNull)
                    .min(Comparator.comparing(AttTimeTable::getId))
                    .orElse(null);
            if (timeTable == null) {
                skippedNoTimeTable++;
                continue;
            }
            List<BranchShiftEntity> branchShifts =
                    shiftsByMysqlId.getOrDefault(timeTable.getId(), List.of());
            BranchShiftEntity matched = branchShifts.stream()
                    .filter(s -> employee.branchId() != null
                            && Objects.equals(s.getBranchId(), employee.branchId()))
                    .findFirst()
                    .orElse(branchShifts.isEmpty() ? null : branchShifts.get(0));
            if (matched == null) {
                skippedNoBranchShift++;
                continue;
            }
            if (Objects.equals(employee.branchShiftId(), matched.getId())) {
                continue;
            }
            toUpdate.add(new ShiftUpdate(employee.id(), matched.getId()));
        }

        if (skippedNoEmployee + skippedNoTimeTable + skippedNoBranchShift > 0) {
            log.warn(
                    "Permanent shift skips: noEmployee={}, noTimeTable={}, noBranchShift={}",
                    skippedNoEmployee,
                    skippedNoTimeTable,
                    skippedNoBranchShift);
        }

        int updated = 0;
        if (!toUpdate.isEmpty()) {
            Timestamp now = Timestamp.from(Instant.now());
            int[] counts = postgresJdbcTemplate.batchUpdate(
                    UPDATE_BRANCH_SHIFT_SQL,
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ShiftUpdate u = toUpdate.get(i);
                            ps.setObject(1, u.branchShiftId());
                            ps.setTimestamp(2, now);
                            ps.setObject(3, u.employeeId());
                            ps.setObject(4, u.branchShiftId());
                        }

                        @Override
                        public int getBatchSize() {
                            return toUpdate.size();
                        }
                    });
            for (int c : counts) {
                if (c > 0) {
                    updated += c;
                }
            }
            log.info("Permanent shift chunk updated {} employee branch_shift_id row(s)", updated);
        }
        exchange.setProperty("batchImported", updated);
    }

    private record EmployeeRow(UUID id, Long mysqlId, UUID branchId, UUID branchShiftId) {}

    private Map<Long, EmployeeRow> loadEmployeesByMysqlId(Set<Long> mysqlIds) {
        if (mysqlIds == null || mysqlIds.isEmpty()) {
            return Map.of();
        }
        List<Long> idList = new ArrayList<>(mysqlIds);
        Map<Long, EmployeeRow> out = new HashMap<>();
        final int batch = 500;
        for (int i = 0; i < idList.size(); i += batch) {
            List<Long> slice = idList.subList(i, Math.min(i + batch, idList.size()));
            String placeholders = String.join(",", Collections.nCopies(slice.size(), "?"));
            String sql =
                    "SELECT id, mysql_id, branch_id, branch_shift_id FROM employee WHERE mysql_id IN ("
                            + placeholders
                            + ")";
            postgresJdbcTemplate.query(
                    sql,
                    slice.toArray(),
                    rs -> {
                        out.put(
                                rs.getLong("mysql_id"),
                                new EmployeeRow(
                                        (UUID) rs.getObject("id"),
                                        rs.getLong("mysql_id"),
                                        (UUID) rs.getObject("branch_id"),
                                        (UUID) rs.getObject("branch_shift_id")));
                    });
        }
        return out;
    }
}
