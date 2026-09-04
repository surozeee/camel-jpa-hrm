package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveOpeningAdjustmentActionEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
@Table(name = "hrm_leave_opening_adjustment")
public class LeaveOpeningAdjustmentEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "leave_type_id", nullable = false)
    private UUID leaveTypeId;

    @Column(name = "leave_balance_id")
    private UUID leaveBalanceId;

    @Column(name = "company_fiscal_year_id")
    private UUID companyFiscalYearId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private LeaveOpeningAdjustmentActionEnum action;

    @Column(name = "days")
    private Double days;

    @Column(name = "previous_total_leaves")
    private Double previousTotalLeaves;

    @Column(name = "new_total_leaves")
    private Double newTotalLeaves;

    @Column(name = "previous_opening_leaves")
    private Double previousOpeningLeaves;

    @Column(name = "new_opening_leaves")
    private Double newOpeningLeaves;

    @Column(length = 500)
    private String remarks;

    @Column(name = "adjusted_at")
    private LocalDateTime adjustedAt;
}
