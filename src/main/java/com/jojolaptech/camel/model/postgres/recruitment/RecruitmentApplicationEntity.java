package com.jojolaptech.camel.model.postgres.recruitment;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.recruitment.enums.ApplicationStatusEnum;
import com.jojolaptech.camel.model.postgres.recruitment.enums.CandidateSourceEnum;
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
        name = "hrm_recruitment_application",
        indexes = {
            @Index(name = "idx_app_vacancy", columnList = "vacancy_id"),
            @Index(name = "idx_app_candidate", columnList = "candidate_id"),
            @Index(name = "idx_app_unique", columnList = "vacancy_id,candidate_id", unique = true)
        })
public class RecruitmentApplicationEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private RecruitmentCandidateEntity candidate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vacancy_id", nullable = false)
    private RecruitmentVacancyEntity vacancy;

    @Column(name = "application_number", nullable = false, length = 64)
    private String applicationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", nullable = false, length = 40)
    private ApplicationStatusEnum applicationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 40)
    private CandidateSourceEnum source;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "assigned_recruiter_employee_id")
    private UUID assignedRecruiterEmployeeId;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "match_score")
    private Integer matchScore;
}
