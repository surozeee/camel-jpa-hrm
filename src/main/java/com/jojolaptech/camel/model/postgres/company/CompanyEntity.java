package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.CompanyStepperEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "company")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CompanyEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(nullable = false, unique = true)
    private String name;

    /** Current step in company-create stepper; persisted in DB. */
    @Enumerated(EnumType.STRING)
    @Column(name = "current_stepper_step")
    private CompanyStepperEnum currentStepperStep;

    @Column
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String contactNo;

    private String email;

    private String website;

    /** Public branding logo URL (bucket or absolute URL). */
    @Column(name = "logo_url", length = 1024)
    private String logoUrl;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "company_type_id", nullable = false)
    private CompanyTypeEntity companyType;

    /** Required when linked organization type is OTHER. */
    @Column(name = "organization_type_other_specify")
    private String organizationTypeOtherSpecify;

    /** Required when company type is OTHER. */
    @Column(name = "company_type_other_specify")
    private String companyTypeOtherSpecify;

    @OneToMany(mappedBy = "company", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private List<BranchEntity> branches;

    /** User who brought/onboarded this company (dealer portal user). */
    @Column(name = "dealer_user_id")
    private UUID dealerUserId;

    /** When true, company may use roster shift scheduling (system admin). */
    @Column(name = "enable_roster_shift")
    @Builder.Default
    private Boolean enableRosterShift = false;

    /** When true, company may use leave accumulation (system admin). */
    @Column(name = "enable_leave_accumulation")
    @Builder.Default
    private Boolean enableLeaveAccumulation = true;

    /** When true, Divisions menu and assignment fields are available. */
    @Column(name = "enable_division")
    @Builder.Default
    private Boolean enableDivision = true;

    /** When true, Teams menu and assignment fields are available. */
    @Column(name = "enable_team")
    @Builder.Default
    private Boolean enableTeam = true;

    /** When true, Grades menu and assignment fields are available. */
    @Column(name = "enable_grade")
    @Builder.Default
    private Boolean enableGrade = true;

    /** When true, Cost Centers menu and assignment fields are available. */
    @Column(name = "enable_cost_center")
    @Builder.Default
    private Boolean enableCostCenter = true;

    /** When true, Work Locations menu and assignment fields are available. */
    @Column(name = "enable_work_location")
    @Builder.Default
    private Boolean enableWorkLocation = true;

    /** When true, Gratuity menus are shown and new employees are enrolled. */
    @Column(name = "enable_gratuity")
    @Builder.Default
    private Boolean enableGratuity = true;

    /** When true, pre-onboarding pack and employee joining event are available. */
    @Column(name = "enable_onboarding")
    @Builder.Default
    private Boolean enableOnboarding = true;

    /** When true, cross-department joining checklist (HR/IT/Admin/Manager) is available. */
    @Column(name = "enable_joining_checklist")
    @Builder.Default
    private Boolean enableJoiningChecklist = true;

    /** When true, Time & Overtime menus, OT requests, official duty, and OT rate tables are available. */
    @Column(name = "enable_time_overtime")
    @Builder.Default
    private Boolean enableTimeOvertime = true;

    /** When true, Travel authorization menus and trip operations are available. */
    @Column(name = "enable_travel")
    @Builder.Default
    private Boolean enableTravel = true;

    /** When true, company asset inventory, assignment, handover, and clearance are available. */
    @Column(name = "enable_assets")
    @Builder.Default
    private Boolean enableAssets = true;

    /** When true, document templates, letter requests, versions, and expiry packs are available. */
    @Column(name = "enable_documents")
    @Builder.Default
    private Boolean enableDocuments = true;

    /** When true, probation cases, goals, review reminders, and confirmation workflow are available. */
    @Column(name = "enable_probation")
    @Builder.Default
    private Boolean enableProbation = true;

    /** When true, learning catalog, nominations, assessments, and effectiveness are available. */
    @Column(name = "enable_learning")
    @Builder.Default
    private Boolean enableLearning = true;

    /** When true, role competency matrix, skill-gap plans, and heatmaps are available. */
    @Column(name = "enable_competency")
    @Builder.Default
    private Boolean enableCompetency = true;

    /** When true, career paths, HiPo, successors, readiness, and critical-role plans are available. */
    @Column(name = "enable_career")
    @Builder.Default
    private Boolean enableCareer = true;

    /** When true, promotion and salary revision request workflow is available. */
    @Column(name = "enable_promotion")
    @Builder.Default
    private Boolean enablePromotion = true;

    /** When true, grievance / investigation / counseling ER case files are available. */
    @Column(name = "enable_employee_relations")
    @Builder.Default
    private Boolean enableEmployeeRelations = true;

    /** When true, disciplinary incident → warning → hearing → penalty cases are available. */
    @Column(name = "enable_disciplinary")
    @Builder.Default
    private Boolean enableDisciplinary = true;

    /** When true, surveys, polls, recognition, rewards, and ESS engagement hub are available. */
    @Column(name = "enable_engagement")
    @Builder.Default
    private Boolean enableEngagement = true;

    /** When true, resignation case workflow (approval, LWD, notice, clearance) is available. */
    @Column(name = "enable_resignation")
    @Builder.Default
    private Boolean enableResignation = true;

    /** When true, typed HR exit cases (termination/retirement/etc.) with clearance gate are available. */
    @Column(name = "enable_exit")
    @Builder.Default
    private Boolean enableExit = true;

    /** When true, notice policies, serving clock, and LWD reminders are available. */
    @Column(name = "enable_notice_period")
    @Builder.Default
    private Boolean enableNoticePeriod = true;
}

