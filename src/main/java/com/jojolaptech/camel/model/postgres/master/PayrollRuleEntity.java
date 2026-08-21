package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.master.enums.SalaryBaseEnum;
import com.jojolaptech.camel.model.postgres.master.enums.TaxRateTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "payroll_rule",
        uniqueConstraints = @UniqueConstraint(columnNames = {"fiscal_year_id"})
)
public class PayrollRuleEntity extends BaseAuditEntity {

    @Column(name = "fiscal_year_id", nullable = false)
    private UUID fiscalYearId;

    /** Legacy; synced from taxFreePercentage on save */
    @Deprecated
    @Enumerated(EnumType.STRING)
    @Column(name = "tax_free_total_type", nullable = false, length = 32)
    @Builder.Default
    private TaxRateTypeEnum taxFreeTotalType = TaxRateTypeEnum.PERCENTAGE;

    /** Legacy; synced from taxFreePercentage on save */
    @Deprecated
    @Column(name = "tax_free_total", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal taxFreeTotal = BigDecimal.ZERO;

    @Column(name = "tax_free_percentage", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal taxFreePercentage = BigDecimal.ZERO;

    @Column(name = "tax_free_flat_cap", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal taxFreeFlatCap = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_free_salary_base", nullable = false, length = 32)
    @Builder.Default
    private SalaryBaseEnum taxFreeSalaryBase = SalaryBaseEnum.GROSS;

    @Enumerated(EnumType.STRING)
    @Column(name = "cit_max_limit_type", nullable = false, length = 32)
    @Builder.Default
    private TaxRateTypeEnum citMaxLimitType = TaxRateTypeEnum.FLAT;

    @Column(name = "cit_max_limit", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal citMaxLimit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "cit_salary_base", length = 32)
    private SalaryBaseEnum citSalaryBase;

    @Enumerated(EnumType.STRING)
    @Column(name = "ssf_max_limit_type", nullable = false, length = 32)
    @Builder.Default
    private TaxRateTypeEnum ssfMaxLimitType = TaxRateTypeEnum.FLAT;

    @Column(name = "ssf_max_limit", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal ssfMaxLimit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "ssf_salary_base", length = 32)
    private SalaryBaseEnum ssfSalaryBase;

    @Column(name = "festival_allowance_taxable", nullable = false)
    @Builder.Default
    private boolean festivalAllowanceTaxable = true;

    @Column(name = "bonus_taxable", nullable = false)
    @Builder.Default
    private boolean bonusTaxable = true;

    @Column(name = "overtime_taxable", nullable = false)
    @Builder.Default
    private boolean overtimeTaxable = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "insurance_type", nullable = false, length = 32)
    @Builder.Default
    private TaxRateTypeEnum insuranceType = TaxRateTypeEnum.FLAT;

    @Column(name = "insurance_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal insuranceAmount = BigDecimal.ZERO;

    @Column(name = "insurance_tax_free", nullable = false)
    @Builder.Default
    private boolean insuranceTaxFree = true;

    /** Employee external tax relief — life insurance annual cap (NPR). */
    @Column(name = "life_insurance_relief_enabled", nullable = false)
    @Builder.Default
    private boolean lifeInsuranceReliefEnabled = true;

    @Column(name = "life_insurance_annual_limit", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal lifeInsuranceAnnualLimit = new BigDecimal("40000");

    @Column(name = "life_insurance_eligible_percent", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal lifeInsuranceEligiblePercent = new BigDecimal("100");

    @Column(name = "health_insurance_relief_enabled", nullable = false)
    @Builder.Default
    private boolean healthInsuranceReliefEnabled = true;

    @Column(name = "health_insurance_annual_limit", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal healthInsuranceAnnualLimit = new BigDecimal("20000");

    @Column(name = "health_insurance_eligible_percent", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal healthInsuranceEligiblePercent = new BigDecimal("100");

    @Column(name = "building_insurance_relief_enabled", nullable = false)
    @Builder.Default
    private boolean buildingInsuranceReliefEnabled = true;

    @Column(name = "building_insurance_annual_limit", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal buildingInsuranceAnnualLimit = new BigDecimal("10000");

    @Column(name = "building_insurance_eligible_percent", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal buildingInsuranceEligiblePercent = new BigDecimal("100");

    @Column(name = "child_education_relief_enabled", nullable = false)
    @Builder.Default
    private boolean childEducationReliefEnabled = true;

    @Column(name = "child_education_eligible_percent", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal childEducationEligiblePercent = new BigDecimal("25");

    @Column(name = "child_education_annual_limit", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal childEducationAnnualLimit = new BigDecimal("25000");

    /** Per-relief supporting document flags (overrides global when set). */
    @Column(name = "life_insurance_proof_required", nullable = false)
    @Builder.Default
    private boolean lifeInsuranceProofRequired = true;

    @Column(name = "health_insurance_proof_required", nullable = false)
    @Builder.Default
    private boolean healthInsuranceProofRequired = true;

    @Column(name = "building_insurance_proof_required", nullable = false)
    @Builder.Default
    private boolean buildingInsuranceProofRequired = true;

    @Column(name = "child_education_proof_required", nullable = false)
    @Builder.Default
    private boolean childEducationProofRequired = true;

    /**
     * Legacy aggregate flag — kept in sync as OR of enabled per-type proof flags
     * for older clients.
     */
    @Column(name = "tax_relief_proof_required", nullable = false)
    @Builder.Default
    private boolean taxReliefProofRequired = true;

    @Column(name = "female_employment_rebate_enabled", nullable = false)
    @Builder.Default
    private boolean femaleEmploymentRebateEnabled = true;

    @Column(name = "female_employment_rebate_percent", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal femaleEmploymentRebatePercent = new BigDecimal("10");

    /**
     * When true, female rebate applies only if employee tax profile marks
     * {@code femaleTaxRebateEligible} (not gender alone).
     */
    @Column(name = "female_rebate_requires_eligibility", nullable = false)
    @Builder.Default
    private boolean femaleRebateRequiresEligibility = true;

    @Column(name = "description", length = 2000)
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "payrollRule", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, createdAt ASC")
    private List<PayrollRuleContributionEntity> pfContributions = new ArrayList<>();

    /**
     * Loaded separately from {@code pfContributions} — Hibernate cannot JOIN FETCH two bags.
     */
    @Builder.Default
    @OneToMany(mappedBy = "payrollRule", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, createdAt ASC")
    @org.hibernate.annotations.BatchSize(size = 32)
    private List<PayrollRuleTaxReliefEntity> taxReliefs = new ArrayList<>();
}
