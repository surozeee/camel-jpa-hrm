package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveDurationEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
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
@Table(name = "hrm_leave")
public class LeaveRequestEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "mysql_cancellation_id", unique = true)
    private Long mysqlCancellationId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_days")
    private Double totalDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_duration")
    private LeaveDurationEnum leaveDuration;

    @Column(name = "leave_type_id", nullable = false)
    private UUID leaveTypeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_status")
    private LeaveStatusEnum leaveStatus;

    @Column(length = 500)
    private String reason;

    @Column(length = 500)
    private String remarks;

    @Column(name = "supervisor_approved_by")
    private UUID supervisorApprovedBy;

    @Column(name = "supervisor_approved_date")
    private LocalDateTime supervisorApprovedDate;

    @Column(name = "hr_approved_by")
    private UUID hrApprovedBy;

    @Column(name = "hr_approved_date")
    private LocalDateTime hrApprovedDate;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "supervisor_rejected_by")
    private UUID supervisorRejectedBy;

    @Column(name = "supervisor_rejected_date")
    private LocalDateTime supervisorRejectedDate;

    @Column(name = "supervisor_rejection_reason", length = 500)
    private String supervisorRejectionReason;

    @Column(name = "hr_rejected_by")
    private UUID hrRejectedBy;

    @Column(name = "hr_rejected_date")
    private LocalDateTime hrRejectedDate;

    @Column(name = "hr_rejection_reason", length = 500)
    private String hrRejectionReason;

    @Column(name = "rejected_by")
    private UUID rejectedBy;

    @Column(name = "rejected_date")
    private LocalDateTime rejectedDate;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "substitute_employee_id")
    private UUID substituteEmployeeId;
}
