package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EmployeePayrollHeadingPayment;
import com.jojolaptech.camel.model.mysql.EmployeePayrollPayment;
import com.jojolaptech.camel.model.mysql.HeadingWisePayrollPayment;
import com.jojolaptech.camel.model.postgres.company.BranchEmployeeMonthWiseSalaryEntity;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.BranchSalaryBreakdownEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.PayrollCalculationSnapshot;
import com.jojolaptech.camel.repository.mysql.EmployeePayrollHeadingPaymentRepository;
import com.jojolaptech.camel.repository.mysql.HeadingWisePayrollPaymentRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchEmployeeMonthWiseSalaryRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchSalaryBreakdownRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
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
 * Migrates employeePayrollPayment (+ heading lines) → hrm_branch_employee_month_wise_salary.
 */
@Component
@RequiredArgsConstructor
public class MonthWiseSalaryProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(MonthWiseSalaryProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgBranchRepository branchRepository;
    private final PgBranchSalaryBreakdownRepository breakdownRepository;
    private final PgBranchEmployeeMonthWiseSalaryRepository monthWiseSalaryRepository;
    private final HeadingWisePayrollPaymentRepository headingWisePayrollPaymentRepository;
    private final EmployeePayrollHeadingPaymentRepository employeePayrollHeadingPaymentRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeePayrollPayment> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> paymentIds = batch.stream().map(EmployeePayrollPayment::getId).collect(Collectors.toSet());
        Set<Long> existingIds = monthWiseSalaryRepository.findMysqlIdsByMysqlIdIn(paymentIds);

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(p -> p.getEmployee() != null)
                .map(p -> p.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<Long> companyMysqlIds = new HashSet<>();
        for (EmployeePayrollPayment payment : batch) {
            if (payment.getCompany() != null) {
                companyMysqlIds.add(payment.getCompany().getId());
            } else if (payment.getPaymentPeriod() != null && payment.getPaymentPeriod().getCompany() != null) {
                companyMysqlIds.add(payment.getPaymentPeriod().getCompany().getId());
            }
        }
        Map<Long, BranchEntity> primaryBranchByCompanyMysqlId = new HashMap<>();
        if (!companyMysqlIds.isEmpty()) {
            for (BranchEntity branch : branchRepository.findByCompanyMysqlIdIn(companyMysqlIds)) {
                Long companyMysqlId = branch.getCompany() != null ? branch.getCompany().getMysqlId() : null;
                if (companyMysqlId != null) {
                    primaryBranchByCompanyMysqlId.putIfAbsent(companyMysqlId, branch);
                }
            }
        }

        Map<Long, List<HeadingWisePayrollPayment>> headingWiseByPayment =
                headingWisePayrollPaymentRepository.findByPaymentIds(paymentIds).stream()
                        .collect(Collectors.groupingBy(h -> h.getEmployeePayrollPayment().getId()));
        Map<Long, List<EmployeePayrollHeadingPayment>> headingPaymentByPayment =
                employeePayrollHeadingPaymentRepository.findByPaymentIds(paymentIds).stream()
                        .collect(Collectors.groupingBy(h -> h.getEmployeePayrollPayment().getId()));

        // Preload breakdowns for companies in batch (by scanning line names lazily per payment)
        Map<UUID, Map<String, BranchSalaryBreakdownEntity>> breakdownsByCompany = new HashMap<>();
        Set<UUID> companyIdsForBreakdown = new HashSet<>();
        for (BranchEntity branch : primaryBranchByCompanyMysqlId.values()) {
            if (branch.getCompany() != null) {
                companyIdsForBreakdown.add(branch.getCompany().getId());
            }
        }
        if (!companyIdsForBreakdown.isEmpty()) {
            for (BranchSalaryBreakdownEntity breakdown :
                    breakdownRepository.findByCompanyIdIn(companyIdsForBreakdown)) {
                if (breakdown.getCompanyId() == null) {
                    continue;
                }
                breakdownsByCompany
                        .computeIfAbsent(breakdown.getCompanyId(), id -> new HashMap<>())
                        .putIfAbsent(
                                PayrollPaymentHistoryMapper.normalize(breakdown.getLineName()), breakdown);
            }
        }

        List<BranchEmployeeMonthWiseSalaryEntity> toSave = new ArrayList<>();
        Set<String> periodKeysInBatch = new HashSet<>();

        for (EmployeePayrollPayment payment : batch) {
            if (existingIds.contains(payment.getId())) {
                continue;
            }
            if (payment.getEmployee() == null || payment.getPaymentPeriod() == null) {
                log.warn("Skipping employeePayrollPayment id={}, missing employee/period", payment.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(payment.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping employeePayrollPayment id={}, employee mysqlId={} not migrated",
                        payment.getId(),
                        payment.getEmployee().getId());
                continue;
            }
            UUID branchId = employee.getBranchId();
            Long companyMysqlId = payment.getCompany() != null
                    ? payment.getCompany().getId()
                    : (payment.getPaymentPeriod().getCompany() != null
                            ? payment.getPaymentPeriod().getCompany().getId()
                            : null);
            if (branchId == null) {
                BranchEntity fallback = companyMysqlId != null
                        ? primaryBranchByCompanyMysqlId.get(companyMysqlId)
                        : null;
                if (fallback != null) {
                    branchId = fallback.getId();
                }
            }
            if (branchId == null) {
                log.warn("Skipping employeePayrollPayment id={}, no branch for employee", payment.getId());
                continue;
            }

            var periodDate = PayrollPaymentHistoryMapper.periodDate(payment.getPaymentPeriod());
            int year = PayrollPaymentHistoryMapper.salaryYear(periodDate);
            int month = PayrollPaymentHistoryMapper.salaryMonth(periodDate);
            String periodKey = employee.getId() + ":" + year + ":" + month;
            if (!periodKeysInBatch.add(periodKey)) {
                log.warn(
                        "Skipping employeePayrollPayment id={}, duplicate period in batch for employee {}",
                        payment.getId(),
                        employee.getMysqlId());
                continue;
            }
            if (monthWiseSalaryRepository
                    .findByBranchIdAndEmployeeIdAndSalaryMonthAndSalaryYear(branchId, employee.getId(), month, year)
                    .isPresent()) {
                log.info(
                        "Skipping employeePayrollPayment id={}, month-wise row already exists for {}-{}",
                        payment.getId(),
                        year,
                        month);
                continue;
            }

            UUID companyId = null;
            if (companyMysqlId != null) {
                BranchEntity branch = primaryBranchByCompanyMysqlId.get(companyMysqlId);
                if (branch != null && branch.getCompany() != null) {
                    companyId = branch.getCompany().getId();
                }
            }
            Map<String, BranchSalaryBreakdownEntity> breakdownByName =
                    companyId != null ? breakdownsByCompany.getOrDefault(companyId, Map.of()) : Map.of();

            List<HeadingWisePayrollPayment> headingWise =
                    headingWiseByPayment.getOrDefault(payment.getId(), List.of());
            List<PayrollCalculationSnapshot.SnapshotLine> lines =
                    PayrollPaymentHistoryMapper.linesFromHeadingWise(headingWise, breakdownByName);
            if (lines.isEmpty()) {
                List<EmployeePayrollHeadingPayment> alt =
                        headingPaymentByPayment.getOrDefault(payment.getId(), List.of());
                lines = PayrollPaymentHistoryMapper.linesFromEmployeeHeadingPayments(alt, breakdownByName);
            }

            toSave.add(PayrollPaymentHistoryMapper.fromPayment(payment, employee, branchId, lines));
            existingIds.add(payment.getId());
        }

        if (!toSave.isEmpty()) {
            monthWiseSalaryRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
