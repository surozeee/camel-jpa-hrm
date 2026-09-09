package com.jojolaptech.camel.model.postgres.user;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Company admin portal linkage stored separately from generic {@link UserDetailEntity} profile fields.
 */
@Entity
@Table(
        name = "company_user_detail",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_company_user_detail_user_id", columnNames = "user_id")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CompanyUserEntity extends BaseAuditEntity {

    // No cascade: user rows already exist; PERSIST on a detached UserEntity fails migration saves.
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;
}
