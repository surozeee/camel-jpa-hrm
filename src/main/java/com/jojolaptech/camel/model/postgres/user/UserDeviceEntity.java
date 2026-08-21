package com.jojolaptech.camel.model.postgres.user;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.user.enums.PlatformEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Device registered for a user (e.g. on login). One user can have multiple devices.
 * Identified by user + deviceId; FCM token and metadata updated on each login.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_device", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "device_id"}))
public class UserDeviceEntity extends BaseAuditEntity {

    @Column(name = "device_id", nullable = false, length = 255)
    private String deviceId;

    @Column(name = "fcm_token", length = 512)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PlatformEnum platform;

    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Column(name = "os_version", length = 100)
    private String osVersion;

    @Column(name = "app_version", length = 50)
    private String appVersion;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
