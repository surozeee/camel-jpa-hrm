package com.jojolaptech.camel.route;



import com.jojolaptech.camel.processor.AttDeviceMacProcessor;
import com.jojolaptech.camel.processor.AttParamsProcessor;
import com.jojolaptech.camel.processor.AttShiftPatternProcessor;
import com.jojolaptech.camel.processor.AttTimeTableShiftProcessor;
import com.jojolaptech.camel.processor.AttendanceForgotProcessor;
import com.jojolaptech.camel.processor.AttendanceLogProcessor;
import com.jojolaptech.camel.processor.AttendanceRemarkProcessor;
import com.jojolaptech.camel.processor.AttendanceTransactionProcessor;
import com.jojolaptech.camel.processor.DeviceLogsProcessor;
import com.jojolaptech.camel.processor.OldAttendanceTransactionProcessor;
import com.jojolaptech.camel.processor.TempDeviceLogsProcessor;
import com.jojolaptech.camel.processor.WorkShiftProcessor;

import com.jojolaptech.camel.processor.BranchHolidayProcessor;
import com.jojolaptech.camel.processor.BranchAddressProcessor;

import com.jojolaptech.camel.processor.BranchLeaveTypeProcessor;

import com.jojolaptech.camel.processor.BranchProcessor;

import com.jojolaptech.camel.processor.CompanyAddressProcessor;

import com.jojolaptech.camel.processor.CompanyProcessor;

import com.jojolaptech.camel.processor.EmployeeAddressProcessor;
import com.jojolaptech.camel.processor.EmployeeAwardProcessor;
import com.jojolaptech.camel.processor.EmployeeDesignationLinkProcessor;
import com.jojolaptech.camel.processor.EmployeeDesignationProcessor;
import com.jojolaptech.camel.processor.EmployeeEducationProcessor;
import com.jojolaptech.camel.processor.EmployeeEmergencyContactProcessor;
import com.jojolaptech.camel.processor.EmployeeExperienceProcessor;
import com.jojolaptech.camel.processor.EmployeeFamilyProcessor;
import com.jojolaptech.camel.processor.EmployeeHealthProcessor;
import com.jojolaptech.camel.processor.EmployeeInsuranceProcessor;
import com.jojolaptech.camel.processor.EmployeeJobDescriptionProcessor;
import com.jojolaptech.camel.processor.EmployeeLanguageProcessor;
import com.jojolaptech.camel.processor.EmployeeLeaveAccumulationProcessor;
import com.jojolaptech.camel.processor.EmployeeMasterAddressProcessor;
import com.jojolaptech.camel.processor.EmployeeProcessor;
import com.jojolaptech.camel.processor.EmployeePublicationProcessor;
import com.jojolaptech.camel.processor.EmployeeSeminarProcessor;
import com.jojolaptech.camel.processor.EmployeeSkillProcessor;
import com.jojolaptech.camel.processor.EmployeeTerminationProcessor;
import com.jojolaptech.camel.processor.EmployeeTrainingProcessor;
import com.jojolaptech.camel.processor.EmploymentSuspensionProcessor;
import com.jojolaptech.camel.processor.BranchDepartmentHeadProcessor;
import com.jojolaptech.camel.processor.DivisionSeedProcessor;
import com.jojolaptech.camel.processor.CostCenterSeedProcessor;
import com.jojolaptech.camel.processor.TeamSeedProcessor;
import com.jojolaptech.camel.processor.OrgMasterHeadLinkProcessor;
import com.jojolaptech.camel.processor.EmployeeOrgFkBackfillProcessor;
import com.jojolaptech.camel.processor.EmployeeJobLevelHistoryProcessor;
import com.jojolaptech.camel.processor.JobStatusHistoryProcessor;
import com.jojolaptech.camel.processor.JobPositionProcessor;
import com.jojolaptech.camel.processor.CompanyEmployeeContractProcessor;
import com.jojolaptech.camel.processor.EmployeeJobHistoryProcessor;
import com.jojolaptech.camel.processor.DocumentProcessor;
import com.jojolaptech.camel.processor.EmployeeProjectProcessor;
import com.jojolaptech.camel.processor.JobCategoryProcessor;
import com.jojolaptech.camel.processor.JobCategoriesProcessor;
import com.jojolaptech.camel.processor.SkillMasterProcessor;
import com.jojolaptech.camel.processor.CostTypePackageProcessor;
import com.jojolaptech.camel.processor.PayPlanPackageProcessor;
import com.jojolaptech.camel.processor.ModulePricingPackageProcessor;
import com.jojolaptech.camel.processor.PayTypePackageProcessor;
import com.jojolaptech.camel.processor.CompanyValiditySubscriptionProcessor;
import com.jojolaptech.camel.processor.SubscriptionPaymentHistoryProcessor;
import com.jojolaptech.camel.processor.UserLicenseSubscriptionProcessor;
import com.jojolaptech.camel.processor.MarketingPersonDetailProcessor;
import com.jojolaptech.camel.processor.PricingEstimateEmailDetailsProcessor;
import com.jojolaptech.camel.processor.ApplicationModuleLookupProcessor;
import com.jojolaptech.camel.processor.PayrollInstitutionLookupProcessor;
import com.jojolaptech.camel.processor.CompanyPayrollBankProcessor;
import com.jojolaptech.camel.processor.CompanyPayrollInstitutionProcessor;
import com.jojolaptech.camel.processor.ParentPayrollHeadingLookupProcessor;
import com.jojolaptech.camel.processor.ChildPayrollHeadingLookupProcessor;
import com.jojolaptech.camel.processor.CompanyBranchPayrollHeadingProcessor;
import com.jojolaptech.camel.processor.PayrollLabelLookupProcessor;
import com.jojolaptech.camel.processor.PayrollHeadingPriorityLookupProcessor;
import com.jojolaptech.camel.processor.PayrollHeadingTemplateLookupProcessor;
import com.jojolaptech.camel.processor.PayrollHeadingDateLookupProcessor;
import com.jojolaptech.camel.processor.PayrollHeadingCalculationLookupProcessor;
import com.jojolaptech.camel.processor.PayPeriodSpecificHeadingLookupProcessor;
import com.jojolaptech.camel.processor.BranchPayPeriodLookupProcessor;
import com.jojolaptech.camel.processor.CompanySettingParamsProcessor;
import com.jojolaptech.camel.processor.CompanyAdminParamsProcessor;
import com.jojolaptech.camel.processor.CompanyEmployeeParamsProcessor;
import com.jojolaptech.camel.processor.EmployeeSummaryProcessor;
import com.jojolaptech.camel.processor.EditedOvertimeDetailsProcessor;
import com.jojolaptech.camel.processor.PayrollSettingLookupProcessor;
import com.jojolaptech.camel.processor.PayrollOvertimeLookupProcessor;
import com.jojolaptech.camel.processor.CalculatedTypeValueLookupProcessor;
import com.jojolaptech.camel.processor.PayByOnlineTransactionProcessor;
import com.jojolaptech.camel.processor.VacancyProcessor;
import com.jojolaptech.camel.processor.VacancyNewspaperProcessor;
import com.jojolaptech.camel.processor.InterviewStageProcessor;
import com.jojolaptech.camel.processor.ScreeningQuestionProcessor;
import com.jojolaptech.camel.processor.ApplicantProcessor;
import com.jojolaptech.camel.processor.ScreeningAnswerProcessor;
import com.jojolaptech.camel.processor.ApplicantsTransactionProcessor;
import com.jojolaptech.camel.processor.RecruitersProcessor;
import com.jojolaptech.camel.processor.EvaluationProcessor;
import com.jojolaptech.camel.processor.NoticeProcessor;
import com.jojolaptech.camel.processor.MessageNoticeProcessor;
import com.jojolaptech.camel.processor.CompanyMessageNoticeProcessor;
import com.jojolaptech.camel.processor.HappeningNoticeProcessor;
import com.jojolaptech.camel.processor.EventNoticeProcessor;
import com.jojolaptech.camel.processor.NotificationNoticeProcessor;
import com.jojolaptech.camel.processor.NotificationViewedProcessor;

import com.jojolaptech.camel.processor.DepartmentParentLinkProcessor;
import com.jojolaptech.camel.processor.DepartmentProcessor;

import com.jojolaptech.camel.processor.FiscalYearClosingParameterProcessor;

import com.jojolaptech.camel.processor.FiscalYearProcessor;

import com.jojolaptech.camel.processor.LeaveAccumulationRuleProcessor;
import com.jojolaptech.camel.processor.LeaveAdjustmentProcessor;
import com.jojolaptech.camel.processor.LeaveApplicationProcessor;
import com.jojolaptech.camel.processor.LeaveBalanceProcessor;
import com.jojolaptech.camel.processor.LeaveCancellationProcessor;
import com.jojolaptech.camel.processor.CalculatedAutoLeaveCreditProcessor;
import com.jojolaptech.camel.processor.CalculatedOtLeaveAccrualProcessor;
import com.jojolaptech.camel.processor.LeaveTypeProcessor;

import com.jojolaptech.camel.processor.OvertimeAccLeaveParamsProcessor;
import com.jojolaptech.camel.processor.PayrollRuleProcessor;

import com.jojolaptech.camel.processor.BranchSalaryBreakdownProcessor;

import com.jojolaptech.camel.processor.EmployeeSalaryProcessor;

import com.jojolaptech.camel.processor.PayrollOpeningBalanceProcessor;

import com.jojolaptech.camel.processor.MonthWiseSalaryProcessor;

import com.jojolaptech.camel.processor.PayrollTransactionHistoryProcessor;

import com.jojolaptech.camel.processor.GradeProcessor;

import com.jojolaptech.camel.processor.GradePayStepProcessor;

import com.jojolaptech.camel.processor.GradeComponentValueProcessor;

import com.jojolaptech.camel.processor.HeadingTemplateSeedProcessor;

import com.jojolaptech.camel.processor.LegacyBankProcessor;

import com.jojolaptech.camel.processor.EmployeeGradeLinkProcessor;

import com.jojolaptech.camel.processor.EmployeeBankDetailProcessor;

import com.jojolaptech.camel.processor.MassSalaryAdjustmentProcessor;

import com.jojolaptech.camel.processor.EmployeeLoanProcessor;

import com.jojolaptech.camel.processor.EmployeeLoanPaymentProcessor;

import com.jojolaptech.camel.processor.EmployeeDeviceEnrollProcessor;

import com.jojolaptech.camel.processor.EmployeeTempShiftProcessor;

import com.jojolaptech.camel.processor.EmployeePermanentShiftProcessor;

import com.jojolaptech.camel.processor.PrivilegeProcessor;

import com.jojolaptech.camel.processor.RoleProcessor;

import com.jojolaptech.camel.processor.TaxationProcessor;

import com.jojolaptech.camel.processor.UserDetailProcessor;

import com.jojolaptech.camel.processor.UserPortalLinkProcessor;
import com.jojolaptech.camel.processor.UserProcessor;

import com.jojolaptech.camel.qa.MigrationRowCountQaProcessor;

import com.jojolaptech.camel.repository.mysql.AttDeviceMACRepository;
import com.jojolaptech.camel.repository.mysql.AttEmpShiftRepository;
import com.jojolaptech.camel.repository.mysql.AttEmpTempShiftRepository;
import com.jojolaptech.camel.repository.mysql.AttHolidayDateRepository;
import com.jojolaptech.camel.repository.mysql.AttLogsRepository;
import com.jojolaptech.camel.repository.mysql.AttShiftRepository;
import com.jojolaptech.camel.repository.mysql.AttTimeTableRepository;
import com.jojolaptech.camel.repository.mysql.AttendanceForgotRepository;
import com.jojolaptech.camel.repository.mysql.AttendanceRemarkRepository;
import com.jojolaptech.camel.repository.mysql.AttendanceTransactionRepository;
import com.jojolaptech.camel.repository.mysql.DeviceLogsRepository;
import com.jojolaptech.camel.repository.mysql.OldAttendanceTransactionRepository;
import com.jojolaptech.camel.repository.mysql.TempDeviceLogsRepository;
import com.jojolaptech.camel.repository.mysql.WorkShiftRepository;
import com.jojolaptech.camel.repository.mysql.AutoLeaveAccParamsRepository;
import com.jojolaptech.camel.repository.mysql.BranchDepartmentRepository;

import com.jojolaptech.camel.repository.mysql.BranchRepository;

import com.jojolaptech.camel.repository.mysql.CompanyFiscalYearClosingParameterRepository;

import com.jojolaptech.camel.repository.mysql.CompanyRepository;

import com.jojolaptech.camel.repository.mysql.EmployeeAddressRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeAwardRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeContactRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeEducationRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeExperienceRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeHealthRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeInsuranceRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeJobRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeLanguageRepository;
import com.jojolaptech.camel.repository.mysql.EmployeePublicationRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeSeminarRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeSkillRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeTerminationRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeTrainingRepository;
import com.jojolaptech.camel.repository.mysql.EmploymentSuspensionRepository;
import com.jojolaptech.camel.repository.mysql.JobDescriptionRepository;
import com.jojolaptech.camel.repository.mysql.JobTitleRepository;
import com.jojolaptech.camel.repository.mysql.BranchDepartmentHeadRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeJobLevelRepository;
import com.jojolaptech.camel.repository.mysql.JobStatusRepository;
import com.jojolaptech.camel.repository.mysql.JobPositionRepository;
import com.jojolaptech.camel.repository.mysql.CompanyEmployeeContractRepository;
import com.jojolaptech.camel.repository.mysql.DocumentRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeProjectRepository;
import com.jojolaptech.camel.repository.mysql.JobCategoryRepository;
import com.jojolaptech.camel.repository.mysql.JobCategoriesRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeRepository;
import com.jojolaptech.camel.repository.mysql.FamilyRepository;
import com.jojolaptech.camel.repository.mysql.LeaveAccumulationRepository;
import com.jojolaptech.camel.repository.mysql.LeaveAdjustmentRepository;
import com.jojolaptech.camel.repository.mysql.LeaveApplicationRepository;
import com.jojolaptech.camel.repository.mysql.LeaveBalanceRepository;
import com.jojolaptech.camel.repository.mysql.LeaveCancellationRepository;
import com.jojolaptech.camel.repository.mysql.CalculatedAutoLeaveAccumulationRepository;
import com.jojolaptech.camel.repository.mysql.CalculatedOTLeaveBalanceRepository;
import com.jojolaptech.camel.repository.mysql.OvertimeAccLeaveParamsRepository;
import com.jojolaptech.camel.repository.mysql.DepartmentRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;

import com.jojolaptech.camel.repository.mysql.FiscalYearRepository;

import com.jojolaptech.camel.repository.mysql.LeavesRepository;

import com.jojolaptech.camel.repository.mysql.PayrollCalculationSettingRepository;

import com.jojolaptech.camel.repository.mysql.CompanyPayrollHeadingRepository;

import com.jojolaptech.camel.repository.mysql.PayrollHeadingRepository;

import com.jojolaptech.camel.repository.mysql.EmployeePayrollHeadingRepository;

import com.jojolaptech.camel.repository.mysql.OpeningPayrollBalanceRepository;

import com.jojolaptech.camel.repository.mysql.EmployeePayrollPaymentRepository;

import com.jojolaptech.camel.repository.mysql.PayrollTransactionRepository;

import com.jojolaptech.camel.repository.mysql.PayrollMonthRepository;

import com.jojolaptech.camel.repository.mysql.JobLevelRepository;

import com.jojolaptech.camel.repository.mysql.JobLevelGradeValueRepository;

import com.jojolaptech.camel.repository.mysql.JobLevelPayrollRepository;

import com.jojolaptech.camel.repository.mysql.EmployeeGradeRepository;

import com.jojolaptech.camel.repository.mysql.MassIncrementRepository;

import com.jojolaptech.camel.repository.mysql.EmployeePayrollPaymentSettingRepository;

import com.jojolaptech.camel.repository.mysql.EmployeeLoanRepository;

import com.jojolaptech.camel.repository.mysql.EmployeeLoanPaymentRepository;

import com.jojolaptech.camel.repository.mysql.TemplatePayrollHeadingRepository;

