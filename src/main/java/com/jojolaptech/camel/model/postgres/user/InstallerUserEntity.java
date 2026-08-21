package com.jojolaptech.camel.model.postgres.user;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Installer portal linkage: assigned company and branch for device MAC and enrollment setup.
 */
@Entity
@Table(
        name = "installer_user_detail",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_installer_user_detail_user_id", columnNames = "user_id")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class InstallerUserEntity extends BaseAuditEntity {

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
}
