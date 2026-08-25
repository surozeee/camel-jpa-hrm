package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.company.enums.FiscalCalendarTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveExpiryPolicyEnum;
import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveApprovalSeparationPolicy;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveCreditTimingEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveProcessingMode;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveVerificationMode;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hrm_leave_policy")
public class LeavePolicyEntity extends BaseAuditEntity {

    @Column(name = "mysql_branch_id", unique = true)
    private Long mysqlBranchId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "policy_name", nullable = false)
    private String policyName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "enable_leave_accumulation")
    private Boolean enableLeaveAccumulation;

    @Enumerated(EnumType.STRING)
    @Column(name = "calendar_type")
    private FiscalCalendarTypeEnum calendarType;

    @Column(name = "enable_automatic_accrual")
    private Boolean enableAutomaticAccrual;

    @Column(name = "enable_carry_forward")
    private Boolean enableCarryForward;

    @Column(name = "enable_leave_expiry")
    private Boolean enableLeaveExpiry;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_expiry_policy")
    private LeaveExpiryPolicyEnum leaveExpiryPolicy;

    @Column(name = "min_service_days_before_accrual")
    private Integer minServiceDaysBeforeAccrual;

    @Column(name = "require_probation_complete")
    private Boolean requireProbationComplete;

    @Column(name = "allow_negative_balance")
    private Boolean allowNegativeBalance;

    @Column(name = "max_negative_balance_days")
    private Integer maxNegativeBalanceDays;

    @Column(name = "enable_notifications")
    private Boolean enableNotifications;

    /** Unified leave credit workflow (accumulation / assignment / balance update). */
    @Enumerated(EnumType.STRING)
    @Column(name = "accumulation_processing_mode", length = 32)
    private LeaveProcessingMode accumulationProcessingMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "accumulation_verification_mode", length = 32)
    private LeaveVerificationMode accumulationVerificationMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_processing_mode", length = 32)
    private LeaveProcessingMode assignmentProcessingMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_verification_mode", length = 32)
    private LeaveVerificationMode assignmentVerificationMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "balance_update_processing_mode", length = 32)
    private LeaveProcessingMode balanceUpdateProcessingMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "balance_update_verification_mode", length = 32)
    private LeaveVerificationMode balanceUpdateVerificationMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_separation_policy", length = 32)
    private LeaveApprovalSeparationPolicy approvalSeparationPolicy;

    @Column(name = "allow_self_approval")
    private Boolean allowSelfApproval;

    @Column(name = "allow_bulk_verification")
    private Boolean allowBulkVerification;

    @Column(name = "allow_bulk_approval")
    private Boolean allowBulkApproval;

    @Enumerated(EnumType.STRING)
    @Column(name = "accumulation_credit_timing", length = 32)
    private LeaveCreditTimingEnum accumulationCreditTiming;

    @Column(length = 500)
    private String remarks;
}
