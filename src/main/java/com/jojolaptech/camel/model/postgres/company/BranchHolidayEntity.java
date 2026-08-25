package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hrm_branch_holiday")
public class BranchHolidayEntity extends BaseAuditEntity {

    @Column(name = "mysql_id")
    private Long mysqlId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate holidayDate;

    private String description;

    @Column(length = 500)
    private String remarks;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
}

