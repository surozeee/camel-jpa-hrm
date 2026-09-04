package com.jojolaptech.camel.model.postgres.user;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.user.enums.SubscriptionBillingCycleEnum;
import com.jojolaptech.camel.model.postgres.user.enums.SubscriptionDiscountTypeEnum;
import com.jojolaptech.camel.model.postgres.user.enums.SubscriptionPaymentStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "subscription_payment_history",
        indexes = {
            @Index(name = "idx_sub_pay_hist_company_id", columnList = "company_id"),
            @Index(name = "idx_sub_pay_hist_status", columnList = "payment_status")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPaymentHistoryEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "package_code", nullable = false, length = 64)
    private String packageCode;

    @Column(name = "package_name", nullable = false)
    private String packageName;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false, length = 20)
    private SubscriptionBillingCycleEnum billingCycle;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "original_amount", precision = 19, scale = 2)
    private BigDecimal originalAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", length = 20)
    private SubscriptionDiscountTypeEnum discountType;

    @Column(name = "discount_value", precision = 19, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "payment_method", length = 64)
    private String paymentMethod;

    @Column(name = "payment_reference", length = 128)
    private String paymentReference;

    @Column(name = "proof_document_url", columnDefinition = "TEXT")
    private String proofDocumentUrl;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private SubscriptionPaymentStatusEnum paymentStatus;

    @Column(name = "previous_end_date")
    private LocalDate previousEndDate;

    @Column(name = "period_start_date")
    private LocalDate periodStartDate;

    @Column(name = "period_end_date")
    private LocalDate periodEndDate;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
}
