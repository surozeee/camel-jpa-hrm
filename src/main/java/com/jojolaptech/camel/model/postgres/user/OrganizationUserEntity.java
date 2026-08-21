package com.jojolaptech.camel.model.postgres.user;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Organization admin portal linkage stored separately from generic {@link UserDetailEntity} profile fields.
 */
@Entity
@Table(
        name = "organization_user_detail",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_organization_user_detail_user_id", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_organization_user_detail_organization_id", columnNames = "organization_id")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrganizationUserEntity extends BaseAuditEntity {

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "organization_id", nullable = false, unique = true)
    private UUID organizationId;
}
