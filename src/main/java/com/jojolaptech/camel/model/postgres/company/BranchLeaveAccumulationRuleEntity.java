package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.AccumulationPeriodEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveAccumulationScopeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveAccumulationUnitEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveRoundRuleEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hrm_branch_leave_accumulation_rule")
public class BranchLeaveAccumulationRuleEntity extends BaseAuditEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_level", nullable = false, length = 32)
    @Builder.Default
    private LeaveAccumulationScopeEnum scopeLevel = LeaveAccumulationScopeEnum.BRANCH;

    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "grade_id")
    private UUID gradeId;

    @Column(name = "grade_code", length = 64)
    private String gradeCode;

    @Column(name = "designation_id")
    private UUID designationId;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "accumulation_enabled", nullable = false)
    @Builder.Default
    private Boolean accumulationEnabled = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private LeaveAccumulationUnitEnum unit = LeaveAccumulationUnitEnum.DAYS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccumulationPeriodEnum accumulationPeriod;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal leaveDays;

    private Integer maxAccumulationDays;

    private Boolean allowCarryForward;

    private Integer maxCarryForwardDays;

    private Boolean resetAtYearEnd;

    private Boolean prorateForNewJoiners;

    private Boolean prorateForTerminations;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "require_confirmation", nullable = false)
    @Builder.Default
    private Boolean requireConfirmation = false;

    @Column(name = "min_working_days")
    private Integer minWorkingDays;

    @Column(name = "exclude_leave_without_pay", nullable = false)
    @Builder.Default
    private Boolean excludeLeaveWithoutPay = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "round_rule", nullable = false, length = 32)
    @Builder.Default
    private LeaveRoundRuleEnum roundRule = LeaveRoundRuleEnum.NEAREST_HALF;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "branch_leave_type_id", nullable = false)
    private BranchLeaveTypeEntity branchLeaveType;
}
