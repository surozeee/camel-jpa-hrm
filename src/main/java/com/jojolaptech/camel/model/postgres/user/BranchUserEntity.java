package com.jojolaptech.camel.model.postgres.user;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Branch admin portal linkage stored separately from generic {@link UserDetailEntity} profile fields.
 */
@Entity
@Table(
        name = "branch_user_detail",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_branch_user_detail_user_id", columnNames = "user_id")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BranchUserEntity extends BaseAuditEntity {

    // No cascade: user rows already exist; PERSIST on a detached UserEntity fails migration saves.
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "company_id")
    private UUID companyId;
}
