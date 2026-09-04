package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Company announcement composed by company/branch admin (email / SMS / in-app).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hrm_company_notice")
public class CompanyNoticeEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    /** Legacy single branch; prefer {@link #branchIds}. Null + empty list = whole company. */
    @Column(name = "branch_id")
    private UUID branchId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hrm_company_notice_branch", joinColumns = @JoinColumn(name = "notice_id"))
    @Column(name = "branch_id")
    @Builder.Default
    private List<UUID> branchIds = new ArrayList<>();

    @Column(name = "department_id")
    private UUID departmentId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hrm_company_notice_department", joinColumns = @JoinColumn(name = "notice_id"))
    @Column(name = "department_id")
    @Builder.Default
    private List<UUID> departmentIds = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hrm_company_notice_employee", joinColumns = @JoinColumn(name = "notice_id"))
    @Column(name = "employee_id")
    @Builder.Default
    private List<UUID> employeeIds = new ArrayList<>();

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "published_by")
    private UUID publishedBy;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "send_email")
    @Builder.Default
    private Boolean sendEmail = Boolean.TRUE;

    @Column(name = "send_sms")
    @Builder.Default
    private Boolean sendSms = Boolean.FALSE;

    @Column(name = "send_notification")
    @Builder.Default
    private Boolean sendNotification = Boolean.FALSE;

    @Column(name = "email_subject", length = 500)
    private String emailSubject;

    @Column(name = "email_body", columnDefinition = "text")
    private String emailBody;

    @Column(name = "sms_body", columnDefinition = "text")
    private String smsBody;
}
