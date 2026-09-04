package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.enums.CurrencyEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "hrm_branch_employee_month_wise_salary",
        uniqueConstraints =
                @UniqueConstraint(columnNames = {"branch_id", "employee_id", "salary_month", "salary_year"}))
public class BranchEmployeeMonthWiseSalaryEntity extends BaseAuditEntity {

    /** employeePayrollPayment.id, or TRANSACTION_OFFSET + aggregate key */
    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "salary_year", nullable = false)
    private Integer salaryYear;

    @Column(name = "salary_month", nullable = false)
    private Integer salaryMonth;

    @Column(name = "gross_salary", nullable = false, precision = 19, scale = 2)
    private BigDecimal grossSalary;

    @Column(name = "net_salary", precision = 19, scale = 2)
    private BigDecimal netSalary;

    @Column(name = "basic_salary", precision = 19, scale = 2)
    private BigDecimal basicSalary;

    @Column(name = "total_allowances", precision = 19, scale = 2)
    private BigDecimal totalAllowances;

    @Column(name = "total_deductions", precision = 19, scale = 2)
    private BigDecimal totalDeductions;

    @Column(name = "total_tax", precision = 19, scale = 2)
    private BigDecimal totalTax;

    @Column(name = "tax_free_redemption", precision = 19, scale = 2)
    private BigDecimal taxFreeRedemption;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency")
    private CurrencyEnum currency;

    @Column(name = "is_processed", nullable = false)
    @Builder.Default
    private Boolean isProcessed = false;

    @Column(name = "is_paid", nullable = false)
    @Builder.Default
    private Boolean isPaid = false;

    @Column(name = "payroll_rule_id")
    private UUID payrollRuleId;

    @Column(name = "client_request_id", length = 100)
    private String clientRequestId;

    @Column(name = "calculation_engine_version", length = 40)
    private String calculationEngineVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "calculation_snapshot", columnDefinition = "jsonb")
    private PayrollCalculationSnapshot calculationSnapshot;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String remarks;
}
