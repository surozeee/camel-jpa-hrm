package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.AttEmpTempShift;
import com.jojolaptech.camel.model.postgres.company.BranchShiftEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeTempShiftEntity;
import com.jojolaptech.camel.repository.postgres.company.PgBranchShiftRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeTempShiftRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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
public class EmployeeTempShiftProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeTempShiftProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgCompanyRepository companyRepository;
    private final PgBranchShiftRepository branchShiftRepository;
    private final PgEmployeeTempShiftRepository employeeTempShiftRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<AttEmpTempShift> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(AttEmpTempShift::getId).collect(Collectors.toSet());
        Set<Long> existingIds = new HashSet<>(employeeTempShiftRepository.findMysqlIdsByMysqlIdIn(mysqlIds));

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

        Set<Long> timeTableIds = batch.stream()
                .map(AttEmpTempShift::getAttTimeTable)
                .filter(Objects::nonNull)
                .map(tt -> tt.getId())
                .collect(Collectors.toSet());
        Map<Long, List<BranchShiftEntity>> shiftsByMysqlId = timeTableIds.isEmpty()
                ? Map.of()
                : branchShiftRepository.findByMysqlIdIn(timeTableIds).stream()
                        .collect(Collectors.groupingBy(BranchShiftEntity::getMysqlId));

        List<EmployeeTempShiftEntity> toSave = new ArrayList<>();
        for (AttEmpTempShift source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployee() == null || source.getDates() == null) {
                log.warn("Skipping attEmpTempShift id={}, missing employee/dates", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping attEmpTempShift id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }
            CompanyEntity company = source.getCompany() == null
                    ? null
                    : companyByMysqlId.get(source.getCompany().getId());
            if (company == null) {
                log.warn(
                        "Skipping attEmpTempShift id={}, company not migrated",
                        source.getId());
                continue;
            }

            UUID branchShiftId = null;
            if (source.getAttTimeTable() != null) {
                List<BranchShiftEntity> shifts =
                        shiftsByMysqlId.getOrDefault(source.getAttTimeTable().getId(), List.of());
                BranchShiftEntity matched = shifts.stream()
                        .filter(s -> employee.getBranchId() != null
                                && Objects.equals(s.getBranchId(), employee.getBranchId()))
                        .findFirst()
                        .orElse(shifts.isEmpty() ? null : shifts.get(0));
                if (matched != null) {
                    branchShiftId = matched.getId();
                } else if (!Boolean.TRUE.equals(source.getIsOffDay())) {
                    log.warn(
                            "Skipping attEmpTempShift id={}, branch shift not found for attTimeTable mysqlId={}",
                            source.getId(),
                            source.getAttTimeTable().getId());
                    continue;
                }
            }

            LocalDate assignmentDate = Instant.ofEpochMilli(source.getDates().getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            toSave.add(EmployeeTempShiftEntity.builder()
                    .mysqlId(source.getId())
                    .companyId(company.getId())
                    .branchId(employee.getBranchId())
                    .employeeId(employee.getId())
                    .assignmentDate(assignmentDate)
                    .branchShiftId(branchShiftId)
                    .offDay(Boolean.TRUE.equals(source.getIsOffDay()))
                    .build());
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            employeeTempShiftRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
