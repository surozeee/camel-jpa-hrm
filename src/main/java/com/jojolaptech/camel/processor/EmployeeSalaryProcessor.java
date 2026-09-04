package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EmployeePayrollDate;
import com.jojolaptech.camel.model.mysql.EmployeePayrollHeading;
import com.jojolaptech.camel.model.postgres.company.BranchSalaryBreakdownEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeSalaryComponentEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeSalaryEntity;
import com.jojolaptech.camel.repository.mysql.EmployeePayrollDateRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchSalaryBreakdownRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeSalaryComponentRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeSalaryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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

/**
 * Migrates open employeePayrollHeading + employeePayrollDate amounts into
 * hrm_employee_salary / hrm_employee_salary_component.
 */
@Component
@RequiredArgsConstructor
public class EmployeeSalaryProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeSalaryProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgBranchSalaryBreakdownRepository breakdownRepository;
    private final PgEmployeeSalaryRepository employeeSalaryRepository;
    private final PgEmployeeSalaryComponentRepository employeeSalaryComponentRepository;
    private final EmployeePayrollDateRepository employeePayrollDateRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeePayrollHeading> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> headingMysqlIds = batch.stream().map(EmployeePayrollHeading::getId).collect(Collectors.toSet());
        Set<Long> existingComponentIds = employeeSalaryComponentRepository.findMysqlIdsByMysqlIdIn(headingMysqlIds);

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, row -> row, (a, b) -> a));

        Set<Long> companyHeadingIds = batch.stream()
                .filter(row -> row.getPayrollHeading() != null)
                .map(row -> row.getPayrollHeading().getId())
                .collect(Collectors.toSet());
        Map<Long, BranchSalaryBreakdownEntity> breakdownByMysqlId =
                breakdownRepository.findByMysqlIdIn(companyHeadingIds).stream()
                        .collect(Collectors.toMap(
                                BranchSalaryBreakdownEntity::getMysqlId, row -> row, (a, b) -> a));

        Map<Long, EmployeePayrollDate> openDateByHeadingId =
                employeePayrollDateRepository.findOpenByHeadingIds(headingMysqlIds).stream()
                        .collect(Collectors.toMap(
                                d -> d.getEmployeePayrollHeading().getId(), d -> d, (a, b) -> a));

        Map<Long, EmployeeSalaryEntity> openSalaryByEmployeeMysqlId = new HashMap<>();
        for (EmployeeEntity employee : employeeByMysqlId.values()) {
            employeeSalaryRepository
                    .findByEmployeeIdAndEndDateIsNull(employee.getId())
                    .ifPresent(salary -> openSalaryByEmployeeMysqlId.put(employee.getMysqlId(), salary));
            employeeSalaryRepository
                    .findByMysqlId(employee.getMysqlId())
                    .ifPresent(salary -> openSalaryByEmployeeMysqlId.putIfAbsent(employee.getMysqlId(), salary));
        }

        List<EmployeeSalaryEntity> salariesToSave = new ArrayList<>();
        List<EmployeeSalaryComponentEntity> componentsToSave = new ArrayList<>();

        for (EmployeePayrollHeading source : batch) {
            if (existingComponentIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployee() == null || source.getPayrollHeading() == null) {
                log.warn("Skipping employeePayrollHeading id={}, missing employee/heading", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping employeePayrollHeading id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }
            BranchSalaryBreakdownEntity breakdown = breakdownByMysqlId.get(source.getPayrollHeading().getId());
            if (breakdown == null) {
                log.warn(
                        "Skipping employeePayrollHeading id={}, companyPayrollHeading mysqlId={} not migrated",
                        source.getId(),
                        source.getPayrollHeading().getId());
                continue;
            }
            EmployeePayrollDate openDate = openDateByHeadingId.get(source.getId());
            if (openDate == null || openDate.getPayrollAmount() == null) {
                log.warn("Skipping employeePayrollHeading id={}, no open employeePayrollDate amount", source.getId());
                continue;
            }

            LocalDate effectiveDate = PayrollHeadingMigrationMapper.toLocalDate(source.getStartDate());
            if (effectiveDate == null) {
                effectiveDate = PayrollHeadingMigrationMapper.toLocalDate(openDate.getStartDate());
            }
            if (effectiveDate == null) {
                effectiveDate = LocalDate.now();
            }

            EmployeeSalaryEntity salary = openSalaryByEmployeeMysqlId.get(employee.getMysqlId());
            if (salary == null) {
                salary = EmployeeSalaryEntity.builder()
                        .mysqlId(employee.getMysqlId())
                        .employeeId(employee.getId())
                        .effectiveDate(effectiveDate)
                        .endDate(null)
                        .remarks("migrated from employeePayrollHeading")
                        .build();
                openSalaryByEmployeeMysqlId.put(employee.getMysqlId(), salary);
                salariesToSave.add(salary);
            }

            LocalDate componentEnd = PayrollHeadingMigrationMapper.toLocalDate(source.getEndDate());
            if (componentEnd == null) {
                componentEnd = PayrollHeadingMigrationMapper.toLocalDate(openDate.getEndDate());
            }

            componentsToSave.add(EmployeeSalaryComponentEntity.builder()
                    .mysqlId(source.getId())
                    .employeeId(employee.getId())
                    .amount(openDate.getPayrollAmount().compareTo(BigDecimal.ZERO) < 0
                            ? openDate.getPayrollAmount().abs()
                            : openDate.getPayrollAmount())
                    .effectiveDate(effectiveDate)
                    .endDate(componentEnd)
                    .isActive(Boolean.TRUE.equals(source.getStatus()))
                    .remarks("migrated from employeePayrollHeading#" + source.getId())
                    .lineName(breakdown.getLineName())
                    .lineType(breakdown.getLineType())
                    .rateType(breakdown.getRateType())
                    .rateValue(breakdown.getRateValue())
                    .percentBase(breakdown.getPercentBase())
                    .isBasicSalaryLine(breakdown.getIsBasicSalaryLine())
                    .displayOrder(breakdown.getDisplayOrder())
                    .description(breakdown.getDescription())
                    .isTaxable(breakdown.getIsTaxable())
                    .appliesDuringProbation(breakdown.getAppliesDuringProbation())
                    .appliesAfterProbation(breakdown.getAppliesAfterProbation())
                    .employeeSalary(salary)
                    .branchSalaryBreakdown(breakdown)
                    .build());
            existingComponentIds.add(source.getId());
        }

        if (!salariesToSave.isEmpty()) {
            employeeSalaryRepository.saveAll(salariesToSave);
        }
        if (!componentsToSave.isEmpty()) {
            employeeSalaryComponentRepository.saveAll(componentsToSave);
        }
        exchange.setProperty("batchImported", componentsToSave.size());
    }
}
