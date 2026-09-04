package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.PayrollOpeningBalanceTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "hrm_payroll_opening_balance_line")
public class PayrollOpeningBalanceLineEntity extends BaseAuditEntity {

    /** openingPayrollBalance.id */
    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opening_balance_id", nullable = false)
    private PayrollOpeningBalanceEntity openingBalance;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "balance_type", nullable = false, length = 40)
    private PayrollOpeningBalanceTypeEnum balanceType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 200)
    private String label;

    @Column(name = "branch_salary_breakdown_id")
    private UUID branchSalaryBreakdownId;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
