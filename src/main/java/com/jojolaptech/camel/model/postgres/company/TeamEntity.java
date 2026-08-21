package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "hrm_team")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamEntity extends BaseAuditEntity {

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "department_id", nullable = false)
    private UUID departmentId;

    @Column(name = "leader_employee_id")
    private UUID leaderEmployeeId;
}
