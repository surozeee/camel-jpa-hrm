package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.RateTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.SalaryBreakdownLineTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.SalaryBreakdownPercentBaseEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "hrm_employee_salary_component",
        uniqueConstraints =
                @UniqueConstraint(columnNames = {"employee_id", "branch_salary_breakdown_id", "effective_date"}))
public class EmployeeSalaryComponentEntity extends BaseAuditEntity {

    /** employeePayrollHeading.id */
    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(length = 500)
    private String remarks;

    @Column(name = "line_name", nullable = false)
    private String lineName;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_type", nullable = false)
    private SalaryBreakdownLineTypeEnum lineType;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type")
    private RateTypeEnum rateType;

    @Column(name = "rate_value", precision = 15, scale = 2)
    private BigDecimal rateValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "percent_base")
    private SalaryBreakdownPercentBaseEnum percentBase;

    @Column(name = "is_basic_salary_line")
    private Boolean isBasicSalaryLine;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_taxable")
    private Boolean isTaxable;

    @Column(name = "applies_during_probation")
    private Boolean appliesDuringProbation;

    @Column(name = "applies_after_probation")
    private Boolean appliesAfterProbation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_salary_id")
    private EmployeeSalaryEntity employeeSalary;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_salary_breakdown_id", nullable = false)
    private BranchSalaryBreakdownEntity branchSalaryBreakdown;
}
