package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceApprovalStatusEnum;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceCalculationModeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceSourceEnum;
import com.jojolaptech.camel.model.postgres.company.enums.AttendanceStatusEnum;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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
@Table(
        name = "hrm_attendance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "attendance_date"}))
public class AttendanceEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    @Column(name = "total_working_hours")
    private Integer totalWorkingHours;

    @Column(name = "total_break_minutes")
    private Integer totalBreakMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status")
    private AttendanceStatusEnum attendanceStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    private AttendanceSourceEnum source;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private AttendanceApprovalStatusEnum approvalStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_mode")
    private AttendanceCalculationModeEnum calculationMode;

    @Column(length = 1000)
    private String remarks;

    @Column(name = "overtime_override_minutes")
    private Integer overtimeOverrideMinutes;

    @Builder.Default
    @Column(name = "overtime_manually_edited", nullable = false)
    private Boolean overtimeManuallyEdited = false;

    @Column(name = "device_mac_address")
    private String deviceMacAddress;

    @Column(name = "device_name")
    private String deviceName;

    @Builder.Default
    @OneToMany(mappedBy = "attendance", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    private List<AttendanceLogEntity> logs = new ArrayList<>();
}
