package com.jojolaptech.camel.model.postgres.recruitment;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.recruitment.enums.ScreeningQuestionTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "hrm_recruitment_vacancy_screening_question",
        indexes = {@Index(name = "idx_vsq_vacancy", columnList = "vacancy_id")})
public class RecruitmentVacancyScreeningQuestionEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vacancy_id", nullable = false)
    private RecruitmentVacancyEntity vacancy;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 24)
    private ScreeningQuestionTypeEnum questionType;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "required_flag", nullable = false)
    @Builder.Default
    private Boolean required = Boolean.TRUE;

    @Column(name = "knockout_flag", nullable = false)
    @Builder.Default
    private Boolean knockout = Boolean.FALSE;

    @Column(name = "points", nullable = false)
    @Builder.Default
    private Integer points = 1;

    @Column(name = "correct_option_key", length = 32)
    private String correctOptionKey;

    @Column(name = "options_json", columnDefinition = "TEXT")
    private String optionsJson;
}
