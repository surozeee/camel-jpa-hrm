package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "hrm_branch_shift_weekday",
        uniqueConstraints = @UniqueConstraint(columnNames = {"branch_shift_id", "day_of_week"}))
public class BranchShiftWeekdayEntity extends BaseAuditEntity {

    @Column(name = "branch_shift_id", nullable = false)
    private UUID branchShiftId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 16)
    private DayOfWeek dayOfWeek;

    @Column(name = "is_off", nullable = false)
    private Boolean off;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "break_duration_minutes")
    private Integer breakDurationMinutes;
}
