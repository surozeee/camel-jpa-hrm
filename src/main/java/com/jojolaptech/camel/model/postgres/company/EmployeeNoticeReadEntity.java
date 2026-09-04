package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "hrm_employee_notice_read",
        indexes = {@Index(name = "idx_enr_user_read_at", columnList = "user_id, read_at")},
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_enr_user_notice", columnNames = {"user_id", "notice_id"})
        })
public class EmployeeNoticeReadEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "notice_id", nullable = false)
    private UUID noticeId;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;
}
