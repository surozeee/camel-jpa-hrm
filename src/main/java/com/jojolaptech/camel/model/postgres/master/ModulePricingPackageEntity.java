package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.master.enums.BillingCycleEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Master subscription catalog package (ERP {@code module_pricing_package}).
 * CostType / PayPlan migrate here — not into {@code hrm_cost_center}.
 */
@Entity
@Table(
        name = "module_pricing_package",
        uniqueConstraints = @UniqueConstraint(columnNames = {"package_code"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModulePricingPackageEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "package_code", nullable = false, length = 64)
    private String packageCode;

    @Column(name = "package_name", nullable = false)
    private String packageName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingCycleEnum billingCycle;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(columnDefinition = "TEXT")
    private String features;

    @Column(columnDefinition = "TEXT")
    private String description;
}
