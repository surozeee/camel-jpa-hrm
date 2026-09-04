package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.RateTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.SalaryBreakdownLineTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.SalaryBreakdownPercentBaseEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "hrm_branch_salary_breakdown")
public class BranchSalaryBreakdownEntity extends BaseAuditEntity {

    /** companyPayrollHeading.id, or PMS_HEADING_OFFSET + payrollHeading.id */
    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "company_id")
    private UUID companyId;

    /** Legacy per-branch; new rules are company-wide (null). */
    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "line_name", nullable = false)
    private String lineName;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_type", nullable = false)
    private SalaryBreakdownLineTypeEnum lineType;

    @Enumerated(EnumType.STRING)
    private RateTypeEnum rateType;

    @Column(name = "rate_value", precision = 15, scale = 2)
    private BigDecimal rateValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "percent_base")
    private SalaryBreakdownPercentBaseEnum percentBase;

    @Column(name = "is_basic_salary_line")
    private Boolean isBasicSalaryLine;

    private Integer displayOrder;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String remarks;

    private Boolean isTaxable;

    @Column(name = "applies_during_probation")
    private Boolean appliesDuringProbation;

    @Column(name = "applies_after_probation")
    private Boolean appliesAfterProbation;
}
