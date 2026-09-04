package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EmployeeLoan;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.LoanAccountEntity;
import com.jojolaptech.camel.model.postgres.company.LoanPolicyEntity;
import com.jojolaptech.camel.model.postgres.company.LoanRequestEntity;
import com.jojolaptech.camel.model.postgres.company.enums.FinancialRequestTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.InterestTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LoanRequestStatusEnum;
import com.jojolaptech.camel.model.postgres.company.enums.RepaymentMethodEnum;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLoanAccountRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLoanPolicyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLoanRequestRepository;
import java.math.BigDecimal;
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
 * Migrates employeeLoan → default LoanPolicy + LoanRequest + LoanAccount.
 */
@Component
@RequiredArgsConstructor
public class EmployeeLoanProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeLoanProcessor.class);

    static final String DEFAULT_POLICY_CODE = "MIGRATE-EMPLOYEE-LOAN";

    private final PgEmployeeRepository employeeRepository;
    private final PgBranchRepository branchRepository;
    private final PgLoanPolicyRepository loanPolicyRepository;
    private final PgLoanRequestRepository loanRequestRepository;
    private final PgLoanAccountRepository loanAccountRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeeLoan> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> existingAccountIds = loanAccountRepository.findMysqlIdsByMysqlIdIn(
                batch.stream().map(EmployeeLoan::getId).collect(Collectors.toSet()));

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<UUID> branchIds = employeeByMysqlId.values().stream()
                .map(EmployeeEntity::getBranchId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<UUID, BranchEntity> branchById = new HashMap<>();
        if (!branchIds.isEmpty()) {
            for (BranchEntity branch : branchRepository.findByIdInWithCompany(branchIds)) {
                branchById.put(branch.getId(), branch);
            }
        }

        Set<UUID> companyIds = branchById.values().stream()
                .filter(b -> b.getCompany() != null)
                .map(b -> b.getCompany().getId())
                .collect(Collectors.toSet());

        Map<UUID, LoanPolicyEntity> policyByCompanyId = new HashMap<>();
        if (!companyIds.isEmpty()) {
            for (LoanPolicyEntity policy :
                    loanPolicyRepository.findByCompanyIdInAndPolicyCode(companyIds, DEFAULT_POLICY_CODE)) {
                policyByCompanyId.put(policy.getCompanyId(), policy);
            }
        }

        List<LoanPolicyEntity> policiesToSave = new ArrayList<>();
        List<LoanRequestEntity> requestsToSave = new ArrayList<>();
        List<LoanAccountEntity> accountsToSave = new ArrayList<>();
        Set<UUID> policiesQueued = new HashSet<>();

        for (EmployeeLoan source : batch) {
            if (existingAccountIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployee() == null) {
                log.warn("Skipping employeeLoan id={}, missing employee", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping employeeLoan id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }
            if (employee.getBranchId() == null) {
                log.warn("Skipping employeeLoan id={}, employee has no branchId", source.getId());
                continue;
            }
            BranchEntity branch = branchById.get(employee.getBranchId());
            if (branch == null || branch.getCompany() == null) {
                log.warn(
                        "Skipping employeeLoan id={}, branch/company not found for employee",
                        source.getId());
                continue;
            }

            UUID companyId = branch.getCompany().getId();
            LoanPolicyEntity policy = policyByCompanyId.get(companyId);
            if (policy == null && !policiesQueued.contains(companyId)) {
                policy = LoanPolicyEntity.builder()
                        .companyId(companyId)
                        .policyCode(DEFAULT_POLICY_CODE)
                        .policyName("Migrated Employee Loan")
                        .description("Default policy created during MySQL employeeLoan migration")
                        .requestType(FinancialRequestTypeEnum.EMPLOYEE_LOAN)
                        .interestType(InterestTypeEnum.INTEREST_FREE)
                        .defaultRepaymentMethod(RepaymentMethodEnum.PAYROLL_DEDUCTION)
                        .guarantorRequired(false)
                        .documentRequired(false)
                        .allowEarlySettlement(true)
                        .allowReschedule(true)
                        .allowPaymentHoliday(false)
                        .allowConcurrent(false)
                        .remarks("auto-created for migration")
                        .build();
                policiesToSave.add(policy);
                policyByCompanyId.put(companyId, policy);
                policiesQueued.add(companyId);
            } else if (policy == null) {
                policy = policyByCompanyId.get(companyId);
            }

            BigDecimal principal = source.getLoanAmount() != null ? source.getLoanAmount() : BigDecimal.ZERO;
            BigDecimal remaining =
                    source.getRemainingAmount() != null ? source.getRemainingAmount() : BigDecimal.ZERO;
            BigDecimal paymentAmount =
                    source.getPaymentAmount() != null ? source.getPaymentAmount() : BigDecimal.ZERO;
            BigDecimal totalPaid = principal.subtract(remaining).max(BigDecimal.ZERO);

            boolean closed = Boolean.FALSE.equals(source.getStatus())
                    || remaining.compareTo(BigDecimal.ZERO) <= 0;
            LoanRequestStatusEnum status =
                    closed ? LoanRequestStatusEnum.CLOSED : LoanRequestStatusEnum.REPAYMENT_ACTIVE;

            LoanRequestEntity request = LoanRequestEntity.builder()
                    .mysqlId(source.getId())
                    .requestNumber("LR-" + source.getId())
                    .requestType(FinancialRequestTypeEnum.EMPLOYEE_LOAN)
                    .policy(policy)
                    .companyId(companyId)
                    .branchId(branch.getId())
                    .employeeId(employee.getId())
                    .requestDate(LocalDate.now())
                    .requestedAmount(principal)
                    .currencyCode("NPR")
                    .requestStatus(status)
                    .approvedAmount(principal)
                    .termsAccepted(true)
                    .remarks("migrated from employeeLoan#" + source.getId()
                            + "; provider=" + source.getLoanProvider())
                    .build();

            LoanAccountEntity account = LoanAccountEntity.builder()
                    .mysqlId(source.getId())
                    .request(request)
                    .policy(policy)
                    .accountNumber("LOAN-" + source.getId())
                    .requestType(FinancialRequestTypeEnum.EMPLOYEE_LOAN)
                    .companyId(companyId)
                    .branchId(branch.getId())
                    .employeeId(employee.getId())
                    .currencyCode("NPR")
                    .principalAmount(principal)
                    .totalRepayable(principal)
                    .outstandingPrincipal(remaining)
                    .outstandingBalance(remaining)
                    .totalPaid(totalPaid)
                    .interestType(InterestTypeEnum.INTEREST_FREE)
                    .repaymentMethod(RepaymentMethodEnum.PAYROLL_DEDUCTION)
                    .installmentCount(source.getPaymentFrequency())
                    .monthlyInstallment(paymentAmount)
                    .fixedMonthlyAmount(paymentAmount)
                    .accountStatus(status)
                    .remarks("migrated from employeeLoan#" + source.getId())
                    .build();

            requestsToSave.add(request);
            accountsToSave.add(account);
            existingAccountIds.add(source.getId());
        }

        if (!policiesToSave.isEmpty()) {
            loanPolicyRepository.saveAll(policiesToSave);
        }
        if (!requestsToSave.isEmpty()) {
            loanRequestRepository.saveAll(requestsToSave);
        }
        if (!accountsToSave.isEmpty()) {
            loanAccountRepository.saveAll(accountsToSave);
        }
        exchange.setProperty("batchImported", accountsToSave.size());
    }
}
