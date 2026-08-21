package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.JoiningTaskDepartment;
import com.jojolaptech.camel.model.postgres.company.enums.JoiningTaskOwnerType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "hrm_joining_checklist_template", indexes = {
        @Index(name = "idx_jct_company", columnList = "company_id")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JoiningChecklistTemplateEntity extends BaseAuditEntity {

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JoiningTaskDepartment department;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 30)
    @Builder.Default
    private JoiningTaskOwnerType ownerType = JoiningTaskOwnerType.UNASSIGNED;

    @Column(name = "due_days_from_joining")
    @Builder.Default
    private Integer dueDaysFromJoining = 0;

    @Column(name = "requires_evidence", nullable = false)
    @Builder.Default
    private Boolean requiresEvidence = Boolean.FALSE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean mandatory = Boolean.TRUE;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;
}
