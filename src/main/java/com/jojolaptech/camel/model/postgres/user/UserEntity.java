package com.jojolaptech.camel.model.postgres.user;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.user.enums.AuthProviderEnum;
import com.jojolaptech.camel.model.postgres.user.enums.UserStatusEnum;
import com.jojolaptech.camel.model.postgres.user.enums.UserTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = {"email_address", "mobile_number"}))
public class UserEntity extends BaseAuditEntity {

    @Column(unique = true, nullable = false)
    private String emailAddress;

    @Column(length = 32)
    private String mobileNumber;

    @Column(nullable = false)
    private String password;

    @Column(name = "is_account_non_expired", nullable = false)
    @Builder.Default
    private boolean accountNonExpired = true;

    @Column(name = "is_account_non_locked", nullable = false)
    @Builder.Default
    private boolean accountNonLocked = true;

    @Column(name = "is_credentials_non_expired", nullable = false)
    @Builder.Default
    private boolean credentialsNonExpired = true;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private UserStatusEnum userStatus = UserStatusEnum.ACTIVE;

    /** Assigned roles (join table {@code user_role}). */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private List<RoleEntity> roles = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "user_type")
    @Builder.Default
    private List<UserTypeEnum> userType = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", length = 32)
    private AuthProviderEnum authProvider;

    /** Stable provider subject (Google {@code sub} / Facebook {@code id}). */
    @Column(name = "provider_id", length = 128)
    private String providerId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserDeviceEntity> devices = new ArrayList<>();

}