import com.jojolaptech.camel.repository.mysql.BankRepository;
import com.jojolaptech.camel.repository.mysql.CostTypeRepository;
import com.jojolaptech.camel.repository.mysql.PayPlanRepository;
import com.jojolaptech.camel.repository.mysql.ModulePricingRepository;
import com.jojolaptech.camel.repository.mysql.PayTypeRepository;
import com.jojolaptech.camel.repository.mysql.CompanyValidityRepository;
import com.jojolaptech.camel.repository.mysql.SubscriptionPaymentRepository;
import com.jojolaptech.camel.repository.mysql.UserLicenseRepository;
import com.jojolaptech.camel.repository.mysql.MarketingPersonDetailRepository;
import com.jojolaptech.camel.repository.mysql.PricingEstimateEmailDetailsRepository;
import com.jojolaptech.camel.repository.mysql.ApplicationModuleRepository;
import com.jojolaptech.camel.repository.mysql.PayrollInstitutionRepository;
import com.jojolaptech.camel.repository.mysql.CompanyPayrollRepository;
import com.jojolaptech.camel.repository.mysql.CompanyPayrollInstitutionRepository;
import com.jojolaptech.camel.repository.mysql.ParentPayrollHeadingRepository;
import com.jojolaptech.camel.repository.mysql.ChildPayrollHeadingRepository;
import com.jojolaptech.camel.repository.mysql.CompanyBranchPayrollHeadingRepository;
import com.jojolaptech.camel.repository.mysql.PayrollLabelRepository;
import com.jojolaptech.camel.repository.mysql.PayrollHeadingPriorityRepository;
import com.jojolaptech.camel.repository.mysql.PayrollHeadingTemplateRepository;
import com.jojolaptech.camel.repository.mysql.PayrollHeadingDateRepository;
import com.jojolaptech.camel.repository.mysql.PayrollHeadingCalculationRepository;
import com.jojolaptech.camel.repository.mysql.PayPeriodSpecificHeadingRepository;
import com.jojolaptech.camel.repository.mysql.BranchPayPeriodRepository;
import com.jojolaptech.camel.repository.mysql.CompanySettingParamsRepository;
import com.jojolaptech.camel.repository.mysql.CompanyAdminParamsRepository;
import com.jojolaptech.camel.repository.mysql.CompanyEmployeeParamsRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeSummaryRepository;
import com.jojolaptech.camel.repository.mysql.EditedOvertimeDetailsRepository;
import com.jojolaptech.camel.repository.mysql.PayrollSettingRepository;
import com.jojolaptech.camel.repository.mysql.PayrollOvertimeRepository;
import com.jojolaptech.camel.repository.mysql.CalculatedTypeValueRepository;
import com.jojolaptech.camel.repository.mysql.PayByOnlineTransactionRepository;
import com.jojolaptech.camel.repository.mysql.VacancyRepository;
import com.jojolaptech.camel.repository.mysql.VacancyNewspaperRepository;
import com.jojolaptech.camel.repository.mysql.StagesRepository;
import com.jojolaptech.camel.repository.mysql.ScreeningQuestionRepository;
import com.jojolaptech.camel.repository.mysql.ScreeningAnswerRepository;
import com.jojolaptech.camel.repository.mysql.ApplicantRepository;
import com.jojolaptech.camel.repository.mysql.ApplicantsTransactionRepository;
import com.jojolaptech.camel.repository.mysql.RecruitersRepository;
import com.jojolaptech.camel.repository.mysql.EvaluationRepository;
import com.jojolaptech.camel.repository.mysql.NoticeRepository;
import com.jojolaptech.camel.repository.mysql.MessageRepository;
import com.jojolaptech.camel.repository.mysql.CompanyMessageCompanyRepository;
import com.jojolaptech.camel.repository.mysql.HappeningRepository;
import com.jojolaptech.camel.repository.mysql.EventRepository;
import com.jojolaptech.camel.repository.mysql.NotificationRepository;
import com.jojolaptech.camel.repository.mysql.NotificationViewedRepository;

import com.jojolaptech.camel.repository.mysql.RequestmapRepository;

import com.jojolaptech.camel.repository.mysql.SecRoleRepository;

import com.jojolaptech.camel.repository.mysql.SecUserRepository;

import com.jojolaptech.camel.repository.mysql.TaxationRepository;

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

import lombok.RequiredArgsConstructor;

import org.apache.camel.builder.RouteBuilder;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Sort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component

@RequiredArgsConstructor

public class ImportRouteBuilder extends RouteBuilder {



    private static final Logger log = LoggerFactory.getLogger(ImportRouteBuilder.class);



    private static final int PAGE_SIZE = 100;

    private static final int MIGRATION_THROTTLE_MS = 1000;



    private final PrivilegeProcessor privilegeProcessor;

    private final RoleProcessor roleProcessor;

    private final CompanyProcessor companyProcessor;

    private final CompanyAddressProcessor companyAddressProcessor;

    private final BranchProcessor branchProcessor;

    private final BranchAddressProcessor branchAddressProcessor;

    private final FiscalYearProcessor fiscalYearProcessor;

    private final TaxationProcessor taxationProcessor;

    private final PayrollRuleProcessor payrollRuleProcessor;

    private final BranchSalaryBreakdownProcessor branchSalaryBreakdownProcessor;

    private final EmployeeSalaryProcessor employeeSalaryProcessor;

    private final PayrollOpeningBalanceProcessor payrollOpeningBalanceProcessor;

    private final MonthWiseSalaryProcessor monthWiseSalaryProcessor;

    private final PayrollTransactionHistoryProcessor payrollTransactionHistoryProcessor;

    private final GradeProcessor gradeProcessor;

    private final GradePayStepProcessor gradePayStepProcessor;

    private final GradeComponentValueProcessor gradeComponentValueProcessor;

    private final HeadingTemplateSeedProcessor headingTemplateSeedProcessor;

    private final LegacyBankProcessor legacyBankProcessor;

    private final CostTypePackageProcessor costTypePackageProcessor;

    private final PayPlanPackageProcessor payPlanPackageProcessor;

    private final ModulePricingPackageProcessor modulePricingPackageProcessor;

    private final PayTypePackageProcessor payTypePackageProcessor;

    private final CompanyValiditySubscriptionProcessor companyValiditySubscriptionProcessor;

    private final SubscriptionPaymentHistoryProcessor subscriptionPaymentHistoryProcessor;

    private final UserLicenseSubscriptionProcessor userLicenseSubscriptionProcessor;

    private final VacancyProcessor vacancyProcessor;
    private final VacancyNewspaperProcessor vacancyNewspaperProcessor;
    private final InterviewStageProcessor interviewStageProcessor;
    private final ScreeningQuestionProcessor screeningQuestionProcessor;
    private final ApplicantProcessor applicantProcessor;
    private final ScreeningAnswerProcessor screeningAnswerProcessor;
    private final ApplicantsTransactionProcessor applicantsTransactionProcessor;
    private final RecruitersProcessor recruitersProcessor;
    private final EvaluationProcessor evaluationProcessor;

    private final NoticeProcessor noticeProcessor;
    private final MessageNoticeProcessor messageNoticeProcessor;
    private final CompanyMessageNoticeProcessor companyMessageNoticeProcessor;
    private final HappeningNoticeProcessor happeningNoticeProcessor;
    private final EventNoticeProcessor eventNoticeProcessor;
    private final NotificationNoticeProcessor notificationNoticeProcessor;
    private final NotificationViewedProcessor notificationViewedProcessor;

    private final MarketingPersonDetailProcessor marketingPersonDetailProcessor;
    private final PricingEstimateEmailDetailsProcessor pricingEstimateEmailDetailsProcessor;
    private final ApplicationModuleLookupProcessor applicationModuleLookupProcessor;

    @Autowired
    private PayrollInstitutionLookupProcessor payrollInstitutionLookupProcessor;
    @Autowired
    private CompanyPayrollBankProcessor companyPayrollBankProcessor;
    @Autowired
    private CompanyPayrollInstitutionProcessor companyPayrollInstitutionProcessor;
    @Autowired
    private ParentPayrollHeadingLookupProcessor parentPayrollHeadingLookupProcessor;
    @Autowired
    private ChildPayrollHeadingLookupProcessor childPayrollHeadingLookupProcessor;
    @Autowired
    private CompanyBranchPayrollHeadingProcessor companyBranchPayrollHeadingProcessor;
    @Autowired
    private PayrollLabelLookupProcessor payrollLabelLookupProcessor;
    @Autowired
    private PayrollHeadingPriorityLookupProcessor payrollHeadingPriorityLookupProcessor;
    @Autowired
    private PayrollHeadingTemplateLookupProcessor payrollHeadingTemplateLookupProcessor;
    @Autowired
    private PayrollHeadingDateLookupProcessor payrollHeadingDateLookupProcessor;
    @Autowired
    private PayrollHeadingCalculationLookupProcessor payrollHeadingCalculationLookupProcessor;
    @Autowired
    private PayPeriodSpecificHeadingLookupProcessor payPeriodSpecificHeadingLookupProcessor;
    @Autowired
    private BranchPayPeriodLookupProcessor branchPayPeriodLookupProcessor;
    @Autowired
    private CompanySettingParamsProcessor companySettingParamsProcessor;
    @Autowired
    private CompanyAdminParamsProcessor companyAdminParamsProcessor;
    @Autowired
    private CompanyEmployeeParamsProcessor companyEmployeeParamsProcessor;
    @Autowired
    private EmployeeSummaryProcessor employeeSummaryProcessor;
    @Autowired
    private EditedOvertimeDetailsProcessor editedOvertimeDetailsProcessor;
    @Autowired
    private PayrollSettingLookupProcessor payrollSettingLookupProcessor;
    @Autowired
    private PayrollOvertimeLookupProcessor payrollOvertimeLookupProcessor;
    @Autowired
    private CalculatedTypeValueLookupProcessor calculatedTypeValueLookupProcessor;
    @Autowired
    private PayByOnlineTransactionProcessor payByOnlineTransactionProcessor;

    private final EmployeeGradeLinkProcessor employeeGradeLinkProcessor;

    private final EmployeeBankDetailProcessor employeeBankDetailProcessor;

    private final MassSalaryAdjustmentProcessor massSalaryAdjustmentProcessor;

    private final EmployeeLoanProcessor employeeLoanProcessor;

    private final EmployeeLoanPaymentProcessor employeeLoanPaymentProcessor;

    private final LeaveTypeProcessor leaveTypeProcessor;

    private final BranchLeaveTypeProcessor branchLeaveTypeProcessor;

    private final AttTimeTableShiftProcessor attTimeTableShiftProcessor;

    private final AttShiftPatternProcessor attShiftPatternProcessor;

    private final BranchHolidayProcessor branchHolidayProcessor;

    private final LeaveAccumulationRuleProcessor leaveAccumulationRuleProcessor;

    private final OvertimeAccLeaveParamsProcessor overtimeAccLeaveParamsProcessor;

    private final FiscalYearClosingParameterProcessor fiscalYearClosingParameterProcessor;

    private final AttParamsProcessor attParamsProcessor;

    private final AttDeviceMacProcessor attDeviceMacProcessor;

    private final EmployeeDeviceEnrollProcessor employeeDeviceEnrollProcessor;

    private final EmployeeTempShiftProcessor employeeTempShiftProcessor;

    private final EmployeePermanentShiftProcessor employeePermanentShiftProcessor;

    private final EmployeeExperienceProcessor employeeExperienceProcessor;
    private final EmployeeAwardProcessor employeeAwardProcessor;
    private final EmployeeLanguageProcessor employeeLanguageProcessor;
    private final EmployeeSeminarProcessor employeeSeminarProcessor;
    private final EmployeePublicationProcessor employeePublicationProcessor;
    private final EmployeeHealthProcessor employeeHealthProcessor;
    private final EmployeeTrainingProcessor employeeTrainingProcessor;
    private final EmployeeJobDescriptionProcessor employeeJobDescriptionProcessor;
    private final EmploymentSuspensionProcessor employmentSuspensionProcessor;
    private final EmployeeInsuranceProcessor employeeInsuranceProcessor;
    private final SkillMasterProcessor skillMasterProcessor;
    private final EmployeeSkillProcessor employeeSkillProcessor;
    private final EmployeeDesignationProcessor employeeDesignationProcessor;
    private final EmployeeDesignationLinkProcessor employeeDesignationLinkProcessor;
    private final EmployeeEmergencyContactProcessor employeeEmergencyContactProcessor;
    private final EmployeeTerminationProcessor employeeTerminationProcessor;
    private final BranchDepartmentHeadProcessor branchDepartmentHeadProcessor;
    private final DivisionSeedProcessor divisionSeedProcessor;
    private final CostCenterSeedProcessor costCenterSeedProcessor;
    private final TeamSeedProcessor teamSeedProcessor;
    private final OrgMasterHeadLinkProcessor orgMasterHeadLinkProcessor;
    private final EmployeeOrgFkBackfillProcessor employeeOrgFkBackfillProcessor;
    private final EmployeeJobLevelHistoryProcessor employeeJobLevelHistoryProcessor;
    private final JobStatusHistoryProcessor jobStatusHistoryProcessor;
    private final JobPositionProcessor jobPositionProcessor;
    private final CompanyEmployeeContractProcessor companyEmployeeContractProcessor;
    private final EmployeeJobHistoryProcessor employeeJobHistoryProcessor;
    private final DocumentProcessor documentProcessor;
    private final EmployeeProjectProcessor employeeProjectProcessor;
    private final JobCategoryProcessor jobCategoryProcessor;
    private final JobCategoriesProcessor jobCategoriesProcessor;

    private final AttendanceLogProcessor attendanceLogProcessor;

    private final AttendanceTransactionProcessor attendanceTransactionProcessor;

    private final AttendanceForgotProcessor attendanceForgotProcessor;

    private final AttendanceRemarkProcessor attendanceRemarkProcessor;

    private final DeviceLogsProcessor deviceLogsProcessor;

    private final TempDeviceLogsProcessor tempDeviceLogsProcessor;

    private final OldAttendanceTransactionProcessor oldAttendanceTransactionProcessor;

    private final WorkShiftProcessor workShiftProcessor;

    private final DepartmentProcessor departmentProcessor;
    private final DepartmentParentLinkProcessor departmentParentLinkProcessor;

    private final EmployeeProcessor employeeProcessor;

    private final EmployeeAddressProcessor employeeAddressProcessor;

    private final EmployeeMasterAddressProcessor employeeMasterAddressProcessor;

    private final EmployeeEducationProcessor employeeEducationProcessor;

    private final EmployeeFamilyProcessor employeeFamilyProcessor;

    private final EmployeeLeaveAccumulationProcessor employeeLeaveAccumulationProcessor;

    private final LeaveBalanceProcessor leaveBalanceProcessor;

    private final LeaveAdjustmentProcessor leaveAdjustmentProcessor;

    private final LeaveApplicationProcessor leaveApplicationProcessor;

    private final LeaveCancellationProcessor leaveCancellationProcessor;

    private final CalculatedAutoLeaveCreditProcessor calculatedAutoLeaveCreditProcessor;

    private final CalculatedOtLeaveAccrualProcessor calculatedOtLeaveAccrualProcessor;

    private final UserProcessor userProcessor;

    private final UserDetailProcessor userDetailProcessor;

    private final UserPortalLinkProcessor userPortalLinkProcessor;

    private final MigrationRowCountQaProcessor migrationRowCountQaProcessor;

    private final RequestmapRepository requestmapRepository;

    private final SecRoleRepository secRoleRepository;

    private final CompanyRepository companyRepository;

    private final BranchRepository branchRepository;

    private final BranchDepartmentRepository branchDepartmentRepository;

    private final FiscalYearRepository fiscalYearRepository;

    private final TaxationRepository taxationRepository;

    private final PayrollCalculationSettingRepository payrollCalculationSettingRepository;

    private final CompanyPayrollHeadingRepository companyPayrollHeadingRepository;

    private final PayrollHeadingRepository payrollHeadingRepository;

    private final EmployeePayrollHeadingRepository employeePayrollHeadingRepository;

    private final OpeningPayrollBalanceRepository openingPayrollBalanceRepository;

    private final EmployeePayrollPaymentRepository employeePayrollPaymentRepository;

