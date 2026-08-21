package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hrm_branch_roster_settings")
public class BranchRosterSettingsEntity extends BaseAuditEntity {

    @Column(name = "branch_id", nullable = false, unique = true)
    private UUID branchId;

    @Column(name = "require_approval")
    private Boolean requireApproval;

    @Column(name = "allow_employee_shift_change_request")
    private Boolean allowEmployeeShiftChangeRequest;

    @Column(name = "allow_update_when_attendance_exists")
    private Boolean allowUpdateWhenAttendanceExists;

    @Column(length = 500)
    private String remarks;

    @Column(name = "shift_slot_presets", columnDefinition = "TEXT")
    private String shiftSlotPresets;

    /** Minutes before roster shift start allowed for check-in capture. Default 120. */
    @Column(name = "roster_capture_before_minutes")
    private Integer rosterCaptureBeforeMinutes;

    /** Minutes after roster shift end allowed for checkout capture. Default 120. */
    @Column(name = "roster_capture_after_minutes")
    private Integer rosterCaptureAfterMinutes;

    /** When false, one punch cannot be reused as next shift check-in. */
    @Column(name = "roster_allow_punch_reuse")
    private Boolean rosterAllowPunchReuse;

    /** Ignore punches within this many seconds of the previous punch. Default 60. */
    @Column(name = "duplicate_punch_interval_seconds")
    private Integer duplicatePunchIntervalSeconds;

    /** When true, a single punch in the capture window satisfies attendance (in = out). */
    @Column(name = "single_punch_mode")
    private Boolean singlePunchMode;

    /** When true, break-out / break-in logs reduce worked time. */
    @Column(name = "enable_break_tracking")
    private Boolean enableBreakTracking;

    /** When false, new punches are stored but attendance summary is not auto-recalculated. */
    @Column(name = "auto_recalculate_attendance")
    private Boolean autoRecalculateAttendance;
}
