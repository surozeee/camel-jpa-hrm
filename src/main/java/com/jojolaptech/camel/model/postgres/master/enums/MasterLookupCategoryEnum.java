package com.jojolaptech.camel.model.postgres.master.enums;

public enum MasterLookupCategoryEnum {
    EDUCATION,
    PRIORITY,
    REQUISITION_TYPE,
    WORK_ARRANGEMENT,
    PUBLISH_SCOPE,
    JOB_LEVEL,
    DOCUMENT_TYPE,
    /** Platform marketing contacts from MySQL marketingPersonDetail. */
    MARKETING_PERSON,
    /** Pricing-estimate form leads from MySQL pricingEstimateEmailDetails. */
    PRICING_ESTIMATE_LEAD,
    /** Legacy applicationModule catalog names (reference only — ERP module tree is seeded separately). */
    LEGACY_APP_MODULE,
    /** Company-level EAV settings from MySQL companySettingParams. */
    COMPANY_SETTING_PARAM,
    /** Admin-scoped EAV from MySQL companyAdminParams. */
    COMPANY_ADMIN_PARAM,
    /** Employee-scoped EAV from MySQL companyEmployeeParams. */
    EMPLOYEE_SETTING_PARAM,
    /** Global payroll institution catalog from MySQL payrollInstitution. */
    PAYROLL_INSTITUTION,
    /** Company–institution link archive (identity only; no password). */
    COMPANY_PAYROLL_INSTITUTION,
    /** Parent payroll heading catalog from MySQL parentPayrollHeading. */
    PARENT_PAYROLL_HEADING,
    /** Child payroll heading catalog from MySQL childPayrollHeading. */
    CHILD_PAYROLL_HEADING,
    /** Payroll label taxonomy from MySQL payrollLabel. */
    PAYROLL_LABEL,
    /** Payroll heading priority defaults from MySQL payrollHeadingPriority. */
    PAYROLL_HEADING_PRIORITY,
    /** Payroll heading template catalog from MySQL payrollHeadingTemplate. */
    PAYROLL_HEADING_TEMPLATE,
    /** Legacy payrollHeadingDate archive. */
    LEGACY_PAYROLL_HEADING_DATE,
    /** Legacy payrollHeadingCalculation archive. */
    LEGACY_PAYROLL_HEADING_CALC,
    /** Legacy payPeriodSpecificHeading archive. */
    LEGACY_PAY_PERIOD_HEADING,
    /** Legacy branchPayPeriod archive. */
    LEGACY_BRANCH_PAY_PERIOD,
    /** Legacy payrollSetting archive. */
    LEGACY_PAYROLL_SETTING,
    /** Legacy payrollOvertime archive. */
    LEGACY_PAYROLL_OVERTIME,
    /** Legacy calculatedTypeValue archive. */
    LEGACY_CALCULATED_TYPE_VALUE
}
