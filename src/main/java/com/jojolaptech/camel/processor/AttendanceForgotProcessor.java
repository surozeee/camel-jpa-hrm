package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.AttendanceForgot;
import com.jojolaptech.camel.model.postgres.company.AttendanceTimeRequestEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceTimeRequestStatusEnum;
import com.jojolaptech.camel.repository.postgres.company.PgAttendanceTimeRequestRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
public class AttendanceForgotProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(AttendanceForgotProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgCompanyRepository companyRepository;
    private final PgAttendanceTimeRequestRepository attendanceTimeRequestRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<AttendanceForgot> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(AttendanceForgot::getId).collect(Collectors.toSet());
        Set<Long> existingIds = new HashSet<>(attendanceTimeRequestRepository.findMysqlIdsByMysqlIdIn(mysqlIds));

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<Long> companyMysqlIds = batch.stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companyByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, c -> c, (a, b) -> a));

        List<AttendanceTimeRequestEntity> toSave = new ArrayList<>();
        for (AttendanceForgot source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployee() == null || source.getCheckInOutDate() == null) {
                log.warn("Skipping attendanceForgot id={}, missing employee/checkInOutDate", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping attendanceForgot id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }
            CompanyEntity company = source.getCompany() == null
                    ? null
                    : companyByMysqlId.get(source.getCompany().getId());
            if (company == null) {
                log.warn("Skipping attendanceForgot id={}, company not migrated", source.getId());
                continue;
            }

            LocalDate attendanceDate = Instant.ofEpochMilli(source.getCheckInOutDate().getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalTime punchTime = source.getTime() == null ? null : source.getTime().toLocalTime();
            LocalTime checkIn = null;
            LocalTime checkOut = null;
            if (AttendancePunchMigrationMapper.isCheckInType(source.getType())) {
                checkIn = punchTime;
            } else {
                checkOut = punchTime;
            }

            AttendanceTimeRequestStatusEnum status =
                    AttendancePunchMigrationMapper.mapForgotStatus(source.getStatus());
            String reason = AttendancePunchMigrationMapper.combineReason(source.getReason(), source.getRemark());

            toSave.add(AttendanceTimeRequestEntity.builder()
                    .mysqlId(source.getId())
                    .companyId(company.getId())
                    .branchId(employee.getBranchId())
                    .employeeId(employee.getId())
                    .attendanceDate(attendanceDate)
                    .checkInTime(checkIn)
                    .checkOutTime(checkOut)
                    .reason(reason)
                    .requestStatus(status)
                    .remarks(source.getRemark())
                    .build());
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            attendanceTimeRequestRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
