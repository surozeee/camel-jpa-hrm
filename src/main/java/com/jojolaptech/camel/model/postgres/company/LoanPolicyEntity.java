package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.FinancialRequestTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.InterestTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.RepaymentMethodEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
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
@Table(name = "hrm_loan_policy")
public class LoanPolicyEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "policy_code", nullable = false, length = 64)
    private String policyCode;

    @Column(name = "policy_name", nullable = false, length = 255)
    private String policyName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 40)
    private FinancialRequestTypeEnum requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_type", nullable = false, length = 40)
    private InterestTypeEnum interestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_repayment_method", length = 40)
    private RepaymentMethodEnum defaultRepaymentMethod;

    @Column(name = "guarantor_required", nullable = false)
    @Builder.Default
    private Boolean guarantorRequired = false;

    @Column(name = "document_required", nullable = false)
    @Builder.Default
    private Boolean documentRequired = false;

    @Column(name = "allow_early_settlement", nullable = false)
    @Builder.Default
    private Boolean allowEarlySettlement = true;

    @Column(name = "allow_reschedule", nullable = false)
    @Builder.Default
    private Boolean allowReschedule = true;

    @Column(name = "allow_payment_holiday", nullable = false)
    @Builder.Default
    private Boolean allowPaymentHoliday = false;

    @Column(name = "allow_concurrent", nullable = false)
    @Builder.Default
    private Boolean allowConcurrent = false;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
