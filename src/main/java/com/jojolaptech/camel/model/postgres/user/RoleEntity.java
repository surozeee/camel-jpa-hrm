package com.jojolaptech.camel.model.postgres.user;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import com.jojolaptech.camel.model.postgres.user.enums.PermissionForEnum;
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
@Table(name = "role")
public class RoleEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope")
    private PermissionForEnum scope;

    @Column(name = "organization_id")
    private UUID organizationId;

    /** Set when a company admin creates a custom role; null for platform seed roles. */
    @Column(name = "company_id")
    private UUID companyId;

    /** Optional branch scope when a branch admin creates a role. */
    @Column(name = "branch_id")
    private UUID branchId;

    /** Lazy: eager permissions made multi-role user updates load huge join graphs and time out (504). */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id", referencedColumnName = "id")
    )
    private List<PermissionEntity> permissions;
}
