package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "hrm_department",
        uniqueConstraints = @UniqueConstraint(columnNames = {"mysql_id", "mysql_branch_id"})
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DepartmentEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", nullable = false)
    private Long mysqlId;

    @Column(name = "mysql_branch_id", nullable = false)
    private Long mysqlBranchId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * When true, this department is a last/final node (no sub-departments allowed; typical employee assignment level).
     * Persisted as {@code final_child}. Forced to {@code false} when active child departments exist.
     */
    @Column(name = "final_child")
    private Boolean isLeafDepartment;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    /** Denormalized; must match User-Service branch → company. */
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "division_id")
    private UUID divisionId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "parent_department_id")
    private DepartmentEntity parentDepartment;

    @OneToMany(mappedBy = "parentDepartment", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private List<DepartmentEntity> childDepartments;
}
