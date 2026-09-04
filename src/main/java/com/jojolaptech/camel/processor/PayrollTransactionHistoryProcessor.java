package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.PayrollMonth;
import com.jojolaptech.camel.model.mysql.PayrollTransaction;
import com.jojolaptech.camel.model.postgres.company.BranchEmployeeMonthWiseSalaryEntity;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.BranchSalaryBreakdownEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.repository.mysql.PayrollTransactionRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchEmployeeMonthWiseSalaryRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchSalaryBreakdownRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
 * For each payrollMonth, loads all payrollTransaction rows and writes
 * employee+month aggregates into hrm_branch_employee_month_wise_salary
 * when no modern payment month-wise row exists.
 */
@Component
@RequiredArgsConstructor
public class PayrollTransactionHistoryProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(PayrollTransactionHistoryProcessor.class);

    private final PayrollTransactionRepository payrollTransactionRepository;
    private final PgEmployeeRepository employeeRepository;
    private final PgCompanyRepository companyRepository;
    private final PgBranchRepository branchRepository;
    private final PgBranchSalaryBreakdownRepository breakdownRepository;
    private final PgBranchEmployeeMonthWiseSalaryRepository monthWiseSalaryRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<PayrollMonth> months = exchange.getMessage().getBody(List.class);
        if (months == null || months.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        int imported = 0;
        for (PayrollMonth month : months) {
            imported += migrateMonth(month);
        }
        exchange.setProperty("batchImported", imported);
    }

    private int migrateMonth(PayrollMonth month) {
        List<PayrollTransaction> txs = payrollTransactionRepository.findByPayrollMonthId(month.getId());
        if (txs.isEmpty()) {
            return 0;
        }

        Map<Long, List<PayrollTransaction>> byEmployee = txs.stream()
                .filter(t -> t.getEmployee() != null)
                .collect(Collectors.groupingBy(t -> t.getEmployee().getId()));

        Set<Long> employeeMysqlIds = byEmployee.keySet();
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<Long> companyMysqlIds = txs.stream()
                .filter(t -> t.getCompany() != null)
                .map(t -> t.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, UUID> companyIdByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, CompanyEntity::getId, (a, b) -> a));

        Map<Long, BranchEntity> primaryBranchByCompanyMysqlId = new HashMap<>();
        if (!companyMysqlIds.isEmpty()) {
            for (BranchEntity branch : branchRepository.findByCompanyMysqlIdIn(companyMysqlIds)) {
                Long companyMysqlId = branch.getCompany() != null ? branch.getCompany().getMysqlId() : null;
                if (companyMysqlId != null) {
                    primaryBranchByCompanyMysqlId.putIfAbsent(companyMysqlId, branch);
                }
            }
        }

        Set<UUID> companyIds = new HashSet<>(companyIdByMysqlId.values());
        Map<UUID, Map<String, BranchSalaryBreakdownEntity>> breakdownsByCompany = new HashMap<>();
        if (!companyIds.isEmpty()) {
            for (BranchSalaryBreakdownEntity breakdown : breakdownRepository.findByCompanyIdIn(companyIds)) {
                if (breakdown.getCompanyId() == null) {
                    continue;
                }
                breakdownsByCompany
                        .computeIfAbsent(breakdown.getCompanyId(), id -> new HashMap<>())
                        .putIfAbsent(
                                PayrollPaymentHistoryMapper.normalize(breakdown.getLineName()), breakdown);
            }
        }

        Set<Long> aggregateIds = byEmployee.keySet().stream()
                .map(empId -> PayrollPaymentHistoryMapper.transactionAggregateMysqlId(month.getId(), empId))
                .collect(Collectors.toSet());
        Set<Long> existingMysqlIds = monthWiseSalaryRepository.findMysqlIdsByMysqlIdIn(aggregateIds);

        LocalDate monthDate = PayrollHeadingMigrationMapper.toLocalDate(month.getEndDate());
        if (monthDate == null) {
            monthDate = PayrollHeadingMigrationMapper.toLocalDate(month.getStartDate());
        }
        int year = PayrollPaymentHistoryMapper.salaryYear(monthDate);
        int monthNum = PayrollPaymentHistoryMapper.salaryMonth(monthDate);

        List<BranchEmployeeMonthWiseSalaryEntity> toSave = new ArrayList<>();
        for (Map.Entry<Long, List<PayrollTransaction>> entry : byEmployee.entrySet()) {
            long employeeMysqlId = entry.getKey();
            List<PayrollTransaction> group = entry.getValue();
            long aggregateId =
                    PayrollPaymentHistoryMapper.transactionAggregateMysqlId(month.getId(), employeeMysqlId);
            if (existingMysqlIds.contains(aggregateId)) {
                continue;
            }

            EmployeeEntity employee = employeeByMysqlId.get(employeeMysqlId);
            if (employee == null) {
                log.warn(
                        "Skipping payrollTransaction group month={}, employee mysqlId={} not migrated",
                        month.getId(),
                        employeeMysqlId);
                continue;
            }

            PayrollTransaction sample = group.getFirst();
            UUID branchId = employee.getBranchId();
            if (branchId == null && sample.getCompany() != null) {
                BranchEntity fallback = primaryBranchByCompanyMysqlId.get(sample.getCompany().getId());
                if (fallback != null) {
                    branchId = fallback.getId();
                }
            }
            if (branchId == null) {
                log.warn("Skipping payrollTransaction group employee={}, no branch", employeeMysqlId);
                continue;
            }

            if (monthWiseSalaryRepository
                    .findByBranchIdAndEmployeeIdAndSalaryMonthAndSalaryYear(
                            branchId, employee.getId(), monthNum, year)
                    .isPresent()) {
                continue;
            }

            UUID companyId = sample.getCompany() != null
                    ? companyIdByMysqlId.get(sample.getCompany().getId())
                    : null;
            Map<String, BranchSalaryBreakdownEntity> breakdownByName =
                    companyId != null ? breakdownsByCompany.getOrDefault(companyId, Map.of()) : Map.of();

            toSave.add(PayrollPaymentHistoryMapper.fromTransactions(
                    aggregateId, employee, branchId, year, monthNum, group, breakdownByName));
            existingMysqlIds.add(aggregateId);
        }

        if (!toSave.isEmpty()) {
            monthWiseSalaryRepository.saveAll(toSave);
        }
        return toSave.size();
    }
}
