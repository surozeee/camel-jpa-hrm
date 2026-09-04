package com.jojolaptech.camel.model.postgres.recruitment;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.recruitment.enums.RecruitmentEmploymentTypeEnum;
import com.jojolaptech.camel.model.postgres.recruitment.enums.VacancyPriorityEnum;
import com.jojolaptech.camel.model.postgres.recruitment.enums.VacancyPublishScopeEnum;
import com.jojolaptech.camel.model.postgres.recruitment.enums.VacancyStatusEnum;
import com.jojolaptech.camel.model.postgres.recruitment.enums.WorkArrangementEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
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
        name = "hrm_recruitment_vacancy",
        indexes = {
            @Index(name = "idx_vacancy_company", columnList = "company_id"),
            @Index(name = "idx_vacancy_branch", columnList = "branch_id"),
            @Index(name = "idx_vacancy_code", columnList = "vacancy_code", unique = true)
        })
public class RecruitmentVacancyEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "vacancy_code", nullable = false, length = 64)
    private String vacancyCode;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "designation_id")
    private UUID designationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 40)
    private RecruitmentEmploymentTypeEnum employmentType;

    @Column(name = "openings", nullable = false)
    private Integer openings;

    @Column(name = "filled_count", nullable = false)
    @Builder.Default
    private Integer filledCount = 0;

    @Column(name = "job_level", length = 100)
    private String jobLevel;

    @Column(name = "work_location", length = 255)
    private String workLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_arrangement", length = 40)
    private WorkArrangementEnum workArrangement;

    @Column(name = "hiring_manager_employee_id")
    private UUID hiringManagerEmployeeId;

    @Column(name = "recruiter_employee_id")
    private UUID recruiterEmployeeId;

    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    @Column(name = "responsibilities", columnDefinition = "TEXT")
    private String responsibilities;

    @Column(name = "required_qualifications", columnDefinition = "TEXT")
    private String requiredQualifications;

    @Column(name = "preferred_qualifications", columnDefinition = "TEXT")
    private String preferredQualifications;

    @Column(name = "required_experience_years")
    private Integer requiredExperienceYears;

    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills;

    @Column(name = "salary_min", precision = 19, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 19, scale = 2)
    private BigDecimal salaryMax;

    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    @Column(name = "expected_joining_date")
    private LocalDate expectedJoiningDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private VacancyPriorityEnum priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "publish_scope", length = 20)
    private VacancyPublishScopeEnum publishScope;

    @Column(name = "published_at")
    private LocalDate publishedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "vacancy_status", nullable = false, length = 40)
    private VacancyStatusEnum vacancyStatus;
}
