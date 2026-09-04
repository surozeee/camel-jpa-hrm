package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EmployeeLoanPayment;
import com.jojolaptech.camel.model.mysql.enums.LoanPaymentType;
import com.jojolaptech.camel.model.postgres.company.LoanAccountEntity;
import com.jojolaptech.camel.model.postgres.company.LoanPaymentEntity;
import com.jojolaptech.camel.model.postgres.company.enums.LoanPaymentMethodEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LoanPaymentStatusEnum;
import com.jojolaptech.camel.repository.postgres.company.PgLoanAccountRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLoanPaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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

/** Migrates employeeLoanPayment → hrm_loan_payment. */
@Component
@RequiredArgsConstructor
public class EmployeeLoanPaymentProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeLoanPaymentProcessor.class);

    private final PgLoanAccountRepository loanAccountRepository;
    private final PgLoanPaymentRepository loanPaymentRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeeLoanPayment> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> existingIds = loanPaymentRepository.findMysqlIdsByMysqlIdIn(
                batch.stream().map(EmployeeLoanPayment::getId).collect(Collectors.toSet()));

        Set<Long> loanMysqlIds = batch.stream()
                .filter(row -> row.getEmployeeLoan() != null)
                .map(row -> row.getEmployeeLoan().getId())
                .collect(Collectors.toSet());
        Map<Long, LoanAccountEntity> accountByLoanMysqlId =
                loanAccountRepository.findByMysqlIdIn(loanMysqlIds).stream()
                        .collect(Collectors.toMap(LoanAccountEntity::getMysqlId, a -> a, (a, b) -> a));

        List<LoanPaymentEntity> toSave = new ArrayList<>();
        for (EmployeeLoanPayment source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployeeLoan() == null) {
                log.warn("Skipping employeeLoanPayment id={}, missing employeeLoan", source.getId());
                continue;
            }
            LoanAccountEntity account = accountByLoanMysqlId.get(source.getEmployeeLoan().getId());
            if (account == null) {
                log.warn(
                        "Skipping employeeLoanPayment id={}, loan account mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployeeLoan().getId());
                continue;
            }

            LocalDate paymentDate = PayrollHeadingMigrationMapper.toLocalDate(source.getPaidDate());
            if (paymentDate == null) {
                paymentDate = LocalDate.now();
            }

            LoanPaymentMethodEnum method = LoanPaymentMethodEnum.PAYROLL_DEDUCTION;
            if (source.getEmployeeLoan().getPaymentType() != null
                    && source.getEmployeeLoan().getPaymentType() != LoanPaymentType.NORMAL_DEDUCTION) {
                method = LoanPaymentMethodEnum.BANK_TRANSFER;
            }

            String payPeriodRef = source.getPayPeriod() != null
                    ? "payPeriod#" + source.getPayPeriod().getId()
                    : null;

            toSave.add(LoanPaymentEntity.builder()
                    .mysqlId(source.getId())
                    .loanAccount(account)
                    .paymentNumber("LP-" + source.getId())
                    .companyId(account.getCompanyId())
                    .employeeId(account.getEmployeeId())
                    .paymentDate(paymentDate)
                    .amount(source.getPaidAmount() != null ? source.getPaidAmount() : BigDecimal.ZERO)
                    .paymentMethod(method)
                    .paymentStatus(LoanPaymentStatusEnum.COMPLETED)
                    .transactionReference(payPeriodRef)
                    .remarks("migrated from employeeLoanPayment#" + source.getId())
                    .build());
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            loanPaymentRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
