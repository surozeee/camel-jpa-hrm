package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.AttEmpShift;
import com.jojolaptech.camel.model.mysql.AttShiftDetails;
import com.jojolaptech.camel.model.mysql.AttTimeTable;
import com.jojolaptech.camel.model.postgres.company.BranchShiftEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.repository.mysql.AttShiftDetailsRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchShiftRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
public class EmployeePermanentShiftProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeePermanentShiftProcessor.class);

    private final AttShiftDetailsRepository attShiftDetailsRepository;
    private final PgEmployeeRepository employeeRepository;
    private final PgBranchShiftRepository branchShiftRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<AttEmpShift> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        // Latest assignment per employee within this page (and across pages: last write wins by shiftDate).
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
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

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
        Map<Long, List<BranchShiftEntity>> shiftsByMysqlId = timeTableIds.isEmpty()
                ? Map.of()
                : branchShiftRepository.findByMysqlIdIn(timeTableIds).stream()
                        .collect(Collectors.groupingBy(BranchShiftEntity::getMysqlId));

        List<EmployeeEntity> toSave = new ArrayList<>();
        for (Map.Entry<Long, AttEmpShift> entry : latestByEmployee.entrySet()) {
            EmployeeEntity employee = employeeByMysqlId.get(entry.getKey());
            if (employee == null) {
                log.warn(
                        "Skipping attEmpShift id={}, employee mysqlId={} not migrated",
                        entry.getValue().getId(),
                        entry.getKey());
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
                log.warn(
                        "Skipping attEmpShift id={}, attShift id={} has no attTimeTable",
                        entry.getValue().getId(),
                        entry.getValue().getAttShift().getId());
                continue;
            }
            List<BranchShiftEntity> branchShifts =
                    shiftsByMysqlId.getOrDefault(timeTable.getId(), List.of());
            BranchShiftEntity matched = branchShifts.stream()
                    .filter(s -> employee.getBranchId() != null
                            && Objects.equals(s.getBranchId(), employee.getBranchId()))
                    .findFirst()
                    .orElse(branchShifts.isEmpty() ? null : branchShifts.get(0));
            if (matched == null) {
                log.warn(
                        "Skipping attEmpShift id={}, branch shift not found for attTimeTable mysqlId={}",
                        entry.getValue().getId(),
                        timeTable.getId());
                continue;
            }
            if (Objects.equals(employee.getBranchShiftId(), matched.getId())) {
                continue;
            }
            employee.setBranchShiftId(matched.getId());
            toSave.add(employee);
        }

        if (!toSave.isEmpty()) {
            employeeRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
