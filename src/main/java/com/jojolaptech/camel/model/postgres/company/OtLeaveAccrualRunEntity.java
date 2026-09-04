package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.OtLeaveAccrualRunStatusEnum;
import com.jojolaptech.camel.model.postgres.company.enums.OtLeaveAccrualSourceEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "hrm_ot_leave_accrual_run")
public class OtLeaveAccrualRunEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "run_status", nullable = false)
    private OtLeaveAccrualRunStatusEnum runStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "ot_source", nullable = false)
    private OtLeaveAccrualSourceEnum otSource;

    @Column(name = "hours_equivalent_to_one_day", nullable = false, precision = 8, scale = 2)
    private BigDecimal hoursEquivalentToOneDay;

    @Column(name = "max_leave_days", precision = 12, scale = 4)
    private BigDecimal maxLeaveDays;

    @Column(name = "leave_type_id", nullable = false)
    private UUID leaveTypeId;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(name = "credited_employee_count")
    private Integer creditedEmployeeCount;

    @Column(name = "total_leave_days", precision = 12, scale = 4)
    private BigDecimal totalLeaveDays;
}