    private final PayrollTransactionRepository payrollTransactionRepository;

    private final PayrollMonthRepository payrollMonthRepository;

    private final JobLevelRepository jobLevelRepository;

    private final JobLevelGradeValueRepository jobLevelGradeValueRepository;

    private final JobLevelPayrollRepository jobLevelPayrollRepository;

    private final EmployeeGradeRepository employeeGradeRepository;

    private final MassIncrementRepository massIncrementRepository;

    private final EmployeePayrollPaymentSettingRepository employeePayrollPaymentSettingRepository;

    private final EmployeeLoanRepository employeeLoanRepository;

    private final EmployeeLoanPaymentRepository employeeLoanPaymentRepository;

    private final TemplatePayrollHeadingRepository templatePayrollHeadingRepository;

    private final BankRepository bankRepository;

    private final CostTypeRepository costTypeRepository;

    private final PayPlanRepository payPlanRepository;

    private final ModulePricingRepository modulePricingRepository;

    private final PayTypeRepository payTypeRepository;

    private final CompanyValidityRepository companyValidityRepository;

    private final SubscriptionPaymentRepository subscriptionPaymentRepository;

    private final UserLicenseRepository userLicenseRepository;

    private final VacancyRepository vacancyRepository;
    private final VacancyNewspaperRepository vacancyNewspaperRepository;
    private final StagesRepository stagesRepository;
    private final ScreeningQuestionRepository screeningQuestionRepository;
    private final ScreeningAnswerRepository screeningAnswerRepository;
    private final ApplicantRepository applicantRepository;
    private final ApplicantsTransactionRepository applicantsTransactionRepository;
    private final RecruitersRepository recruitersRepository;
    private final EvaluationRepository evaluationRepository;

    private final NoticeRepository noticeRepository;
    private final MessageRepository messageRepository;
    private final CompanyMessageCompanyRepository companyMessageCompanyRepository;
    private final HappeningRepository happeningRepository;
    private final EventRepository eventRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationViewedRepository notificationViewedRepository;

    private final MarketingPersonDetailRepository marketingPersonDetailRepository;
    private final PricingEstimateEmailDetailsRepository pricingEstimateEmailDetailsRepository;
    private final ApplicationModuleRepository applicationModuleRepository;

    @Autowired
    private PayrollInstitutionRepository payrollInstitutionRepository;
    @Autowired
    private CompanyPayrollRepository companyPayrollRepository;
    @Autowired
    private CompanyPayrollInstitutionRepository companyPayrollInstitutionRepository;
    @Autowired
    private ParentPayrollHeadingRepository parentPayrollHeadingRepository;
    @Autowired
    private ChildPayrollHeadingRepository childPayrollHeadingRepository;
    @Autowired
    private CompanyBranchPayrollHeadingRepository companyBranchPayrollHeadingRepository;
    @Autowired
    private PayrollLabelRepository payrollLabelRepository;
    @Autowired
    private PayrollHeadingPriorityRepository payrollHeadingPriorityRepository;
    @Autowired
    private PayrollHeadingTemplateRepository payrollHeadingTemplateRepository;
    @Autowired
    private PayrollHeadingDateRepository payrollHeadingDateRepository;
    @Autowired
    private PayrollHeadingCalculationRepository payrollHeadingCalculationRepository;
    @Autowired
    private PayPeriodSpecificHeadingRepository payPeriodSpecificHeadingRepository;
    @Autowired
    private BranchPayPeriodRepository branchPayPeriodRepository;
    @Autowired
    private CompanySettingParamsRepository companySettingParamsRepository;
    @Autowired
    private CompanyAdminParamsRepository companyAdminParamsRepository;
    @Autowired
    private CompanyEmployeeParamsRepository companyEmployeeParamsRepository;
    @Autowired
    private EmployeeSummaryRepository employeeSummaryRepository;
    @Autowired
    private EditedOvertimeDetailsRepository editedOvertimeDetailsRepository;
    @Autowired
    private PayrollSettingRepository payrollSettingRepository;
    @Autowired
    private PayrollOvertimeRepository payrollOvertimeRepository;
    @Autowired
    private CalculatedTypeValueRepository calculatedTypeValueRepository;
    @Autowired
    private PayByOnlineTransactionRepository payByOnlineTransactionRepository;

    private final CompanyFiscalYearClosingParameterRepository companyFiscalYearClosingParameterRepository;

    private final LeavesRepository leavesRepository;

    private final AttTimeTableRepository attTimeTableRepository;

    private final AttShiftRepository attShiftRepository;

    private final AttDeviceMACRepository attDeviceMACRepository;

    private final AttEmpTempShiftRepository attEmpTempShiftRepository;

    private final AttEmpShiftRepository attEmpShiftRepository;

    private final AttLogsRepository attLogsRepository;

    private final AttendanceTransactionRepository attendanceTransactionRepository;

    private final AttendanceForgotRepository attendanceForgotRepository;

    private final AttendanceRemarkRepository attendanceRemarkRepository;

    private final DeviceLogsRepository deviceLogsRepository;

    private final TempDeviceLogsRepository tempDeviceLogsRepository;

    private final OldAttendanceTransactionRepository oldAttendanceTransactionRepository;

    private final WorkShiftRepository workShiftRepository;

    private final AttHolidayDateRepository attHolidayDateRepository;

    private final AutoLeaveAccParamsRepository autoLeaveAccParamsRepository;

    private final OvertimeAccLeaveParamsRepository overtimeAccLeaveParamsRepository;

    private final EmployeeRepository employeeRepository;

    private final EmployeeAddressRepository employeeAddressRepository;

    private final EmployeeEducationRepository employeeEducationRepository;

    private final FamilyRepository familyRepository;

    private final EmployeeExperienceRepository employeeExperienceRepository;
    private final EmployeeAwardRepository employeeAwardRepository;
    private final EmployeeLanguageRepository employeeLanguageRepository;
    private final EmployeeSeminarRepository employeeSeminarRepository;
    private final EmployeePublicationRepository employeePublicationRepository;
    private final EmployeeHealthRepository employeeHealthRepository;
    private final EmployeeTrainingRepository employeeTrainingRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final EmploymentSuspensionRepository employmentSuspensionRepository;
    private final EmployeeInsuranceRepository employeeInsuranceRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final JobTitleRepository jobTitleRepository;
    private final EmployeeJobRepository employeeJobRepository;
    private final EmployeeContactRepository employeeContactRepository;
    private final EmployeeTerminationRepository employeeTerminationRepository;
    private final BranchDepartmentHeadRepository branchDepartmentHeadRepository;
    private final PgEmployeeRepository pgEmployeeRepository;
    private final EmployeeJobLevelRepository employeeJobLevelRepository;
    private final JobStatusRepository jobStatusRepository;
    private final JobPositionRepository jobPositionRepository;
    private final CompanyEmployeeContractRepository companyEmployeeContractRepository;
    private final DocumentRepository documentRepository;
    private final EmployeeProjectRepository employeeProjectRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final JobCategoriesRepository jobCategoriesRepository;

    private final LeaveAccumulationRepository leaveAccumulationRepository;

    private final LeaveBalanceRepository leaveBalanceRepository;

    private final LeaveAdjustmentRepository leaveAdjustmentRepository;

    private final LeaveApplicationRepository leaveApplicationRepository;

    private final LeaveCancellationRepository leaveCancellationRepository;

    private final CalculatedAutoLeaveAccumulationRepository calculatedAutoLeaveAccumulationRepository;

    private final CalculatedOTLeaveBalanceRepository calculatedOTLeaveBalanceRepository;

    private final DepartmentRepository departmentRepository;

    private final SecUserRepository secUserRepository;



    @Override

