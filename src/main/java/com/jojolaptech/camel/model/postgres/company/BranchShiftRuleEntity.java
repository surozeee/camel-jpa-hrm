package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.company.enums.OvertimeSalaryBaseEnum;
import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hrm_branch_shift_rule", uniqueConstraints = @UniqueConstraint(columnNames = {"branch_shift_id"}))
public class BranchShiftRuleEntity extends BaseAuditEntity {

    @Column(name = "branch_shift_id", nullable = false)
    private UUID branchShiftId;

    private Integer lateArrivalToleranceMinutes;

    private Integer earlyDepartureToleranceMinutes;

    private Integer gracePeriodMinutes;

    private Integer minimumWorkingHours;

    private Boolean allowOvertime;

    private Integer maxOvertimeHoursPerDay;

    private Integer maxOvertimeHoursPerWeek;

    private Integer maxOvertimeHoursPerMonth;

    private Boolean requireBreak;

    private Integer maximumBreakDurationMinutes;

    @Column(precision = 5, scale = 2)
    private BigDecimal overtimeRateMultiplier;

    /**
     * When overtime is paid, multiplier applies to basic or gross (see {@link #overtimeRateMultiplier}).
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private OvertimeSalaryBaseEnum overtimeSalaryBase;

    /**
     * If true, a streak of late check-ins and/or late check-outs (per attendance rules) can trigger a
     * salary deduction of {@link #salaryDaysDeductedAfterConsecutiveLatePunches} after
     * {@link #consecutiveLatePunchCountForSalaryDeduction} consecutive qualifying punches.
     */
    private Boolean consecutiveLatePunchSalaryDeductionEnabled;

    /**
     * Number of <strong>consecutive</strong> late check-ins or late check-outs required before the deduction applies
     * (e.g. 3 means three consecutive qualifying late punches trigger one deduction).
     */
    private Integer consecutiveLatePunchCountForSalaryDeduction;

    /**
     * Salary expressed in day equivalents to deduct when the consecutive late punch threshold is reached
     * (e.g. 0.5 = half day, 1.0 = one day). Payroll/attendance services apply this to the pay run.
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal salaryDaysDeductedAfterConsecutiveLatePunches;

    /** Scheduled lunch break length (minutes). */
    private Integer lunchBreakDurationMinutes;

    /** Whether paid or unpaid tea break rules apply for this branch. */
    private Boolean teaBreakApplicable;

    /** Tea break duration in minutes when {@link #teaBreakApplicable} is true. */
    private Integer teaBreakDurationMinutes;

    /**
     * Minimum total worked minutes required in a month; if actual is below and deduction flag is on,
     * payroll may apply a salary adjustment (policy enforced in payroll/attendance services).
     */
    private Integer minimumMonthlyWorkingMinutes;

    /** If true and monthly worked minutes fall below {@link #minimumMonthlyWorkingMinutes}, salary deduction applies. */
    private Boolean deductSalaryIfBelowMinimumMonthlyMinutes;

    /**
     * Sandwich rule: leave on Friday and Monday treats intervening weekend as leave (common leave policy).
     */
    private Boolean sandwichLeaveApplicable;

    private Boolean allowFlexibleTiming;

    private Integer flexibleTimingWindowMinutes;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
}

