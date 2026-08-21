package com.jojolaptech.camel.model.postgres.user;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.user.enums.UserInviteStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_employee_invite", indexes = {
        @Index(name = "idx_uei_token", columnList = "token", unique = true),
        @Index(name = "idx_uei_employee", columnList = "employee_id"),
        @Index(name = "idx_uei_company_status", columnList = "company_id,invite_status"),
        @Index(name = "idx_uei_email", columnList = "email")
})
public class UserEmployeeInviteEntity extends BaseAuditEntity {

    @Column(name = "token", nullable = false, length = 64, unique = true)
    private String token;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "employee_code", length = 64)
    private String employeeCode;

    @Column(name = "employee_name", length = 255)
    private String employeeName;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "branch_name", length = 255)
    private String branchName;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "invite_status", nullable = false, length = 16)
    @Builder.Default
    private UserInviteStatusEnum inviteStatus = UserInviteStatusEnum.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "invited_by_user_id")
    private UUID invitedByUserId;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "resend_count", nullable = false)
    @Builder.Default
    private Integer resendCount = 0;
}