    public void configure() {

        errorHandler(defaultErrorHandler()

                .maximumRedeliveries(3)

                .redeliveryDelay(2000));



        from("timer:master-import?repeatCount=1&delay=0")

                .routeId("master-migration-route")

                .process(exchange -> {

                    long startTime = System.currentTimeMillis();

                    exchange.setProperty("startTime", startTime);

                    LocalDateTime startDateTime = LocalDateTime.now();

                    exchange.setProperty("startDateTime", startDateTime);

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

                    log.info("==========================================");

                    log.info("Starting HRM master migration...");

                    log.info("Start Time: {}", startDateTime.format(formatter));

                    log.info("Page Size: {}", PAGE_SIZE);

                    log.info("==========================================");

                })

                .to("direct:privilege-migration")

                .log("Step 1 completed: privilege-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:role-migration")

                .log("Step 2 completed: role-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:company-migration")

                .log("Step 3 completed: company-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:company-address-migration")

                .log("Step 4 completed: company-address-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:branch-migration")

                .log("Step 5 completed: branch-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:branch-address-migration")

                .log("Step 6 completed: branch-address-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:fiscal-year-migration")

                .log("Step 7 completed: fiscal-year-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:taxation-migration")

                .log("Step 8 completed: taxation-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:payroll-rule-migration")

                .log("Step 9 completed: payroll-rule-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:salary-breakdown-migration")

                .log("Step 9a completed: salary-breakdown-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:pms-salary-breakdown-migration")

                .log("Step 9b completed: pms-salary-breakdown-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:grade-migration")

                .log("Step 9c completed: grade-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:grade-pay-step-migration")

                .log("Step 9d completed: grade-pay-step-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:grade-component-migration")

                .log("Step 9e completed: grade-component-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:heading-template-seed-migration")

                .log("Step 9f completed: heading-template-seed-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:legacy-bank-migration")

                .log("Step 9g completed: legacy-bank-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:cost-type-package-migration")

                .log("Step 9h completed: cost-type-package-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:pay-plan-package-migration")

                .log("Step 9i completed: pay-plan-package-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:module-pricing-package-migration")

                .log("Step 9j completed: module-pricing-package-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:pay-type-package-migration")

                .log("Step 9k completed: pay-type-package-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:payroll-institution-lookup-migration")
                .log("Step 9l completed: payroll-institution-lookup-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:company-payroll-bank-migration")
                .log("Step 9m completed: company-payroll-bank-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:company-payroll-institution-migration")
                .log("Step 9n completed: company-payroll-institution-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:parent-payroll-heading-lookup-migration")
                .log("Step 9o completed: parent-payroll-heading-lookup-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:child-payroll-heading-lookup-migration")
                .log("Step 9o2 completed: child-payroll-heading-lookup-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:company-branch-payroll-heading-migration")
                .log("Step 9p completed: company-branch-payroll-heading-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:payroll-label-lookup-migration")
                .log("Step 9q completed: payroll-label-lookup-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:payroll-heading-priority-lookup-migration")
                .log("Step 9r completed: payroll-heading-priority-lookup-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:payroll-heading-template-lookup-migration")
                .log("Step 9s completed: payroll-heading-template-lookup-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:payroll-heading-date-lookup-migration")
                .log("Step 9t completed: payroll-heading-date-lookup-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:payroll-heading-calculation-lookup-migration")
                .log("Step 9u completed: payroll-heading-calculation-lookup-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:pay-period-specific-heading-lookup-migration")
                .log("Step 9v completed: pay-period-specific-heading-lookup-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:branch-pay-period-lookup-migration")
                .log("Step 9w completed: branch-pay-period-lookup-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:leave-type-migration")

                .log("Step 10 completed: leave-type-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:branch-leave-type-migration")

                .log("Step 11 completed: branch-leave-type-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:att-timetable-shift-migration")

                .log("Step 12 completed: att-timetable-shift-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:att-shift-pattern-migration")

                .log("Step 13 completed: att-shift-pattern-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:branch-holiday-migration")

                .log("Step 14 completed: branch-holiday-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:leave-accumulation-rule-migration")

                .log("Step 15 completed: leave-accumulation-rule-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:overtime-acc-leave-params-migration")

                .log("Step 16 completed: overtime-acc-leave-params-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:fy-closing-parameter-migration")

                .log("Step 17 completed: fy-closing-parameter-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:att-params-migration")

                .log("Step 18 completed: att-params-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:device-mac-migration")

                .log("Step 18a completed: device-mac-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:company-setting-params-migration")
                .log("Step 18b completed: company-setting-params-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:company-admin-params-migration")
                .log("Step 18c completed: company-admin-params-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:company-employee-params-migration")
                .log("Step 18d completed: company-employee-params-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-summary-migration")
                .log("Step 18e completed: employee-summary-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:department-migration")

                .log("Step 19 completed: department-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:department-orphan-migration")

                .log("Step 20 completed: department-orphan-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:department-parent-link")

                .log("Step 21 completed: department-parent-link")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:division-seed-migration")
                .log("Step 21a completed: division-seed-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:cost-center-seed-migration")
                .log("Step 21b completed: cost-center-seed-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:team-seed-migration")
                .log("Step 21c completed: team-seed-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-migration")

                .log("Step 22 completed: employee-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-address-migration")

                .log("Step 22a completed: employee-address-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-master-address-migration")

                .log("Step 22b completed: employee-master-address-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-education-migration")

                .log("Step 22c completed: employee-education-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-family-migration")

                .log("Step 22d completed: employee-family-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-grade-link-migration")

                .log("Step 22e completed: employee-grade-link-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-bank-detail-migration")

                .log("Step 22f completed: employee-bank-detail-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-device-enroll-migration")

                .log("Step 22g completed: employee-device-enroll-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:emp-temp-shift-migration")

                .log("Step 22h completed: emp-temp-shift-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:emp-permanent-shift-migration")

                .log("Step 22i completed: emp-permanent-shift-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-experience-migration")
                .log("Step 22j completed: employee-experience-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-award-migration")
                .log("Step 22k completed: employee-award-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-language-migration")
                .log("Step 22l completed: employee-language-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-seminar-migration")
                .log("Step 22m completed: employee-seminar-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-publication-migration")
                .log("Step 22n completed: employee-publication-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-health-migration")
                .log("Step 22o completed: employee-health-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-training-migration")
                .log("Step 22p completed: employee-training-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-job-description-migration")
                .log("Step 22q completed: employee-job-description-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employment-suspension-migration")
                .log("Step 22r completed: employment-suspension-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-insurance-migration")
                .log("Step 22s completed: employee-insurance-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:skill-master-migration")
                .log("Step 22t completed: skill-master-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-skill-migration")
                .log("Step 22u completed: employee-skill-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-designation-migration")
                .log("Step 22v completed: employee-designation-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-designation-link-migration")
                .log("Step 22w completed: employee-designation-link-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-emergency-contact-migration")
                .log("Step 22x completed: employee-emergency-contact-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-termination-migration")
                .log("Step 22y completed: employee-termination-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:branch-department-head-migration")
                .log("Step 22z completed: branch-department-head-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:org-master-head-link-migration")
                .log("Step 22zj completed: org-master-head-link-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-org-fk-backfill-migration")
                .log("Step 22zk completed: employee-org-fk-backfill-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-job-level-history-migration")
                .log("Step 22za completed: employee-job-level-history-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:job-status-history-migration")
                .log("Step 22zb completed: job-status-history-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:job-position-migration")
                .log("Step 22zc completed: job-position-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:company-employee-contract-migration")
                .log("Step 22zd completed: company-employee-contract-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-job-history-migration")
                .log("Step 22ze completed: employee-job-history-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-document-migration")
                .log("Step 22zf completed: employee-document-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-project-migration")
                .log("Step 22zg completed: employee-project-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:job-category-migration")
                .log("Step 22zh completed: job-category-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:job-categories-migration")
                .log("Step 22zi completed: job-categories-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-leave-accumulation-migration")

                .log("Step 23 completed: employee-leave-accumulation-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:leave-balance-migration")

                .log("Step 23l completed: leave-balance-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:leave-adjustment-migration")

                .log("Step 23m completed: leave-adjustment-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:leave-application-migration")

                .log("Step 23n completed: leave-application-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:leave-cancellation-migration")

                .log("Step 23o completed: leave-cancellation-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:calculated-auto-leave-credit-migration")

                .log("Step 23p completed: calculated-auto-leave-credit-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:calculated-ot-leave-accrual-migration")

                .log("Step 23q completed: calculated-ot-leave-accrual-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-salary-migration")

                .log("Step 23a completed: employee-salary-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:payroll-opening-balance-migration")

                .log("Step 23b completed: payroll-opening-balance-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:month-wise-salary-migration")

                .log("Step 23c completed: month-wise-salary-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:payroll-transaction-history-migration")

                .log("Step 23d completed: payroll-transaction-history-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:mass-salary-adjustment-migration")

                .log("Step 23e completed: mass-salary-adjustment-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-loan-migration")

                .log("Step 23f completed: employee-loan-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:employee-loan-payment-migration")

                .log("Step 23g completed: employee-loan-payment-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:attendance-log-migration")

                .log("Step 23h completed: attendance-log-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:attendance-transaction-migration")

                .log("Step 23i completed: attendance-transaction-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:attendance-forgot-migration")

                .log("Step 23j completed: attendance-forgot-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:attendance-remark-migration")

                .log("Step 23k completed: attendance-remark-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:device-logs-migration")

                .log("Step 23r completed: device-logs-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:temp-device-logs-migration")

                .log("Step 23s completed: temp-device-logs-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:old-attendance-transaction-migration")

                .log("Step 23t completed: old-attendance-transaction-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:work-shift-migration")

                .log("Step 23u completed: work-shift-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:edited-overtime-details-migration")
                .log("Step 23v completed: edited-overtime-details-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:payroll-setting-lookup-migration")
                .log("Step 23w completed: payroll-setting-lookup-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:payroll-overtime-lookup-migration")
                .log("Step 23x completed: payroll-overtime-lookup-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:calculated-type-value-lookup-migration")
                .log("Step 23y completed: calculated-type-value-lookup-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:pay-by-online-transaction-migration")
                .log("Step 23z completed: pay-by-online-transaction-migration")
                .process(exchange -> throttleBetweenSteps())

                .to("direct:company-validity-subscription-migration")

                .log("Step 24a completed: company-validity-subscription-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:subscription-payment-history-migration")

                .log("Step 24b completed: subscription-payment-history-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:user-license-subscription-migration")

                .log("Step 24c completed: user-license-subscription-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:user-migration")

                .log("Step 24 completed: user-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:user-detail-migration")

                .log("Step 25 completed: user-detail-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:user-portal-link-migration")

                .log("Step 26 completed: user-portal-link-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:vacancy-migration")

                .log("Step 27a completed: vacancy-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:vacancy-newspaper-migration")

                .log("Step 27b completed: vacancy-newspaper-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:interview-stage-migration")

                .log("Step 27c completed: interview-stage-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:screening-question-migration")

                .log("Step 27d completed: screening-question-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:applicant-migration")

                .log("Step 27e completed: applicant-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:screening-answer-migration")

                .log("Step 27f completed: screening-answer-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:applicants-transaction-migration")

                .log("Step 27g completed: applicants-transaction-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:recruiters-migration")

                .log("Step 27h completed: recruiters-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:evaluation-migration")

                .log("Step 27i completed: evaluation-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:notice-migration")

                .log("Step 28a completed: notice-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:message-notice-migration")

                .log("Step 28b completed: message-notice-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:company-message-notice-migration")

                .log("Step 28c completed: company-message-notice-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:happening-notice-migration")

                .log("Step 28d completed: happening-notice-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:event-notice-migration")

                .log("Step 28e completed: event-notice-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:notification-notice-migration")

                .log("Step 28f completed: notification-notice-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:notification-viewed-migration")

                .log("Step 28g completed: notification-viewed-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:marketing-person-detail-migration")

                .log("Step 29a completed: marketing-person-detail-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:pricing-estimate-email-details-migration")

                .log("Step 29b completed: pricing-estimate-email-details-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:application-module-lookup-migration")

                .log("Step 29c completed: application-module-lookup-migration")

                .process(migrationRowCountQaProcessor)

                .log("Row-count QA completed (see migration row-count QA report in logs)")

                .process(exchange -> {

                    long endTime = System.currentTimeMillis();

                    long startTime = exchange.getProperty("startTime", Long.class);

                    long totalTime = endTime - startTime;

                    LocalDateTime endDateTime = LocalDateTime.now();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");



                    long hours = totalTime / (1000 * 60 * 60);

                    long minutes = (totalTime % (1000 * 60 * 60)) / (1000 * 60);

                    long seconds = (totalTime % (1000 * 60)) / 1000;

                    long milliseconds = totalTime % 1000;



                    int privilegeCount = exchange.getProperty("privilegeCount", 0, Integer.class);

                    int roleCount = exchange.getProperty("roleCount", 0, Integer.class);

                    int companyCount = exchange.getProperty("companyCount", 0, Integer.class);

                    int companyAddressCount = exchange.getProperty("companyAddressCount", 0, Integer.class);

                    int branchCount = exchange.getProperty("branchCount", 0, Integer.class);

                    int branchAddressCount = exchange.getProperty("branchAddressCount", 0, Integer.class);

                    int fiscalYearCount = exchange.getProperty("fiscalYearCount", 0, Integer.class);

                    int taxationCount = exchange.getProperty("taxationCount", 0, Integer.class);

                    int payrollRuleCount = exchange.getProperty("payrollRuleCount", 0, Integer.class);

                    int salaryBreakdownCount = exchange.getProperty("salaryBreakdownCount", 0, Integer.class);

                    int pmsSalaryBreakdownCount = exchange.getProperty("pmsSalaryBreakdownCount", 0, Integer.class);

                    int gradeCount = exchange.getProperty("gradeCount", 0, Integer.class);

                    int gradePayStepCount = exchange.getProperty("gradePayStepCount", 0, Integer.class);

                    int gradeComponentValueCount = exchange.getProperty("gradeComponentValueCount", 0, Integer.class);

                    int headingTemplateSeedCount = exchange.getProperty("headingTemplateSeedCount", 0, Integer.class);

                    int legacyBankCount = exchange.getProperty("legacyBankCount", 0, Integer.class);

                    int costTypePackageCount = exchange.getProperty("costTypePackageCount", 0, Integer.class);

                    int payPlanPackageCount = exchange.getProperty("payPlanPackageCount", 0, Integer.class);

                    int modulePricingPackageCount =
                            exchange.getProperty("modulePricingPackageCount", 0, Integer.class);

                    int payTypePackageCount = exchange.getProperty("payTypePackageCount", 0, Integer.class);

                    int payrollInstitutionCount =
                            exchange.getProperty("payrollInstitutionCount", 0, Integer.class);
                    int companyPayrollBankCount =
                            exchange.getProperty("companyPayrollBankCount", 0, Integer.class);
                    int companyPayrollInstitutionCount =
                            exchange.getProperty("companyPayrollInstitutionCount", 0, Integer.class);
                    int parentPayrollHeadingCount =
                            exchange.getProperty("parentPayrollHeadingCount", 0, Integer.class);
                    int childPayrollHeadingCount =
                            exchange.getProperty("childPayrollHeadingCount", 0, Integer.class);
                    int companyBranchPayrollHeadingCount =
                            exchange.getProperty("companyBranchPayrollHeadingCount", 0, Integer.class);
                    int payrollLabelCount = exchange.getProperty("payrollLabelCount", 0, Integer.class);
                    int payrollHeadingPriorityCount =
                            exchange.getProperty("payrollHeadingPriorityCount", 0, Integer.class);
                    int payrollHeadingTemplateCount =
                            exchange.getProperty("payrollHeadingTemplateCount", 0, Integer.class);
                    int payrollHeadingDateCount =
                            exchange.getProperty("payrollHeadingDateCount", 0, Integer.class);
                    int payrollHeadingCalculationCount =
                            exchange.getProperty("payrollHeadingCalculationCount", 0, Integer.class);
                    int payPeriodSpecificHeadingCount =
                            exchange.getProperty("payPeriodSpecificHeadingCount", 0, Integer.class);
                    int branchPayPeriodCount =
                            exchange.getProperty("branchPayPeriodCount", 0, Integer.class);

                    int leaveTypeCount = exchange.getProperty("leaveTypeCount", 0, Integer.class);

                    int branchLeaveTypeCount = exchange.getProperty("branchLeaveTypeCount", 0, Integer.class);

                    int attTimeTableShiftCount = exchange.getProperty("attTimeTableShiftCount", 0, Integer.class);

                    int attShiftPatternCount = exchange.getProperty("attShiftPatternCount", 0, Integer.class);

                    int branchHolidayCount = exchange.getProperty("branchHolidayCount", 0, Integer.class);

                    int leaveAccumulationRuleCount =
                            exchange.getProperty("leaveAccumulationRuleCount", 0, Integer.class);

                    int overtimeAccLeaveParamsCount =
                            exchange.getProperty("overtimeAccLeaveParamsCount", 0, Integer.class);

                    int fyClosingParameterCount = exchange.getProperty("fyClosingParameterCount", 0, Integer.class);

                    int attParamsCount = exchange.getProperty("attParamsCount", 0, Integer.class);

                    int deviceMacCount = exchange.getProperty("deviceMacCount", 0, Integer.class);

                    int companySettingParamsCount =
                            exchange.getProperty("companySettingParamsCount", 0, Integer.class);
                    int companyAdminParamsCount =
                            exchange.getProperty("companyAdminParamsCount", 0, Integer.class);
                    int companyEmployeeParamsCount =
                            exchange.getProperty("companyEmployeeParamsCount", 0, Integer.class);
                    int employeeSummaryCount =
                            exchange.getProperty("employeeSummaryCount", 0, Integer.class);

                    int departmentCount = exchange.getProperty("departmentCount", 0, Integer.class);

                    int departmentOrphanCount = exchange.getProperty("departmentOrphanCount", 0, Integer.class);

                    int departmentParentLinkCount = exchange.getProperty("departmentParentLinkCount", 0, Integer.class);

                    int divisionCount = exchange.getProperty("divisionCount", 0, Integer.class);
                    int costCenterCount = exchange.getProperty("costCenterCount", 0, Integer.class);
                    int teamCount = exchange.getProperty("teamCount", 0, Integer.class);

                    int employeeCount = exchange.getProperty("employeeCount", 0, Integer.class);

                    int employeeAddressCount = exchange.getProperty("employeeAddressCount", 0, Integer.class);

                    int employeeMasterAddressCount =
                            exchange.getProperty("employeeMasterAddressCount", 0, Integer.class);

                    int employeeEducationCount = exchange.getProperty("employeeEducationCount", 0, Integer.class);

                    int employeeFamilyCount = exchange.getProperty("employeeFamilyCount", 0, Integer.class);

                    int employeeGradeLinkCount = exchange.getProperty("employeeGradeLinkCount", 0, Integer.class);

                    int employeeBankDetailCount = exchange.getProperty("employeeBankDetailCount", 0, Integer.class);

                    int employeeDeviceEnrollCount =
                            exchange.getProperty("employeeDeviceEnrollCount", 0, Integer.class);

                    int empTempShiftCount = exchange.getProperty("empTempShiftCount", 0, Integer.class);

                    int empPermanentShiftCount = exchange.getProperty("empPermanentShiftCount", 0, Integer.class);

                    int employeeExperienceCount = exchange.getProperty("employeeExperienceCount", 0, Integer.class);
                    int employeeAwardCount = exchange.getProperty("employeeAwardCount", 0, Integer.class);
                    int employeeLanguageCount = exchange.getProperty("employeeLanguageCount", 0, Integer.class);
                    int employeeSeminarCount = exchange.getProperty("employeeSeminarCount", 0, Integer.class);
                    int employeePublicationCount = exchange.getProperty("employeePublicationCount", 0, Integer.class);
                    int employeeHealthCount = exchange.getProperty("employeeHealthCount", 0, Integer.class);
                    int employeeTrainingCount = exchange.getProperty("employeeTrainingCount", 0, Integer.class);
                    int employeeJobDescriptionCount =
                            exchange.getProperty("employeeJobDescriptionCount", 0, Integer.class);
                    int employmentSuspensionCount =
                            exchange.getProperty("employmentSuspensionCount", 0, Integer.class);
                    int employeeInsuranceCount = exchange.getProperty("employeeInsuranceCount", 0, Integer.class);
                    int skillMasterCount = exchange.getProperty("skillMasterCount", 0, Integer.class);
                    int employeeSkillCount = exchange.getProperty("employeeSkillCount", 0, Integer.class);
                    int employeeDesignationCount = exchange.getProperty("employeeDesignationCount", 0, Integer.class);
                    int employeeDesignationLinkCount =
                            exchange.getProperty("employeeDesignationLinkCount", 0, Integer.class);
                    int employeeEmergencyContactCount =
                            exchange.getProperty("employeeEmergencyContactCount", 0, Integer.class);
                    int employeeTerminationCount = exchange.getProperty("employeeTerminationCount", 0, Integer.class);
                    int branchDepartmentHeadCount =
                            exchange.getProperty("branchDepartmentHeadCount", 0, Integer.class);
                    int orgMasterHeadLinkCount = exchange.getProperty("orgMasterHeadLinkCount", 0, Integer.class);
                    int employeeOrgFkBackfillCount =
                            exchange.getProperty("employeeOrgFkBackfillCount", 0, Integer.class);
                    int employeeJobLevelHistoryCount =
                            exchange.getProperty("employeeJobLevelHistoryCount", 0, Integer.class);
                    int jobStatusHistoryCount = exchange.getProperty("jobStatusHistoryCount", 0, Integer.class);
                    int jobPositionCount = exchange.getProperty("jobPositionCount", 0, Integer.class);
                    int companyEmployeeContractCount =
                            exchange.getProperty("companyEmployeeContractCount", 0, Integer.class);
                    int employeeJobHistoryCount =
                            exchange.getProperty("employeeJobHistoryCount", 0, Integer.class);
                    int employeeDocumentCount = exchange.getProperty("employeeDocumentCount", 0, Integer.class);
                    int employeeProjectCount = exchange.getProperty("employeeProjectCount", 0, Integer.class);
                    int jobCategoryCount = exchange.getProperty("jobCategoryCount", 0, Integer.class);
                    int jobCategoriesCount = exchange.getProperty("jobCategoriesCount", 0, Integer.class);

                    int employeeLeaveAccumulationCount =
                            exchange.getProperty("employeeLeaveAccumulationCount", 0, Integer.class);

                    int leaveBalanceCount = exchange.getProperty("leaveBalanceCount", 0, Integer.class);

                    int leaveAdjustmentCount = exchange.getProperty("leaveAdjustmentCount", 0, Integer.class);

                    int leaveApplicationCount = exchange.getProperty("leaveApplicationCount", 0, Integer.class);

                    int leaveCancellationCount = exchange.getProperty("leaveCancellationCount", 0, Integer.class);

                    int calculatedAutoLeaveCreditCount =
                            exchange.getProperty("calculatedAutoLeaveCreditCount", 0, Integer.class);

                    int calculatedOtLeaveAccrualCount =
                            exchange.getProperty("calculatedOtLeaveAccrualCount", 0, Integer.class);

                    int employeeSalaryCount = exchange.getProperty("employeeSalaryCount", 0, Integer.class);

                    int payrollOpeningBalanceCount =
                            exchange.getProperty("payrollOpeningBalanceCount", 0, Integer.class);

                    int monthWiseSalaryCount = exchange.getProperty("monthWiseSalaryCount", 0, Integer.class);

                    int payrollTransactionHistoryCount =
                            exchange.getProperty("payrollTransactionHistoryCount", 0, Integer.class);

                    int massSalaryAdjustmentCount =
                            exchange.getProperty("massSalaryAdjustmentCount", 0, Integer.class);

                    int employeeLoanCount = exchange.getProperty("employeeLoanCount", 0, Integer.class);

                    int employeeLoanPaymentCount =
                            exchange.getProperty("employeeLoanPaymentCount", 0, Integer.class);

                    int attendanceLogCount = exchange.getProperty("attendanceLogCount", 0, Integer.class);

                    int attendanceTransactionCount =
                            exchange.getProperty("attendanceTransactionCount", 0, Integer.class);

                    int attendanceForgotCount = exchange.getProperty("attendanceForgotCount", 0, Integer.class);

                    int attendanceRemarkCount = exchange.getProperty("attendanceRemarkCount", 0, Integer.class);

                    int deviceLogsCount = exchange.getProperty("deviceLogsCount", 0, Integer.class);

                    int tempDeviceLogsCount = exchange.getProperty("tempDeviceLogsCount", 0, Integer.class);

                    int oldAttendanceTransactionCount =
                            exchange.getProperty("oldAttendanceTransactionCount", 0, Integer.class);

                    int workShiftCount = exchange.getProperty("workShiftCount", 0, Integer.class);

                    int editedOvertimeDetailsCount =
                            exchange.getProperty("editedOvertimeDetailsCount", 0, Integer.class);
                    int payrollSettingCount =
                            exchange.getProperty("payrollSettingCount", 0, Integer.class);
                    int payrollOvertimeCount =
                            exchange.getProperty("payrollOvertimeCount", 0, Integer.class);
                    int calculatedTypeValueCount =
                            exchange.getProperty("calculatedTypeValueCount", 0, Integer.class);
                    int payByOnlineTransactionCount =
                            exchange.getProperty("payByOnlineTransactionCount", 0, Integer.class);

                    int companyValiditySubscriptionCount =
                            exchange.getProperty("companyValiditySubscriptionCount", 0, Integer.class);

                    int subscriptionPaymentHistoryCount =
                            exchange.getProperty("subscriptionPaymentHistoryCount", 0, Integer.class);

                    int userLicenseSubscriptionCount =
                            exchange.getProperty("userLicenseSubscriptionCount", 0, Integer.class);

                    int userCount = exchange.getProperty("userCount", 0, Integer.class);

                    int userDetailCount = exchange.getProperty("userDetailCount", 0, Integer.class);

                    int userPortalLinkCount = exchange.getProperty("userPortalLinkCount", 0, Integer.class);

                    int vacancyCount = exchange.getProperty("vacancyCount", 0, Integer.class);
                    int vacancyNewspaperCount = exchange.getProperty("vacancyNewspaperCount", 0, Integer.class);
                    int interviewStageCount = exchange.getProperty("interviewStageCount", 0, Integer.class);
                    int screeningQuestionCount = exchange.getProperty("screeningQuestionCount", 0, Integer.class);
                    int applicantCount = exchange.getProperty("applicantCount", 0, Integer.class);
                    int screeningAnswerCount = exchange.getProperty("screeningAnswerCount", 0, Integer.class);
                    int applicantsTransactionCount =
                            exchange.getProperty("applicantsTransactionCount", 0, Integer.class);
                    int recruitersCount = exchange.getProperty("recruitersCount", 0, Integer.class);
                    int evaluationCount = exchange.getProperty("evaluationCount", 0, Integer.class);

                    int noticeCount = exchange.getProperty("noticeCount", 0, Integer.class);
                    int messageNoticeCount = exchange.getProperty("messageNoticeCount", 0, Integer.class);
                    int companyMessageNoticeCount =
                            exchange.getProperty("companyMessageNoticeCount", 0, Integer.class);
                    int happeningNoticeCount = exchange.getProperty("happeningNoticeCount", 0, Integer.class);
                    int eventNoticeCount = exchange.getProperty("eventNoticeCount", 0, Integer.class);
                    int notificationNoticeCount =
                            exchange.getProperty("notificationNoticeCount", 0, Integer.class);
                    int notificationViewedCount =
                            exchange.getProperty("notificationViewedCount", 0, Integer.class);

                    int marketingPersonDetailCount =
                            exchange.getProperty("marketingPersonDetailCount", 0, Integer.class);
                    int pricingEstimateEmailDetailsCount =
                            exchange.getProperty("pricingEstimateEmailDetailsCount", 0, Integer.class);
                    int applicationModuleLookupCount =
                            exchange.getProperty("applicationModuleLookupCount", 0, Integer.class);

                    log.info("==========================================");

                    log.info("Master migration completed!");

                    log.info("Start Time: {}", exchange.getProperty("startDateTime", LocalDateTime.class).format(formatter));

                    log.info("End Time: {}", endDateTime.format(formatter));

                    log.info("Total Time: {} hours, {} minutes, {} seconds, {} milliseconds",

                            hours, minutes, seconds, milliseconds);

                    log.info("--------------------------------------------");

                    log.info("1. privilege (requestmap -> permission):   {}", privilegeCount);

                    log.info("2. role (secRole -> role):                 {}", roleCount);

                    log.info("3. company (company -> company):           {}", companyCount);

                    log.info("4. company address -> hrm_company_address: {}", companyAddressCount);

                    log.info("5. branch (branch -> branch):              {}", branchCount);

                    log.info("6. branch address -> address:              {}", branchAddressCount);

                    log.info("7. fiscal year -> master/company/branch:   {}", fiscalYearCount);

                    log.info("8. taxation -> nepali_tax:                {}", taxationCount);

                    log.info("9. payroll settings -> payroll_rule:      {}", payrollRuleCount);

                    log.info("9a. companyPayrollHeading -> salary lines: {}", salaryBreakdownCount);

                    log.info("9b. payrollHeading (PMS) -> salary lines:  {}", pmsSalaryBreakdownCount);

                    log.info("9c. jobLevel -> hrm_grade:                 {}", gradeCount);

                    log.info("9d. jobLevelGradeValue -> pay step:        {}", gradePayStepCount);

                    log.info("9e. jobLevelPayroll -> grade component:    {}", gradeComponentValueCount);

                    log.info("9f. template headings -> salary seed:      {}", headingTemplateSeedCount);

                    log.info("9g. bank -> master bank:                   {}", legacyBankCount);

                    log.info("9h. costType -> module_pricing_package:   {}", costTypePackageCount);

                    log.info("9i. payPlan -> module_pricing_package:    {}", payPlanPackageCount);

                    log.info("9j. modulePricing -> package (no scopes): {}", modulePricingPackageCount);

                                        log.info("9k. payType -> module_pricing_package:    {}", payTypePackageCount);

                    log.info("9l. payrollInstitution -> master_lookup: {}", payrollInstitutionCount);
                    log.info("9m. companyPayroll -> company_bank:      {}", companyPayrollBankCount);
                    log.info("9n. companyPayrollInst -> master_lookup: {}", companyPayrollInstitutionCount);
                    log.info("9o. parentPayrollHeading -> lookup:      {}", parentPayrollHeadingCount);
                    log.info("9o2. childPayrollHeading -> lookup:      {}", childPayrollHeadingCount);
                    log.info("9p. companyBranchHeading -> breakdown:   {}", companyBranchPayrollHeadingCount);
                    log.info("9q. payrollLabel -> master_lookup:       {}", payrollLabelCount);
                    log.info("9r. payrollHeadingPriority -> lookup:    {}", payrollHeadingPriorityCount);
                    log.info("9s. payrollHeadingTemplate -> lookup:    {}", payrollHeadingTemplateCount);
                    log.info("9t. payrollHeadingDate -> lookup:        {}", payrollHeadingDateCount);
                    log.info("9u. payrollHeadingCalc -> lookup:        {}", payrollHeadingCalculationCount);
                    log.info("9v. payPeriodSpecificHeading -> lookup:  {}", payPeriodSpecificHeadingCount);
                    log.info("9w. branchPayPeriod -> master_lookup:    {}", branchPayPeriodCount);


                    log.info("10. leaves -> hrm_leave_type:              {}", leaveTypeCount);

                    log.info("11. leaves -> hrm_branch_leave_type:       {}", branchLeaveTypeCount);

                    log.info("12. attTimeTable -> hrm_branch_shift:       {}", attTimeTableShiftCount);

                    log.info("13. attShift -> shift weekday/weekend:      {}", attShiftPatternCount);

                    log.info("14. attHoliday -> hrm_branch_holiday:       {}", branchHolidayCount);

                    log.info("15. autoLeaveAcc -> accumulation rule:      {}", leaveAccumulationRuleCount);

                    log.info("16. overtimeAcc -> leave policy flags:     {}", overtimeAccLeaveParamsCount);

                    log.info("17. FY closing params -> checklist:         {}", fyClosingParameterCount);

                    log.info("18. company defaults / attParams:          {}", attParamsCount);

                                        log.info("18a. attDeviceMAC -> hrm_device_mac:      {}", deviceMacCount);

                    log.info("18b. companySettingParams -> lookup:     {}", companySettingParamsCount);
                    log.info("18c. companyAdminParams -> lookup:       {}", companyAdminParamsCount);
                    log.info("18d. companyEmployeeParams -> lookup:    {}", companyEmployeeParamsCount);
                    log.info("18e. employeeSummary -> employee.notes:  {}", employeeSummaryCount);


                    log.info("19. department (branchDepartment -> dept):  {}", departmentCount);

                    log.info("20. department orphan (department -> dept): {}", departmentOrphanCount);

                    log.info("21. department parent links:               {}", departmentParentLinkCount);

                    log.info("21a. root dept -> division seed:           {}", divisionCount);
                    log.info("21b. branch -> cost center seed:           {}", costCenterCount);
                    log.info("21c. department -> team seed:              {}", teamCount);

                    log.info("22. employee -> employee:                  {}", employeeCount);

                    log.info("22a. employeeAddress -> address:           {}", employeeAddressCount);

                    log.info("22b. employee master -> address:          {}", employeeMasterAddressCount);

                    log.info("22c. employeeEducation -> education:      {}", employeeEducationCount);

                    log.info("22d. family -> employee_family_detail:   {}", employeeFamilyCount);

                    log.info("22e. employeeGrade -> employee.gradeId:  {}", employeeGradeLinkCount);

                    log.info("22f. paymentSetting -> bank detail:      {}", employeeBankDetailCount);

                    log.info("22g. employee enroll -> device enroll:   {}", employeeDeviceEnrollCount);

                    log.info("22h. attEmpTempShift -> temp shift:      {}", empTempShiftCount);

                    log.info("22i. attEmpShift -> employee.branchShift: {}", empPermanentShiftCount);

                    log.info("22j. employeeExperience -> experience:   {}", employeeExperienceCount);
                    log.info("22k. employeeAward -> award:             {}", employeeAwardCount);
                    log.info("22l. employeeLanguage -> language:       {}", employeeLanguageCount);
                    log.info("22m. employeeSeminar -> seminar:         {}", employeeSeminarCount);
                    log.info("22n. employeePublication -> publication: {}", employeePublicationCount);
                    log.info("22o. employeeHealth -> health:           {}", employeeHealthCount);
                    log.info("22p. employeeTraining -> training:       {}", employeeTrainingCount);
                    log.info("22q. jobDescription -> job description:  {}", employeeJobDescriptionCount);
                    log.info("22r. employmentSuspension -> suspension: {}", employmentSuspensionCount);
                    log.info("22s. employeeInsurance -> insurance:     {}", employeeInsuranceCount);
                    log.info("22t. employeeSkill -> skill master:      {}", skillMasterCount);
                    log.info("22u. employeeSkill -> employee skill:    {}", employeeSkillCount);
                    log.info("22v. jobTitle -> designation:            {}", employeeDesignationCount);
                    log.info("22w. employeeJob -> employee.designation: {}", employeeDesignationLinkCount);
                    log.info("22x. employeeContact -> emergency detail: {}", employeeEmergencyContactCount);
                    log.info("22y. employeeTermination -> term date:   {}", employeeTerminationCount);
                    log.info("22z. branchDepartmentHead -> dept head:  {}", branchDepartmentHeadCount);
                    log.info("22zj. heads -> division/team leaders:    {}", orgMasterHeadLinkCount);
                    log.info("22zk. employee org FK backfill:          {}", employeeOrgFkBackfillCount);
                    log.info("22za. employeeJobLevel -> grade history: {}", employeeJobLevelHistoryCount);
                    log.info("22zb. jobStatus -> employment type hist: {}", jobStatusHistoryCount);
                    log.info("22zc. jobPosition -> hire/contract:      {}", jobPositionCount);
                    log.info("22zd. companyEmployeeContract -> contract: {}", companyEmployeeContractCount);
                    log.info("22ze. employeeJob -> designation history: {}", employeeJobHistoryCount);
                    log.info("22zf. document -> employee document:     {}", employeeDocumentCount);
                    log.info("22zg. employeeProject -> experience:     {}", employeeProjectCount);
                    log.info("22zh. jobCategory -> skill category:     {}", jobCategoryCount);
                    log.info("22zi. jobCategories -> skill category:   {}", jobCategoriesCount);

                    log.info("23. leaveAccumulation -> leave_accumulation: {}", employeeLeaveAccumulationCount);

                    log.info("23l. leaveBalance -> hrm_leave_balance:  {}", leaveBalanceCount);

                    log.info("23m. leaveAdjustment -> opening adj:     {}", leaveAdjustmentCount);

                    log.info("23n. leaveApplication -> hrm_leave:      {}", leaveApplicationCount);

                    log.info("23o. leaveCancellation -> leave status:  {}", leaveCancellationCount);

                    log.info("23p. autoLeaveAcc run -> leave_credit:   {}", calculatedAutoLeaveCreditCount);

                    log.info("23q. OT leave balance -> ot accrual:     {}", calculatedOtLeaveAccrualCount);

                    log.info("23a. employeePayrollHeading -> salary:    {}", employeeSalaryCount);

                    log.info("23b. openingPayrollBalance -> opening:    {}", payrollOpeningBalanceCount);

                    log.info("23c. employeePayrollPayment -> month-wise: {}", monthWiseSalaryCount);

                    log.info("23d. payrollTransaction -> month-wise:   {}", payrollTransactionHistoryCount);

                    log.info("23e. massIncrement -> mass salary adj:   {}", massSalaryAdjustmentCount);

                    log.info("23f. employeeLoan -> loan request/acct:  {}", employeeLoanCount);

                    log.info("23g. employeeLoanPayment -> loan payment: {}", employeeLoanPaymentCount);

                    log.info("23h. attLogs -> attendance_log:          {}", attendanceLogCount);

                    log.info("23i. attendanceTransaction -> attendance: {}", attendanceTransactionCount);

                    log.info("23j. attendanceForgot -> time request:   {}", attendanceForgotCount);

                    log.info("23k. attendanceRemark -> attendance.remarks: {}", attendanceRemarkCount);

                    log.info("23r. deviceLogs -> attendance_log:       {}", deviceLogsCount);

                    log.info("23s. tempDeviceLogs -> attendance_log:   {}", tempDeviceLogsCount);

                    log.info("23t. oldAttendanceTransaction -> att:    {}", oldAttendanceTransactionCount);

                                        log.info("23u. workShift -> roster_shift_slot:     {}", workShiftCount);

                    log.info("23v. editedOvertimeDetails -> attendance: {}", editedOvertimeDetailsCount);
                    log.info("23w. payrollSetting -> master_lookup:    {}", payrollSettingCount);
                    log.info("23x. payrollOvertime -> master_lookup:   {}", payrollOvertimeCount);
                    log.info("23y. calculatedTypeValue -> lookup:      {}", calculatedTypeValueCount);
                    log.info("23z. payByOnlineTransaction -> pay hist: {}", payByOnlineTransactionCount);


                    log.info("24a. companyValidity -> subscription:    {}", companyValiditySubscriptionCount);

                    log.info("24b. subscriptionPayment -> pay history: {}", subscriptionPaymentHistoryCount);

                    log.info("24c. userLicense -> sub + pay history:   {}", userLicenseSubscriptionCount);

                    log.info("24. user (secUser -> users):               {}", userCount);

                    log.info("25. user detail (employee -> profile):   {}", userDetailCount);

                    log.info("26. user portal links:                     {}", userPortalLinkCount);

                    log.info("27a. vacancy -> recruitment vacancy:      {}", vacancyCount);
                    log.info("27b. vacancyNewspaper -> publication:     {}", vacancyNewspaperCount);
                    log.info("27c. stages -> interview stage:           {}", interviewStageCount);
                    log.info("27d. screeningQuestion -> screening Q:    {}", screeningQuestionCount);
                    log.info("27e. applicant -> candidate+application:  {}", applicantCount);
                    log.info("27f. screeningAnswer -> screening answer: {}", screeningAnswerCount);
                    log.info("27g. applicantsTransaction -> history:    {}", applicantsTransactionCount);
                    log.info("27h. recruiters -> vacancy.recruiter:     {}", recruitersCount);
                    log.info("27i. evaluation -> screening eval:        {}", evaluationCount);

                    log.info("28a. notice -> company_notice:            {}", noticeCount);
                    log.info("28b. message -> company_notice:           {}", messageNoticeCount);
                    log.info("28c. companyMessage -> company_notice:    {}", companyMessageNoticeCount);
                    log.info("28d. happening -> company_notice:         {}", happeningNoticeCount);
                    log.info("28e. event -> company_notice:             {}", eventNoticeCount);
                    log.info("28f. notification -> company_notice:      {}", notificationNoticeCount);
                    log.info("28g. notificationViewed -> notice_read:   {}", notificationViewedCount);

                    log.info("29a. marketingPerson -> master_lookup:    {}", marketingPersonDetailCount);
                    log.info("29b. pricingEstimate -> master_lookup:    {}", pricingEstimateEmailDetailsCount);
                    log.info("29c. applicationModule -> master_lookup:  {}", applicationModuleLookupCount);

                    Boolean migrationQaPassed = exchange.getProperty("migrationQaPassed", Boolean.class);
                    Integer migrationQaFailureCount =
                            exchange.getProperty("migrationQaFailureCount", 0, Integer.class);
                    if (migrationQaPassed != null) {
                        log.info("QA. row-count validation:                  {} ({} failures)",
                                migrationQaPassed ? "PASSED" : "FAILED",
                                migrationQaFailureCount);
                    }

                    log.info("--------------------------------------------");

                    log.info("GRAND TOTAL:                               {}",

                            privilegeCount + roleCount + companyCount + companyAddressCount + branchCount
                                    + branchAddressCount + fiscalYearCount + taxationCount + payrollRuleCount
                                    + salaryBreakdownCount + pmsSalaryBreakdownCount
                                    + gradeCount + gradePayStepCount + gradeComponentValueCount
                                    + headingTemplateSeedCount + legacyBankCount
                                    + costTypePackageCount + payPlanPackageCount + modulePricingPackageCount
                                    + payTypePackageCount
                                    + payrollInstitutionCount + companyPayrollBankCount
                                    + companyPayrollInstitutionCount + parentPayrollHeadingCount
                                    + childPayrollHeadingCount + companyBranchPayrollHeadingCount
                                    + payrollLabelCount + payrollHeadingPriorityCount
                                    + payrollHeadingTemplateCount + payrollHeadingDateCount
                                    + payrollHeadingCalculationCount + payPeriodSpecificHeadingCount
                                    + branchPayPeriodCount
                                    + leaveTypeCount + branchLeaveTypeCount + attTimeTableShiftCount
                                    + attShiftPatternCount + branchHolidayCount + leaveAccumulationRuleCount
                                    + overtimeAccLeaveParamsCount + fyClosingParameterCount + attParamsCount
                                    + deviceMacCount
                                    + companySettingParamsCount + companyAdminParamsCount
                                    + companyEmployeeParamsCount + employeeSummaryCount
                                    + departmentCount + departmentOrphanCount + departmentParentLinkCount
                                    + divisionCount + costCenterCount + teamCount
                                    + employeeCount + employeeAddressCount + employeeMasterAddressCount
                                    + employeeEducationCount + employeeFamilyCount
                                    + employeeGradeLinkCount + employeeBankDetailCount
                                    + employeeDeviceEnrollCount + empTempShiftCount + empPermanentShiftCount
                                    + employeeExperienceCount + employeeAwardCount + employeeLanguageCount
                                    + employeeSeminarCount + employeePublicationCount + employeeHealthCount
                                    + employeeTrainingCount + employeeJobDescriptionCount
                                    + employmentSuspensionCount + employeeInsuranceCount
                                    + skillMasterCount + employeeSkillCount + employeeDesignationCount
                                    + employeeDesignationLinkCount + employeeEmergencyContactCount
                                    + employeeTerminationCount
                                    + branchDepartmentHeadCount + orgMasterHeadLinkCount
                                    + employeeOrgFkBackfillCount + employeeJobLevelHistoryCount
                                    + jobStatusHistoryCount + jobPositionCount
                                    + companyEmployeeContractCount + employeeJobHistoryCount
                                    + employeeDocumentCount + employeeProjectCount
                                    + jobCategoryCount + jobCategoriesCount
                                    + employeeLeaveAccumulationCount
                                    + leaveBalanceCount + leaveAdjustmentCount
                                    + leaveApplicationCount + leaveCancellationCount
                                    + calculatedAutoLeaveCreditCount + calculatedOtLeaveAccrualCount
                                    + employeeSalaryCount + payrollOpeningBalanceCount
                                    + monthWiseSalaryCount + payrollTransactionHistoryCount
                                    + massSalaryAdjustmentCount + employeeLoanCount + employeeLoanPaymentCount
                                    + attendanceLogCount + attendanceTransactionCount
                                    + attendanceForgotCount + attendanceRemarkCount
                                    + deviceLogsCount + tempDeviceLogsCount
                                    + oldAttendanceTransactionCount + workShiftCount
                                    + editedOvertimeDetailsCount + payrollSettingCount
                                    + payrollOvertimeCount + calculatedTypeValueCount
                                    + payByOnlineTransactionCount
                                    + companyValiditySubscriptionCount + subscriptionPaymentHistoryCount
                                    + userLicenseSubscriptionCount
                                    + userCount + userDetailCount + userPortalLinkCount
                                    + vacancyCount + vacancyNewspaperCount + interviewStageCount
                                    + screeningQuestionCount + applicantCount + screeningAnswerCount
                                    + applicantsTransactionCount + recruitersCount + evaluationCount
                                    + noticeCount + messageNoticeCount + companyMessageNoticeCount
                                    + happeningNoticeCount + eventNoticeCount
                                    + notificationNoticeCount + notificationViewedCount
                                    + marketingPersonDetailCount + pricingEstimateEmailDetailsCount
                                    + applicationModuleLookupCount);

                    log.info("==========================================");

                });



        from("direct:privilege-migration")

                .routeId("privilege-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = requestmapRepository.findAll(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                        log.info("Fetched requestmap page={}, size={}, returnedRows={}, hasNext={}",

                                page, PAGE_SIZE, resultPage.getNumberOfElements(), resultPage.hasNext());

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No requestmap rows in this page, continuing...")

                        .otherwise()

                            .process(privilegeProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "privilege-migration", "privilegeCount"));



