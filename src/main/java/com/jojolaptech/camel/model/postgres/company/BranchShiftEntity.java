package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
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
@Table(name = "hrm_branch_shift")
public class BranchShiftEntity extends BaseAuditEntity {

    /** Legacy {@code attTimeTable.id} when migrated from timetable. */
    @Column(name = "mysql_id")
    private Long mysqlId;

    @Column(name = "mysql_branch_id")
    private Long mysqlBranchId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String code;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private Integer breakDurationMinutes;

    private Integer workingHours;

    @Column(length = 500)
    private String description;

    private Boolean isFlexible;

    private Boolean isNightShift;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "effective_start_date")
    private LocalDate effectiveStartDate;

    @Column(name = "effective_end_date")
    private LocalDate effectiveEndDate;
}

