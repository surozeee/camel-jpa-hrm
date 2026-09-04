package com.jojolaptech.camel.model.postgres.recruitment;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
        name = "hrm_recruitment_interview_stage",
        indexes = {@Index(name = "idx_int_stage_vacancy", columnList = "vacancy_id")})
public class RecruitmentInterviewStageEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vacancy_id", nullable = false)
    private RecruitmentVacancyEntity vacancy;

    @Column(name = "stage_name", nullable = false, length = 120)
    private String stageName;

    @Column(name = "sequence_no", nullable = false)
    private Integer sequenceNo;

    @Column(name = "mandatory_feedback", nullable = false)
    @Builder.Default
    private Boolean mandatoryFeedback = Boolean.FALSE;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
