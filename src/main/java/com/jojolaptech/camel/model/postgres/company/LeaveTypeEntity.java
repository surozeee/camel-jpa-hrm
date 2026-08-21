package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hrm_leave_type")
public class LeaveTypeEntity extends BaseAuditEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Boolean isPaid;

    private Boolean compensationType;

    private Boolean requiresApproval;

    private Boolean requiresMedicalCertificate;

    private Integer maxDaysPerYear;

    private Integer maxDaysPerRequest;

    private Boolean canCarryForward;

    private Integer maxCarryForwardDays;

    private Integer displayOrder;

    @OneToMany(mappedBy = "leaveType", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private List<BranchLeaveTypeEntity> branchLeaveTypes;
}

