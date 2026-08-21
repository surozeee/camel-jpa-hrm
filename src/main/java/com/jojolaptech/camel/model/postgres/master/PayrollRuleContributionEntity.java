package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.master.enums.TaxRateTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payroll_rule_contribution")
public class PayrollRuleContributionEntity extends BaseAuditEntity {

    /**
     * Contribution kind code: {@code PF}, {@code SSF}, {@code CIT}, or a custom code.
     * Optional per company — omit rows the company does not use.
     */
    @Column(name = "contribution_code", nullable = false, length = 40)
    @Builder.Default
    private String contributionCode = "PF";

    @Column(name = "contribution_name", nullable = false, length = 120)
    @Builder.Default
    private String contributionName = "Provident Fund";

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    /** When true, employee share counts toward retirement tax-free redemption. */
    @Column(name = "counts_toward_tax_free", nullable = false)
    @Builder.Default
    private boolean countsTowardTaxFree = true;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "company_type", nullable = false, length = 32)
    @Builder.Default
    private TaxRateTypeEnum companyType = TaxRateTypeEnum.PERCENTAGE;

    @Column(name = "company_value", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal companyValue = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", nullable = false, length = 32)
    @Builder.Default
    private TaxRateTypeEnum employeeType = TaxRateTypeEnum.PERCENTAGE;

    @Column(name = "employee_value", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal employeeValue = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payroll_rule_id", nullable = false)
    private PayrollRuleEntity payrollRule;
}
