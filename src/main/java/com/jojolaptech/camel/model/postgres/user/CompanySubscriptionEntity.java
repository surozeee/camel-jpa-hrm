package com.jojolaptech.camel.model.postgres.user;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.user.enums.SubscriptionBillingCycleEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "company_subscription",
        uniqueConstraints = @UniqueConstraint(columnNames = "company_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanySubscriptionEntity extends BaseAuditEntity {

    /** Source companyValidity.id when migrated from MySQL. */
    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "package_code", nullable = false, length = 64)
    private String packageCode;

    @Column(name = "package_name", nullable = false)
    private String packageName;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false, length = 20)
    private SubscriptionBillingCycleEnum billingCycle;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "subscription_start_date", nullable = false)
    private LocalDate subscriptionStartDate;

    @Column(name = "subscription_end_date", nullable = false)
    private LocalDate subscriptionEndDate;

    @Column(name = "last_payment_history_id")
    private UUID lastPaymentHistoryId;
}
