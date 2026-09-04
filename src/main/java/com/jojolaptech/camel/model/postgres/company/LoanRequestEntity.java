package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.FinancialRequestTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LoanRequestStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "hrm_loan_request")
public class LoanRequestEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "request_number", nullable = false, length = 64)
    private String requestNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 40)
    private FinancialRequestTypeEnum requestType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private LoanPolicyEntity policy;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "requested_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "currency_code", length = 10)
    @Builder.Default
    private String currencyCode = "NPR";

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false, length = 40)
    private LoanRequestStatusEnum requestStatus;

    @Column(name = "approved_amount", precision = 19, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "terms_accepted", nullable = false)
    @Builder.Default
    private Boolean termsAccepted = true;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
