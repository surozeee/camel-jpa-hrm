package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Dynamic employee tax-relief row (Life / Health / Building / Education / custom).
 * Eligible amount = min(paid × eligiblePercent / 100, annualFlatCap).
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payroll_rule_tax_relief")
public class PayrollRuleTaxReliefEntity extends BaseAuditEntity {

    /** LIFE, HEALTH, BUILDING, CHILD_EDU, or custom code. */
    @Column(name = "relief_code", nullable = false, length = 40)
    private String reliefCode;

    @Column(name = "relief_name", nullable = false, length = 120)
    private String reliefName;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    /** Eligible % of paid amount (100 = full amount up to flat cap). */
    @Column(name = "eligible_percent", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal eligiblePercent = new BigDecimal("100");

    /** Annual flat amount cap (NPR). */
    @Column(name = "annual_flat_cap", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal annualFlatCap = BigDecimal.ZERO;

    @Column(name = "proof_required", nullable = false)
    @Builder.Default
    private boolean proofRequired = true;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payroll_rule_id", nullable = false)
    private PayrollRuleEntity payrollRule;
}