        from("direct:role-migration")

                .routeId("role-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = secRoleRepository.findAll(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                        log.info("Fetched secRole page={}, size={}, returnedRows={}, hasNext={}",

                                page, PAGE_SIZE, resultPage.getNumberOfElements(), resultPage.hasNext());

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No secRole rows in this page, continuing...")

                        .otherwise()

                            .process(roleProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "role-migration", "roleCount"));



        from("direct:company-migration")

                .routeId("company-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = companyRepository.findAll(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                        log.info("Fetched company page={}, size={}, returnedRows={}, hasNext={}",

                                page, PAGE_SIZE, resultPage.getNumberOfElements(), resultPage.hasNext());

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No company rows in this page, continuing...")

                        .otherwise()

                            .process(companyProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "company-migration", "companyCount"));



        from("direct:company-address-migration")

                .routeId("company-address-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = companyRepository.findAll(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No company rows for address migration in this page, continuing...")

                        .otherwise()

                            .process(companyAddressProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "company-address-migration", "companyAddressCount"));



        from("direct:branch-migration")

                .routeId("branch-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = branchRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                        log.info("Fetched branch page={}, size={}, returnedRows={}, hasNext={}",

                                page, PAGE_SIZE, resultPage.getNumberOfElements(), resultPage.hasNext());

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No branch rows in this page, continuing...")

                        .otherwise()

                            .process(branchProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "branch-migration", "branchCount"));



        from("direct:branch-address-migration")

                .routeId("branch-address-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = branchRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No branch rows for address migration in this page, continuing...")

                        .otherwise()

                            .process(branchAddressProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "branch-address-migration", "branchAddressCount"));



        from("direct:fiscal-year-migration")

                .routeId("fiscal-year-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = fiscalYearRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No fiscalYear rows in this page, continuing...")

                        .otherwise()

                            .process(fiscalYearProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "fiscal-year-migration", "fiscalYearCount"));



        from("direct:taxation-migration")

                .routeId("taxation-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = taxationRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No taxation rows in this page, continuing...")

                        .otherwise()

                            .process(taxationProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "taxation-migration", "taxationCount"));



        from("direct:payroll-rule-migration")

                .routeId("payroll-rule-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = payrollCalculationSettingRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No payrollCalculationSetting rows in this page, continuing...")

                        .otherwise()

                            .process(payrollRuleProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "payroll-rule-migration", "payrollRuleCount"));



        from("direct:salary-breakdown-migration")

                .routeId("salary-breakdown-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .setProperty("payrollHeadingSource").constant("company")

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = companyPayrollHeadingRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No companyPayrollHeading rows in this page, continuing...")

                        .otherwise()

                            .process(branchSalaryBreakdownProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "salary-breakdown-migration", "salaryBreakdownCount"));



        from("direct:pms-salary-breakdown-migration")

                .routeId("pms-salary-breakdown-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .setProperty("payrollHeadingSource").constant("pms")

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = payrollHeadingRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No payrollHeading rows in this page, continuing...")

                        .otherwise()

                            .process(branchSalaryBreakdownProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "pms-salary-breakdown-migration", "pmsSalaryBreakdownCount"));



        from("direct:grade-migration")
                .routeId("grade-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = jobLevelRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No jobLevel rows in this page, continuing...")
                        .otherwise()
                            .process(gradeProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "grade-migration", "gradeCount"));

        from("direct:grade-pay-step-migration")
                .routeId("grade-pay-step-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = jobLevelGradeValueRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No jobLevelGradeValue rows in this page, continuing...")
                        .otherwise()
                            .process(gradePayStepProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "grade-pay-step-migration", "gradePayStepCount"));

        from("direct:grade-component-migration")
                .routeId("grade-component-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = jobLevelPayrollRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No jobLevelPayroll rows in this page, continuing...")
                        .otherwise()
                            .process(gradeComponentValueProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "grade-component-migration", "gradeComponentValueCount"));

        from("direct:heading-template-seed-migration")
                .routeId("heading-template-seed-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = templatePayrollHeadingRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No templatePayrollHeading rows in this page, continuing...")
                        .otherwise()
                            .process(headingTemplateSeedProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "heading-template-seed-migration", "headingTemplateSeedCount"));

        from("direct:legacy-bank-migration")
                .routeId("legacy-bank-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = bankRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No bank rows in this page, continuing...")
                        .otherwise()
                            .process(legacyBankProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "legacy-bank-migration", "legacyBankCount"));

        from("direct:cost-type-package-migration")
                .routeId("cost-type-package-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = costTypeRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No costType rows in this page, continuing...")
                        .otherwise()
                            .process(costTypePackageProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "cost-type-package-migration", "costTypePackageCount"));

        from("direct:pay-plan-package-migration")
                .routeId("pay-plan-package-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = payPlanRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No payPlan rows in this page, continuing...")
                        .otherwise()
                            .process(payPlanPackageProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "pay-plan-package-migration", "payPlanPackageCount"));

        from("direct:module-pricing-package-migration")
                .routeId("module-pricing-package-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = modulePricingRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No modulePricing rows in this page, continuing...")
                        .otherwise()
                            .process(modulePricingPackageProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange ->
                        finishCount(exchange, "module-pricing-package-migration", "modulePricingPackageCount"));

        from("direct:pay-type-package-migration")
                .routeId("pay-type-package-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = payTypeRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No payType rows in this page, continuing...")
                        .otherwise()
                            .process(payTypePackageProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "pay-type-package-migration", "payTypePackageCount"));

        from("direct:leave-type-migration")

                .routeId("leave-type-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = leavesRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No leaves rows in this page, continuing...")

                        .otherwise()

                            .process(leaveTypeProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "leave-type-migration", "leaveTypeCount"));



        from("direct:branch-leave-type-migration")

                .routeId("branch-leave-type-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = leavesRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No leaves rows for branch assignment in this page, continuing...")

                        .otherwise()

                            .process(branchLeaveTypeProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "branch-leave-type-migration", "branchLeaveTypeCount"));



        from("direct:att-timetable-shift-migration")

                .routeId("att-timetable-shift-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = attTimeTableRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No attTimeTable rows in this page, continuing...")

                        .otherwise()

                            .process(attTimeTableShiftProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "att-timetable-shift-migration", "attTimeTableShiftCount"));



        from("direct:att-shift-pattern-migration")

                .routeId("att-shift-pattern-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = attShiftRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No attShift rows in this page, continuing...")

                        .otherwise()

                            .process(attShiftPatternProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "att-shift-pattern-migration", "attShiftPatternCount"));



        from("direct:branch-holiday-migration")

                .routeId("branch-holiday-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = attHolidayDateRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No attHolidayDate rows in this page, continuing...")

                        .otherwise()

                            .process(branchHolidayProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "branch-holiday-migration", "branchHolidayCount"));



        from("direct:leave-accumulation-rule-migration")

                .routeId("leave-accumulation-rule-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("paramDate").ascending());

                        var resultPage = autoLeaveAccParamsRepository.findDistinctParamDates(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No autoLeaveAccParams paramDate rows in this page, continuing...")

                        .otherwise()

                            .process(leaveAccumulationRuleProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange ->
                        finishCount(exchange, "leave-accumulation-rule-migration", "leaveAccumulationRuleCount"));



        from("direct:overtime-acc-leave-params-migration")

                .routeId("overtime-acc-leave-params-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("paramDate").ascending());

                        var resultPage = overtimeAccLeaveParamsRepository.findDistinctParamDates(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No overtimeAccLeaveParams paramDate rows in this page, continuing...")

                        .otherwise()

                            .process(overtimeAccLeaveParamsProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange ->
                        finishCount(exchange, "overtime-acc-leave-params-migration", "overtimeAccLeaveParamsCount"));



        from("direct:fy-closing-parameter-migration")

                .routeId("fy-closing-parameter-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = companyFiscalYearClosingParameterRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No companyFiscalYearClosingParameter rows in this page, continuing...")

                        .otherwise()

                            .process(fiscalYearClosingParameterProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "fy-closing-parameter-migration", "fyClosingParameterCount"));



        from("direct:att-params-migration")

                .routeId("att-params-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = companyRepository.findAll(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No company rows for att params / defaults in this page, continuing...")

                        .otherwise()

                            .process(attParamsProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "att-params-migration", "attParamsCount"));

        from("direct:device-mac-migration")
                .routeId("device-mac-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = attDeviceMACRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No attDeviceMAC rows in this page, continuing...")
                        .otherwise()
                            .process(attDeviceMacProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "device-mac-migration", "deviceMacCount"));

        from("direct:department-migration")

                .routeId("department-migration")

                .setProperty(DepartmentProcessor.MIGRATION_SOURCE).constant(DepartmentProcessor.SOURCE_BRANCH_DEPARTMENT)

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = branchDepartmentRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                        log.info("Fetched branchDepartment page={}, size={}, returnedRows={}, hasNext={}",

                                page, PAGE_SIZE, resultPage.getNumberOfElements(), resultPage.hasNext());

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No branchDepartment rows in this page, continuing...")

                        .otherwise()

                            .process(departmentProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "department-migration", "departmentCount"));



        from("direct:department-orphan-migration")

                .routeId("department-orphan-migration")

                .setProperty(DepartmentProcessor.MIGRATION_SOURCE).constant(DepartmentProcessor.SOURCE_ORPHAN)

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = departmentRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                        log.info("Fetched department page={}, size={}, returnedRows={}, hasNext={}",

                                page, PAGE_SIZE, resultPage.getNumberOfElements(), resultPage.hasNext());

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No department rows in this page, continuing...")

                        .otherwise()

                            .process(departmentProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "department-orphan-migration", "departmentOrphanCount"));



        from("direct:department-parent-link")

                .routeId("department-parent-link")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = departmentRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No department rows for parent-link in this page, continuing...")

                        .otherwise()

                            .process(departmentParentLinkProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "department-parent-link", "departmentParentLinkCount"));

        from("direct:division-seed-migration")
                .routeId("division-seed-migration")
                .setProperty("importCount").constant(0)
                .process(divisionSeedProcessor)
                .process(exchange -> addImported(exchange))
                .process(exchange -> finishCount(exchange, "division-seed-migration", "divisionCount"));

        from("direct:cost-center-seed-migration")
                .routeId("cost-center-seed-migration")
                .setProperty("importCount").constant(0)
                .process(costCenterSeedProcessor)
                .process(exchange -> addImported(exchange))
                .process(exchange -> finishCount(exchange, "cost-center-seed-migration", "costCenterCount"));

        from("direct:team-seed-migration")
                .routeId("team-seed-migration")
                .setProperty("importCount").constant(0)
                .process(teamSeedProcessor)
                .process(exchange -> addImported(exchange))
                .process(exchange -> finishCount(exchange, "team-seed-migration", "teamCount"));

        from("direct:employee-migration")

                .routeId("employee-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = employeeRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No employee rows in this page, continuing...")

                        .otherwise()

                            .process(employeeProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "employee-migration", "employeeCount"));



        from("direct:employee-address-migration")

                .routeId("employee-address-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = employeeAddressRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No employeeAddress rows in this page, continuing...")

                        .otherwise()

                            .process(employeeAddressProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "employee-address-migration", "employeeAddressCount"));



        from("direct:employee-master-address-migration")

                .routeId("employee-master-address-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = employeeRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No employee master address rows in this page, continuing...")

                        .otherwise()

                            .process(employeeMasterAddressProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange ->
                        finishCount(exchange, "employee-master-address-migration", "employeeMasterAddressCount"));



        from("direct:employee-education-migration")

                .routeId("employee-education-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = employeeEducationRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No employeeEducation rows in this page, continuing...")

                        .otherwise()

                            .process(employeeEducationProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "employee-education-migration", "employeeEducationCount"));



        from("direct:employee-family-migration")

                .routeId("employee-family-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = familyRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No family rows in this page, continuing...")

                        .otherwise()

                            .process(employeeFamilyProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "employee-family-migration", "employeeFamilyCount"));

        from("direct:employee-grade-link-migration")
                .routeId("employee-grade-link-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeGradeRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeeGrade rows in this page, continuing...")
                        .otherwise()
                            .process(employeeGradeLinkProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-grade-link-migration", "employeeGradeLinkCount"));

        from("direct:employee-bank-detail-migration")
                .routeId("employee-bank-detail-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeePayrollPaymentSettingRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeePayrollPaymentSetting rows in this page, continuing...")
                        .otherwise()
                            .process(employeeBankDetailProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-bank-detail-migration", "employeeBankDetailCount"));

        from("direct:employee-device-enroll-migration")
                .routeId("employee-device-enroll-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employee rows for device enroll in this page, continuing...")
                        .otherwise()
                            .process(employeeDeviceEnrollProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-device-enroll-migration", "employeeDeviceEnrollCount"));

        from("direct:emp-temp-shift-migration")
                .routeId("emp-temp-shift-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = attEmpTempShiftRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No attEmpTempShift rows in this page, continuing...")
                        .otherwise()
                            .process(employeeTempShiftProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "emp-temp-shift-migration", "empTempShiftCount"));

        from("direct:emp-permanent-shift-migration")
                .routeId("emp-permanent-shift-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = attEmpShiftRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No attEmpShift rows in this page, continuing...")
                        .otherwise()
                            .process(employeePermanentShiftProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "emp-permanent-shift-migration", "empPermanentShiftCount"));

        from("direct:employee-experience-migration")
                .routeId("employee-experience-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeExperienceRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeeExperience rows in this page, continuing...")
                        .otherwise()
                            .process(employeeExperienceProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-experience-migration", "employeeExperienceCount"));

        from("direct:employee-award-migration")
                .routeId("employee-award-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeAwardRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeeAward rows in this page, continuing...")
                        .otherwise()
                            .process(employeeAwardProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-award-migration", "employeeAwardCount"));

        from("direct:employee-language-migration")
                .routeId("employee-language-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeLanguageRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeeLanguage rows in this page, continuing...")
                        .otherwise()
                            .process(employeeLanguageProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-language-migration", "employeeLanguageCount"));

        from("direct:employee-seminar-migration")
                .routeId("employee-seminar-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeSeminarRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeeSeminar rows in this page, continuing...")
                        .otherwise()
                            .process(employeeSeminarProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-seminar-migration", "employeeSeminarCount"));

        from("direct:employee-publication-migration")
                .routeId("employee-publication-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeePublicationRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeePublication rows in this page, continuing...")
                        .otherwise()
                            .process(employeePublicationProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-publication-migration", "employeePublicationCount"));

        from("direct:employee-health-migration")
                .routeId("employee-health-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeHealthRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeeHealth rows in this page, continuing...")
                        .otherwise()
                            .process(employeeHealthProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-health-migration", "employeeHealthCount"));

        from("direct:employee-training-migration")
                .routeId("employee-training-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeTrainingRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeeTraining rows in this page, continuing...")
                        .otherwise()
                            .process(employeeTrainingProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-training-migration", "employeeTrainingCount"));

        from("direct:employee-job-description-migration")
                .routeId("employee-job-description-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = jobDescriptionRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No jobDescription rows in this page, continuing...")
                        .otherwise()
                            .process(employeeJobDescriptionProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange ->
                        finishCount(exchange, "employee-job-description-migration", "employeeJobDescriptionCount"));

        from("direct:employment-suspension-migration")
                .routeId("employment-suspension-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employmentSuspensionRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employmentSuspension rows in this page, continuing...")
                        .otherwise()
                            .process(employmentSuspensionProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange ->
                        finishCount(exchange, "employment-suspension-migration", "employmentSuspensionCount"));

        from("direct:employee-insurance-migration")
                .routeId("employee-insurance-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeInsuranceRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeeInsurance rows in this page, continuing...")
                        .otherwise()
                            .process(employeeInsuranceProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-insurance-migration", "employeeInsuranceCount"));

        from("direct:skill-master-migration")
                .routeId("skill-master-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeSkillRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeeSkill rows for skill master in this page, continuing...")
                        .otherwise()
                            .process(skillMasterProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "skill-master-migration", "skillMasterCount"));

        from("direct:employee-skill-migration")
                .routeId("employee-skill-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeSkillRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeeSkill rows in this page, continuing...")
                        .otherwise()
                            .process(employeeSkillProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-skill-migration", "employeeSkillCount"));

        from("direct:employee-designation-migration")
                .routeId("employee-designation-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = jobTitleRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No jobTitle rows in this page, continuing...")
                        .otherwise()
                            .process(employeeDesignationProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-designation-migration", "employeeDesignationCount"));

        from("direct:employee-designation-link-migration")
                .routeId("employee-designation-link-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeJobRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No active employeeJob rows in this page, continuing...")
                        .otherwise()
                            .process(employeeDesignationLinkProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange ->
                        finishCount(exchange, "employee-designation-link-migration", "employeeDesignationLinkCount"));

        from("direct:employee-emergency-contact-migration")
                .routeId("employee-emergency-contact-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeContactRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeeContact rows in this page, continuing...")
                        .otherwise()
                            .process(employeeEmergencyContactProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange ->
                        finishCount(exchange, "employee-emergency-contact-migration", "employeeEmergencyContactCount"));

        from("direct:employee-termination-migration")
                .routeId("employee-termination-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeTerminationRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeeTermination rows in this page, continuing...")
                        .otherwise()
                            .process(employeeTerminationProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-termination-migration", "employeeTerminationCount"));

        from("direct:branch-department-head-migration")
                .routeId("branch-department-head-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = branchDepartmentHeadRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No branchDepartmentHead rows in this page, continuing...")
                        .otherwise()
                            .process(branchDepartmentHeadProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange ->
                        finishCount(exchange, "branch-department-head-migration", "branchDepartmentHeadCount"));

        from("direct:org-master-head-link-migration")
                .routeId("org-master-head-link-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = branchDepartmentHeadRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No branchDepartmentHead rows for org-master head-link in this page, continuing...")
                        .otherwise()
                            .process(orgMasterHeadLinkProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange ->
                        finishCount(exchange, "org-master-head-link-migration", "orgMasterHeadLinkCount"));

        from("direct:employee-org-fk-backfill-migration")
                .routeId("employee-org-fk-backfill-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = pgEmployeeRepository.findAll(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employee rows for org-FK backfill in this page, continuing...")
                        .otherwise()
                            .process(employeeOrgFkBackfillProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange ->
                        finishCount(exchange, "employee-org-fk-backfill-migration", "employeeOrgFkBackfillCount"));

        from("direct:employee-job-level-history-migration")
                .routeId("employee-job-level-history-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeJobLevelRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeeJobLevel rows in this page, continuing...")
                        .otherwise()
                            .process(employeeJobLevelHistoryProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(
                        exchange, "employee-job-level-history-migration", "employeeJobLevelHistoryCount"));

        from("direct:job-status-history-migration")
                .routeId("job-status-history-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = jobStatusRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No jobStatus rows in this page, continuing...")
                        .otherwise()
                            .process(jobStatusHistoryProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "job-status-history-migration", "jobStatusHistoryCount"));

        from("direct:job-position-migration")
                .routeId("job-position-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = jobPositionRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No jobPosition rows in this page, continuing...")
                        .otherwise()
                            .process(jobPositionProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "job-position-migration", "jobPositionCount"));

        from("direct:company-employee-contract-migration")
                .routeId("company-employee-contract-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = companyEmployeeContractRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No companyEmployeeContract rows in this page, continuing...")
                        .otherwise()
                            .process(companyEmployeeContractProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(
                        exchange, "company-employee-contract-migration", "companyEmployeeContractCount"));

        from("direct:employee-job-history-migration")
                .routeId("employee-job-history-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeJobRepository.findAllForHistory(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeeJob history rows in this page, continuing...")
                        .otherwise()
                            .process(employeeJobHistoryProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange ->
                        finishCount(exchange, "employee-job-history-migration", "employeeJobHistoryCount"));

        from("direct:employee-document-migration")
                .routeId("employee-document-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = documentRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No document rows in this page, continuing...")
                        .otherwise()
                            .process(documentProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-document-migration", "employeeDocumentCount"));

        from("direct:employee-project-migration")
                .routeId("employee-project-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeProjectRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeeProject rows in this page, continuing...")
                        .otherwise()
                            .process(employeeProjectProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-project-migration", "employeeProjectCount"));

        from("direct:job-category-migration")
                .routeId("job-category-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = jobCategoryRepository.findAll(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No jobCategory rows in this page, continuing...")
                        .otherwise()
                            .process(jobCategoryProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "job-category-migration", "jobCategoryCount"));

        from("direct:job-categories-migration")
                .routeId("job-categories-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = jobCategoriesRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No jobCategories rows in this page, continuing...")
                        .otherwise()
                            .process(jobCategoriesProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "job-categories-migration", "jobCategoriesCount"));

        from("direct:employee-leave-accumulation-migration")

                .routeId("employee-leave-accumulation-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = leaveAccumulationRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No leaveAccumulation rows in this page, continuing...")

                        .otherwise()

                            .process(employeeLeaveAccumulationProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange ->
                        finishCount(exchange, "employee-leave-accumulation-migration", "employeeLeaveAccumulationCount"));

        from("direct:leave-balance-migration")
                .routeId("leave-balance-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = leaveBalanceRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No leaveBalance rows in this page, continuing...")
                        .otherwise()
                            .process(leaveBalanceProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "leave-balance-migration", "leaveBalanceCount"));

        from("direct:leave-adjustment-migration")
                .routeId("leave-adjustment-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = leaveAdjustmentRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No leaveAdjustment rows in this page, continuing...")
                        .otherwise()
                            .process(leaveAdjustmentProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "leave-adjustment-migration", "leaveAdjustmentCount"));

        from("direct:leave-application-migration")
                .routeId("leave-application-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = leaveApplicationRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No leaveApplication rows in this page, continuing...")
                        .otherwise()
                            .process(leaveApplicationProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "leave-application-migration", "leaveApplicationCount"));

        from("direct:leave-cancellation-migration")
                .routeId("leave-cancellation-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = leaveCancellationRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No leaveCancellation rows in this page, continuing...")
                        .otherwise()
                            .process(leaveCancellationProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "leave-cancellation-migration", "leaveCancellationCount"));

        from("direct:calculated-auto-leave-credit-migration")
                .routeId("calculated-auto-leave-credit-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = calculatedAutoLeaveAccumulationRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No calculatedAutoLeaveAccumulation rows in this page, continuing...")
                        .otherwise()
                            .process(calculatedAutoLeaveCreditProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange ->
                        finishCount(exchange, "calculated-auto-leave-credit-migration", "calculatedAutoLeaveCreditCount"));

        from("direct:calculated-ot-leave-accrual-migration")
                .routeId("calculated-ot-leave-accrual-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = calculatedOTLeaveBalanceRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No calculatedOTLeaveBalance rows in this page, continuing...")
                        .otherwise()
                            .process(calculatedOtLeaveAccrualProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange ->
                        finishCount(exchange, "calculated-ot-leave-accrual-migration", "calculatedOtLeaveAccrualCount"));

        from("direct:employee-salary-migration")

                .routeId("employee-salary-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = employeePayrollHeadingRepository.findMigratableOpen(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No open employeePayrollHeading rows in this page, continuing...")

                        .otherwise()

                            .process(employeeSalaryProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "employee-salary-migration", "employeeSalaryCount"));



        from("direct:payroll-opening-balance-migration")

                .routeId("payroll-opening-balance-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = openingPayrollBalanceRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No openingPayrollBalance rows in this page, continuing...")

                        .otherwise()

                            .process(payrollOpeningBalanceProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange ->
                        finishCount(exchange, "payroll-opening-balance-migration", "payrollOpeningBalanceCount"));



        from("direct:month-wise-salary-migration")

                .routeId("month-wise-salary-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = employeePayrollPaymentRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No employeePayrollPayment rows in this page, continuing...")

                        .otherwise()

                            .process(monthWiseSalaryProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "month-wise-salary-migration", "monthWiseSalaryCount"));



        from("direct:payroll-transaction-history-migration")

                .routeId("payroll-transaction-history-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = payrollMonthRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No payrollMonth rows in this page, continuing...")

                        .otherwise()

                            .process(payrollTransactionHistoryProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange ->
                        finishCount(exchange, "payroll-transaction-history-migration", "payrollTransactionHistoryCount"));

        from("direct:mass-salary-adjustment-migration")
                .routeId("mass-salary-adjustment-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = massIncrementRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No massIncrement rows in this page, continuing...")
                        .otherwise()
                            .process(massSalaryAdjustmentProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "mass-salary-adjustment-migration", "massSalaryAdjustmentCount"));

        from("direct:employee-loan-migration")
                .routeId("employee-loan-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeLoanRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeeLoan rows in this page, continuing...")
                        .otherwise()
                            .process(employeeLoanProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-loan-migration", "employeeLoanCount"));

        from("direct:employee-loan-payment-migration")
                .routeId("employee-loan-payment-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeLoanPaymentRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeeLoanPayment rows in this page, continuing...")
                        .otherwise()
                            .process(employeeLoanPaymentProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-loan-payment-migration", "employeeLoanPaymentCount"));

        from("direct:attendance-log-migration")
                .routeId("attendance-log-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = attLogsRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No attLogs rows in this page, continuing...")
                        .otherwise()
                            .process(attendanceLogProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "attendance-log-migration", "attendanceLogCount"));

        from("direct:attendance-transaction-migration")
                .routeId("attendance-transaction-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = attendanceTransactionRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No attendanceTransaction rows in this page, continuing...")
                        .otherwise()
                            .process(attendanceTransactionProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange ->
                        finishCount(exchange, "attendance-transaction-migration", "attendanceTransactionCount"));

        from("direct:attendance-forgot-migration")
                .routeId("attendance-forgot-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = attendanceForgotRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No attendanceForgot rows in this page, continuing...")
                        .otherwise()
                            .process(attendanceForgotProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "attendance-forgot-migration", "attendanceForgotCount"));

        from("direct:attendance-remark-migration")
                .routeId("attendance-remark-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = attendanceRemarkRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No attendanceRemark rows in this page, continuing...")
                        .otherwise()
                            .process(attendanceRemarkProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "attendance-remark-migration", "attendanceRemarkCount"));

        from("direct:device-logs-migration")
                .routeId("device-logs-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = deviceLogsRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No deviceLogs rows in this page, continuing...")
                        .otherwise()
                            .process(deviceLogsProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "device-logs-migration", "deviceLogsCount"));

        from("direct:temp-device-logs-migration")
                .routeId("temp-device-logs-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = tempDeviceLogsRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No tempDeviceLogs rows in this page, continuing...")
                        .otherwise()
                            .process(tempDeviceLogsProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "temp-device-logs-migration", "tempDeviceLogsCount"));

        from("direct:old-attendance-transaction-migration")
                .routeId("old-attendance-transaction-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = oldAttendanceTransactionRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No oldAttendanceTransaction rows in this page, continuing...")
                        .otherwise()
                            .process(oldAttendanceTransactionProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(
                        exchange, "old-attendance-transaction-migration", "oldAttendanceTransactionCount"));

        from("direct:work-shift-migration")
                .routeId("work-shift-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = workShiftRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No workShift rows in this page, continuing...")
                        .otherwise()
                            .process(workShiftProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "work-shift-migration", "workShiftCount"));

        from("direct:company-validity-subscription-migration")
                .routeId("company-validity-subscription-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = companyValidityRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No companyValidity rows in this page, continuing...")
                        .otherwise()
                            .process(companyValiditySubscriptionProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(
                        exchange, "company-validity-subscription-migration", "companyValiditySubscriptionCount"));

        from("direct:subscription-payment-history-migration")
                .routeId("subscription-payment-history-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = subscriptionPaymentRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No subscriptionPayment rows in this page, continuing...")
                        .otherwise()
                            .process(subscriptionPaymentHistoryProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(
                        exchange, "subscription-payment-history-migration", "subscriptionPaymentHistoryCount"));

        from("direct:user-license-subscription-migration")
                .routeId("user-license-subscription-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = userLicenseRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No userLicense rows in this page, continuing...")
                        .otherwise()
                            .process(userLicenseSubscriptionProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(
                        exchange, "user-license-subscription-migration", "userLicenseSubscriptionCount"));

        from("direct:user-migration")

                .routeId("user-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = secUserRepository.findAll(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                        log.info("Fetched secUser page={}, size={}, returnedRows={}, hasNext={}",

                                page, PAGE_SIZE, resultPage.getNumberOfElements(), resultPage.hasNext());

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No secUser rows in this page, continuing...")

                        .otherwise()

                            .process(userProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "user-migration", "userCount"));



        from("direct:user-detail-migration")

                .routeId("user-detail-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = secUserRepository.findAll(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                        log.info("Fetched secUser page for user detail={}, size={}, returnedRows={}, hasNext={}",

                                page, PAGE_SIZE, resultPage.getNumberOfElements(), resultPage.hasNext());

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No secUser rows in this page for user detail, continuing...")

                        .otherwise()

                            .process(userDetailProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "user-detail-migration", "userDetailCount"));



        from("direct:user-portal-link-migration")

                .routeId("user-portal-link-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = secUserRepository.findAll(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No secUser rows in this page for user portal links, continuing...")

                        .otherwise()

                            .process(userPortalLinkProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "user-portal-link-migration", "userPortalLinkCount"));

        from("direct:vacancy-migration")
                .routeId("vacancy-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = vacancyRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No vacancy rows in this page, continuing...")
                        .otherwise()
                            .process(vacancyProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "vacancy-migration", "vacancyCount"));

        from("direct:vacancy-newspaper-migration")
                .routeId("vacancy-newspaper-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = vacancyNewspaperRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No vacancyNewspaper rows in this page, continuing...")
                        .otherwise()
                            .process(vacancyNewspaperProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "vacancy-newspaper-migration", "vacancyNewspaperCount"));

        from("direct:interview-stage-migration")
                .routeId("interview-stage-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = stagesRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No stages rows in this page, continuing...")
                        .otherwise()
                            .process(interviewStageProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "interview-stage-migration", "interviewStageCount"));

        from("direct:screening-question-migration")
                .routeId("screening-question-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = screeningQuestionRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No screeningQuestion rows in this page, continuing...")
                        .otherwise()
                            .process(screeningQuestionProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "screening-question-migration", "screeningQuestionCount"));

        from("direct:applicant-migration")
                .routeId("applicant-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = applicantRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No applicant rows in this page, continuing...")
                        .otherwise()
                            .process(applicantProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "applicant-migration", "applicantCount"));

        from("direct:screening-answer-migration")
                .routeId("screening-answer-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = screeningAnswerRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No screeningAnswer rows in this page, continuing...")
                        .otherwise()
                            .process(screeningAnswerProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "screening-answer-migration", "screeningAnswerCount"));

        from("direct:applicants-transaction-migration")
                .routeId("applicants-transaction-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = applicantsTransactionRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No applicantsTransaction rows in this page, continuing...")
                        .otherwise()
                            .process(applicantsTransactionProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange ->
                        finishCount(exchange, "applicants-transaction-migration", "applicantsTransactionCount"));

        from("direct:recruiters-migration")
                .routeId("recruiters-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = recruitersRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No recruiters rows in this page, continuing...")
                        .otherwise()
                            .process(recruitersProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "recruiters-migration", "recruitersCount"));

        from("direct:evaluation-migration")
                .routeId("evaluation-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = evaluationRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No evaluation rows in this page, continuing...")
                        .otherwise()
                            .process(evaluationProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "evaluation-migration", "evaluationCount"));

        from("direct:notice-migration")
                .routeId("notice-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = noticeRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No notice rows in this page, continuing...")
                        .otherwise()
                            .process(noticeProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "notice-migration", "noticeCount"));

        from("direct:message-notice-migration")
                .routeId("message-notice-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = messageRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No message rows in this page, continuing...")
                        .otherwise()
                            .process(messageNoticeProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "message-notice-migration", "messageNoticeCount"));

        from("direct:company-message-notice-migration")
                .routeId("company-message-notice-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = companyMessageCompanyRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No companyMessageCompany rows in this page, continuing...")
                        .otherwise()
                            .process(companyMessageNoticeProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange ->
                        finishCount(exchange, "company-message-notice-migration", "companyMessageNoticeCount"));

        from("direct:happening-notice-migration")
                .routeId("happening-notice-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = happeningRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No happening rows in this page, continuing...")
                        .otherwise()
                            .process(happeningNoticeProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "happening-notice-migration", "happeningNoticeCount"));

        from("direct:event-notice-migration")
                .routeId("event-notice-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = eventRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No event rows in this page, continuing...")
                        .otherwise()
                            .process(eventNoticeProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "event-notice-migration", "eventNoticeCount"));

        from("direct:notification-notice-migration")
                .routeId("notification-notice-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = notificationRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No notification rows in this page, continuing...")
                        .otherwise()
                            .process(notificationNoticeProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange ->
                        finishCount(exchange, "notification-notice-migration", "notificationNoticeCount"));

        from("direct:notification-viewed-migration")
                .routeId("notification-viewed-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = notificationViewedRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No notificationViewed rows in this page, continuing...")
                        .otherwise()
                            .process(notificationViewedProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange ->
                        finishCount(exchange, "notification-viewed-migration", "notificationViewedCount"));

        from("direct:marketing-person-detail-migration")
                .routeId("marketing-person-detail-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = marketingPersonDetailRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No marketingPersonDetail rows in this page, continuing...")
                        .otherwise()
                            .process(marketingPersonDetailProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange ->
                        finishCount(exchange, "marketing-person-detail-migration", "marketingPersonDetailCount"));

        from("direct:pricing-estimate-email-details-migration")
                .routeId("pricing-estimate-email-details-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = pricingEstimateEmailDetailsRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No pricingEstimateEmailDetails rows in this page, continuing...")
                        .otherwise()
                            .process(pricingEstimateEmailDetailsProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(
                        exchange, "pricing-estimate-email-details-migration", "pricingEstimateEmailDetailsCount"));

        from("direct:application-module-lookup-migration")
                .routeId("application-module-lookup-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = applicationModuleRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No applicationModule rows in this page, continuing...")
                        .otherwise()
                            .process(applicationModuleLookupProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange ->
                        finishCount(exchange, "application-module-lookup-migration", "applicationModuleLookupCount"));

        from("direct:payroll-institution-lookup-migration")
                .routeId("payroll-institution-lookup-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = payrollInstitutionRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No payrollInstitution rows in this page, continuing...")
                        .otherwise()
                            .process(payrollInstitutionLookupProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "payroll-institution-lookup-migration", "payrollInstitutionCount"));

        from("direct:company-payroll-bank-migration")
                .routeId("company-payroll-bank-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = companyPayrollRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No companyPayroll rows in this page, continuing...")
                        .otherwise()
                            .process(companyPayrollBankProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "company-payroll-bank-migration", "companyPayrollBankCount"));

        from("direct:company-payroll-institution-migration")
                .routeId("company-payroll-institution-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = companyPayrollInstitutionRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No companyPayrollInstitution rows in this page, continuing...")
                        .otherwise()
                            .process(companyPayrollInstitutionProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "company-payroll-institution-migration", "companyPayrollInstitutionCount"));

        from("direct:parent-payroll-heading-lookup-migration")
                .routeId("parent-payroll-heading-lookup-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = parentPayrollHeadingRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No parentPayrollHeading rows in this page, continuing...")
                        .otherwise()
                            .process(parentPayrollHeadingLookupProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "parent-payroll-heading-lookup-migration", "parentPayrollHeadingCount"));

        from("direct:child-payroll-heading-lookup-migration")
                .routeId("child-payroll-heading-lookup-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = childPayrollHeadingRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No childPayrollHeading rows in this page, continuing...")
                        .otherwise()
                            .process(childPayrollHeadingLookupProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "child-payroll-heading-lookup-migration", "childPayrollHeadingCount"));

        from("direct:company-branch-payroll-heading-migration")
                .routeId("company-branch-payroll-heading-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = companyBranchPayrollHeadingRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No companyBranchPayrollHeading rows in this page, continuing...")
                        .otherwise()
                            .process(companyBranchPayrollHeadingProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "company-branch-payroll-heading-migration", "companyBranchPayrollHeadingCount"));

        from("direct:payroll-label-lookup-migration")
                .routeId("payroll-label-lookup-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = payrollLabelRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No payrollLabel rows in this page, continuing...")
                        .otherwise()
                            .process(payrollLabelLookupProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "payroll-label-lookup-migration", "payrollLabelCount"));

        from("direct:payroll-heading-priority-lookup-migration")
                .routeId("payroll-heading-priority-lookup-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = payrollHeadingPriorityRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No payrollHeadingPriority rows in this page, continuing...")
                        .otherwise()
                            .process(payrollHeadingPriorityLookupProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "payroll-heading-priority-lookup-migration", "payrollHeadingPriorityCount"));

        from("direct:payroll-heading-template-lookup-migration")
                .routeId("payroll-heading-template-lookup-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = payrollHeadingTemplateRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No payrollHeadingTemplate rows in this page, continuing...")
                        .otherwise()
                            .process(payrollHeadingTemplateLookupProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "payroll-heading-template-lookup-migration", "payrollHeadingTemplateCount"));

        from("direct:payroll-heading-date-lookup-migration")
                .routeId("payroll-heading-date-lookup-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = payrollHeadingDateRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No payrollHeadingDate rows in this page, continuing...")
                        .otherwise()
                            .process(payrollHeadingDateLookupProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "payroll-heading-date-lookup-migration", "payrollHeadingDateCount"));

        from("direct:payroll-heading-calculation-lookup-migration")
                .routeId("payroll-heading-calculation-lookup-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = payrollHeadingCalculationRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No payrollHeadingCalculation rows in this page, continuing...")
                        .otherwise()
                            .process(payrollHeadingCalculationLookupProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "payroll-heading-calculation-lookup-migration", "payrollHeadingCalculationCount"));

        from("direct:pay-period-specific-heading-lookup-migration")
                .routeId("pay-period-specific-heading-lookup-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = payPeriodSpecificHeadingRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No payPeriodSpecificHeading rows in this page, continuing...")
                        .otherwise()
                            .process(payPeriodSpecificHeadingLookupProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "pay-period-specific-heading-lookup-migration", "payPeriodSpecificHeadingCount"));

        from("direct:branch-pay-period-lookup-migration")
                .routeId("branch-pay-period-lookup-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = branchPayPeriodRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No branchPayPeriod rows in this page, continuing...")
                        .otherwise()
                            .process(branchPayPeriodLookupProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "branch-pay-period-lookup-migration", "branchPayPeriodCount"));

        from("direct:company-setting-params-migration")
                .routeId("company-setting-params-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = companySettingParamsRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No companySettingParams rows in this page, continuing...")
                        .otherwise()
                            .process(companySettingParamsProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "company-setting-params-migration", "companySettingParamsCount"));

        from("direct:company-admin-params-migration")
                .routeId("company-admin-params-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = companyAdminParamsRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No companyAdminParams rows in this page, continuing...")
                        .otherwise()
                            .process(companyAdminParamsProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "company-admin-params-migration", "companyAdminParamsCount"));

        from("direct:company-employee-params-migration")
                .routeId("company-employee-params-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = companyEmployeeParamsRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No companyEmployeeParams rows in this page, continuing...")
                        .otherwise()
                            .process(companyEmployeeParamsProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "company-employee-params-migration", "companyEmployeeParamsCount"));

        from("direct:employee-summary-migration")
                .routeId("employee-summary-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = employeeSummaryRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No employeeSummary rows in this page, continuing...")
                        .otherwise()
                            .process(employeeSummaryProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "employee-summary-migration", "employeeSummaryCount"));

        from("direct:edited-overtime-details-migration")
                .routeId("edited-overtime-details-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = editedOvertimeDetailsRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No editedOvertimeDetails rows in this page, continuing...")
                        .otherwise()
                            .process(editedOvertimeDetailsProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "edited-overtime-details-migration", "editedOvertimeDetailsCount"));

        from("direct:payroll-setting-lookup-migration")
                .routeId("payroll-setting-lookup-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = payrollSettingRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No payrollSetting rows in this page, continuing...")
                        .otherwise()
                            .process(payrollSettingLookupProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "payroll-setting-lookup-migration", "payrollSettingCount"));

        from("direct:payroll-overtime-lookup-migration")
                .routeId("payroll-overtime-lookup-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = payrollOvertimeRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No payrollOvertime rows in this page, continuing...")
                        .otherwise()
                            .process(payrollOvertimeLookupProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "payroll-overtime-lookup-migration", "payrollOvertimeCount"));

        from("direct:calculated-type-value-lookup-migration")
                .routeId("calculated-type-value-lookup-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = calculatedTypeValueRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No calculatedTypeValue rows in this page, continuing...")
                        .otherwise()
                            .process(calculatedTypeValueLookupProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "calculated-type-value-lookup-migration", "calculatedTypeValueCount"));

        from("direct:pay-by-online-transaction-migration")
                .routeId("pay-by-online-transaction-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = payByOnlineTransactionRepository.findMigratable(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No payByOnlineTransaction rows in this page, continuing...")
                        .otherwise()
                            .process(payByOnlineTransactionProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "pay-by-online-transaction-migration", "payByOnlineTransactionCount"));

    }



    private static void throttleBetweenSteps() throws InterruptedException {

        System.gc();

        Thread.sleep(MIGRATION_THROTTLE_MS);

    }



    private static void addImported(org.apache.camel.Exchange exchange) {

        Integer imported = exchange.getProperty("batchImported", 0, Integer.class);

        Integer count = exchange.getProperty("importCount", Integer.class);

        exchange.setProperty("importCount", count + imported);

    }



    private static void finishCount(org.apache.camel.Exchange exchange, String routeName, String countProperty) {

        Integer totalCount = exchange.getProperty("importCount", Integer.class);

        log.info("==========================================");

        log.info("{} completed!", routeName);

        log.info("Total records imported: {}", totalCount);

        log.info("==========================================");

        exchange.setProperty(countProperty, totalCount);

    }

}

