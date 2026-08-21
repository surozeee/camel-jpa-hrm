package com.jojolaptech.camel.model.postgres.user;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.user.enums.PermissionForEnum;
import com.jojolaptech.camel.model.postgres.user.enums.TrueFalseEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "permission_group")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionGroupEntity extends BaseAuditEntity {

    private String name;
    private String code;
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope")
    private PermissionForEnum scope;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "company_id")
    private UUID companyId;

    @Column(name = "branch_id")
    private UUID branchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "has_sub_child")
    @Builder.Default
    private TrueFalseEnum hasSubChild = TrueFalseEnum.FALSE;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "parent_id")
    private PermissionGroupEntity parent;

    @OneToMany(mappedBy = "parent", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private List<PermissionGroupEntity> children;

    @OneToMany(mappedBy = "permissionGroup", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private List<PermissionEntity> permissions;
}
