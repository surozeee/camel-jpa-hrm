package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.RosterScheduleStatusEnum;
import com.jojolaptech.camel.model.postgres.company.enums.RosterScheduleTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hrm_roster_schedule_period")
public class RosterSchedulePeriodEntity extends BaseAuditEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "roster_name", nullable = false)
    private String rosterName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "roster_type", nullable = false)
    private RosterScheduleTypeEnum rosterType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "roster_status", nullable = false)
    private RosterScheduleStatusEnum rosterStatus;

    @Column(name = "require_approval")
    private Boolean requireApproval;

    @Column(name = "supervisor_employee_id")
    private UUID supervisorEmployeeId;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "published_by")
    private UUID publishedBy;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(length = 500)
    private String remarks;

    /** Comma-separated employee UUIDs pending notification after republish. */
    @Column(name = "pending_notify_employee_ids", columnDefinition = "TEXT")
    private String pendingNotifyEmployeeIds;
}
