package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.LeaveAccumulation;
import com.jojolaptech.camel.model.postgres.company.BranchLeaveTypeEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeLeaveAccumulationEntity;
import com.jojolaptech.camel.repository.postgres.company.PgBranchLeaveTypeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeLeaveAccumulationRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class EmployeeLeaveAccumulationProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeLeaveAccumulationProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgBranchLeaveTypeRepository branchLeaveTypeRepository;
    private final PgEmployeeLeaveAccumulationRepository employeeLeaveAccumulationRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<LeaveAccumulation> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(LeaveAccumulation::getId).collect(Collectors.toSet());
        Set<Long> existingIds = employeeLeaveAccumulationRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, row -> row, (left, right) -> left));

        Set<Long> leaveMysqlIds = batch.stream()
                .filter(row -> row.getLeaves() != null)
                .map(row -> row.getLeaves().getId())
                .collect(Collectors.toSet());
        Map<String, BranchLeaveTypeEntity> branchLeaveTypeByKey =
                branchLeaveTypeRepository.findByMysqlLeaveIdIn(leaveMysqlIds).stream()
                        .collect(Collectors.toMap(
                                row -> row.getMysqlLeaveId() + ":" + row.getBranchId(),
                                row -> row,
                                (left, right) -> left));

        List<EmployeeLeaveAccumulationEntity> toSave = new ArrayList<>();
        for (LeaveAccumulation source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployee() == null || source.getLeaves() == null || source.getSetDate() == null) {
                log.warn("Skipping leaveAccumulation id={}, missing employee/leave/date", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null || employee.getBranchId() == null) {
                log.warn("Skipping leaveAccumulation id={}, employee not migrated", source.getId());
                continue;
            }
            BranchLeaveTypeEntity branchLeaveType =
                    branchLeaveTypeByKey.get(source.getLeaves().getId() + ":" + employee.getBranchId());
            if (branchLeaveType == null) {
                log.warn(
                        "Skipping leaveAccumulation id={}, branch leave type not found for leave mysqlId={}",
                        source.getId(),
                        source.getLeaves().getId());
                continue;
            }

            var localDate = source.getSetDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int accumulationMonth = localDate.getYear() * 100 + localDate.getMonthValue();

            toSave.add(EmployeeLeaveAccumulationEntity.builder()
                    .mysqlId(source.getId())
                    .employeeId(employee.getId())
                    .branchId(employee.getBranchId())
                    .branchLeaveType(branchLeaveType)
                    .accumulationMonth(accumulationMonth)
                    .accumulatedLeaves(BigDecimal.valueOf(source.getDays()))
                    .remarks(source.getRemarks())
                    .build());
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            employeeLeaveAccumulationRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
