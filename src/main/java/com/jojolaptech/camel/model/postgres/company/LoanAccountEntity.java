package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.FinancialRequestTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.InterestTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LoanRequestStatusEnum;
import com.jojolaptech.camel.model.postgres.company.enums.RepaymentMethodEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hrm_loan_account")
public class LoanAccountEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    private LoanRequestEntity request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private LoanPolicyEntity policy;

    @Column(name = "account_number", nullable = false, length = 64)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 40)
    private FinancialRequestTypeEnum requestType;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "currency_code", length = 10)
    private String currencyCode;

    @Column(name = "principal_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "total_repayable", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalRepayable;

    @Column(name = "outstanding_principal", nullable = false, precision = 19, scale = 2)
    private BigDecimal outstandingPrincipal;

    @Column(name = "outstanding_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal outstandingBalance;

    @Column(name = "total_paid", precision = 19, scale = 2)
    private BigDecimal totalPaid;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_type", nullable = false, length = 40)
    private InterestTypeEnum interestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "repayment_method", nullable = false, length = 40)
    private RepaymentMethodEnum repaymentMethod;

    @Column(name = "installment_count")
    private Integer installmentCount;

    @Column(name = "monthly_installment", precision = 19, scale = 2)
    private BigDecimal monthlyInstallment;

    @Column(name = "fixed_monthly_amount", precision = 19, scale = 2)
    private BigDecimal fixedMonthlyAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 40)
    private LoanRequestStatusEnum accountStatus;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
