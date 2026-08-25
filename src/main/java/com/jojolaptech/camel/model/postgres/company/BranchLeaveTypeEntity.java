package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hrm_branch_leave_type", uniqueConstraints = @UniqueConstraint(columnNames = {"branch_id", "leave_type_id"}))
public class BranchLeaveTypeEntity extends BaseAuditEntity {

    @Column(name = "mysql_branch_id")
    private Long mysqlBranchId;

    @Column(name = "mysql_leave_id")
    private Long mysqlLeaveId;

    private Boolean isPaid;

    private Boolean requiresApproval;

    private Boolean requiresMedicalCertificate;

    private Integer maxDaysPerYear;

    private Integer minDaysPerRequest;

    private Integer maxDaysPerRequest;

    private Boolean canCarryForward;

    private Integer maxCarryForwardDays;

    @Column(columnDefinition = "TEXT")
    private String branchSpecificDescription;

    @Column(length = 500)
    private String remarks;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveTypeEntity leaveType;

    @OneToMany(mappedBy = "branchLeaveType", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private List<BranchLeaveAccumulationRuleEntity> accumulationRules;
}

