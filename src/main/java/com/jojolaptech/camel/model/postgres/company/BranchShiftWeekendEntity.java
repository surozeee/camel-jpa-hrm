package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "hrm_branch_shift_weekend",
        uniqueConstraints = @UniqueConstraint(columnNames = {"branch_shift_id", "day_of_week"}))
public class BranchShiftWeekendEntity extends BaseAuditEntity {

    @Column(name = "branch_shift_id", nullable = false)
    private UUID branchShiftId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 16)
    private DayOfWeek dayOfWeek;
}
