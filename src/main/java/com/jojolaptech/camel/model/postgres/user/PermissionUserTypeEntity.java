package com.jojolaptech.camel.model.postgres.user;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.user.enums.UserTypeEnum;
import jakarta.persistence.*;
import lombok.*;

/**
 * Catalog entry linking a permission to an admin portal type (e.g. SUPER_ADMIN vs COMPANY_ADMIN).
 */
@Entity
@Table(
        name = "permission_user_type",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_permission_user_type_permission_user_type",
                columnNames = {"permission_id", "user_type"}
        )
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionUserTypeEntity extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private PermissionEntity permission;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserTypeEnum userType;
}
