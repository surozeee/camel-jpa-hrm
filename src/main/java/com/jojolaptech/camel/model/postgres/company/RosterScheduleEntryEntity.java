package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.RosterShiftSlotEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "hrm_roster_schedule_entry",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_roster_schedule_entry_period_employee_date",
                        columnNames = {"period_id", "employee_id", "assignment_date"}))
public class RosterScheduleEntryEntity extends BaseAuditEntity {

    @Column(name = "period_id", nullable = false)
    private UUID periodId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "assignment_date", nullable = false)
    private LocalDate assignmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_slot", nullable = false)
    private RosterShiftSlotEnum shiftSlot;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "off_day")
    private Boolean offDay;

    @Column(length = 500)
    private String remarks;
}
