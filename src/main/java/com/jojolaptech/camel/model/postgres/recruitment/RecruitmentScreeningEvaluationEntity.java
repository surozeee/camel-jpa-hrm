package com.jojolaptech.camel.model.postgres.recruitment;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.recruitment.enums.ApplicationStatusEnum;
import com.jojolaptech.camel.model.postgres.recruitment.enums.ScreeningEligibilityEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "hrm_recruitment_screening",
        indexes = {@Index(name = "idx_screening_app", columnList = "application_id")})
public class RecruitmentScreeningEvaluationEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private RecruitmentApplicationEntity application;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "concerns", columnDefinition = "TEXT")
    private String concerns;

    @Enumerated(EnumType.STRING)
    @Column(name = "eligibility", nullable = false, length = 40)
    private ScreeningEligibilityEnum eligibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommended_next_status", length = 40)
    private ApplicationStatusEnum recommendedNextStatus;

    @Column(name = "reviewer_employee_id")
    private UUID reviewerEmployeeId;

    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
