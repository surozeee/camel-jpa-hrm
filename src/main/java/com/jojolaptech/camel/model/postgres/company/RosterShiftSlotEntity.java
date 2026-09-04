package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.RosterShiftSlotEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "hrm_roster_shift_slot",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_roster_shift_slot_branch_slot",
                        columnNames = {"branch_id", "shift_slot"}))
public class RosterShiftSlotEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_slot", nullable = false, length = 32)
    private RosterShiftSlotEnum shiftSlot;

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Builder.Default
    @Column(name = "enabled")
    private Boolean enabled = true;
}
