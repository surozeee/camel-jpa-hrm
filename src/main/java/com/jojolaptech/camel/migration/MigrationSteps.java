package com.jojolaptech.camel.migration;

import java.util.ArrayList;
import java.util.List;

/** Ordered master-chain route ids (must match ImportRouteBuilder .to("direct:…") order). */
public final class MigrationSteps {

    public static final List<String> ALL = List.of(
            "privilege-migration",
            "role-migration",
            "role-permission-migration",
            "user-migration",
            "company-migration",
            "company-address-migration",
            "branch-migration",
            "branch-address-migration",
            "fiscal-year-migration",
            "taxation-migration",
            "payroll-rule-migration",
            "salary-breakdown-migration",
            "pms-salary-breakdown-migration",
            "grade-migration",
            "grade-pay-step-migration",
            "grade-component-migration",
            "heading-template-seed-migration",
            "legacy-bank-migration",
            "cost-type-package-migration",
            "pay-plan-package-migration",
            "module-pricing-package-migration",
            "pay-type-package-migration",
            "payroll-institution-lookup-migration",
            "company-payroll-bank-migration",
            "company-payroll-institution-migration",
            "parent-payroll-heading-lookup-migration",
            "child-payroll-heading-lookup-migration",
            "company-branch-payroll-heading-migration",
            "payroll-label-lookup-migration",
            "payroll-heading-priority-lookup-migration",
            "payroll-heading-template-lookup-migration",
            "payroll-heading-date-lookup-migration",
            "payroll-heading-calculation-lookup-migration",
            "pay-period-specific-heading-lookup-migration",
            "branch-pay-period-lookup-migration",
            "leave-type-migration",
            "branch-leave-type-migration",
            "att-timetable-shift-migration",
            "att-shift-pattern-migration",
            "branch-holiday-migration",
            "leave-accumulation-rule-migration",
            "overtime-acc-leave-params-migration",
            "fy-closing-parameter-migration",
            "att-params-migration",
            "device-mac-migration",
            "company-setting-params-migration",
            "company-admin-params-migration",
            "company-employee-params-migration",
            "employee-summary-migration",
            "department-migration",
            "department-orphan-migration",
            "department-parent-link",
            "division-seed-migration",
            "cost-center-seed-migration",
            "team-seed-migration",
            "employee-migration",
            "employee-address-migration",
            "employee-master-address-migration",
            "employee-education-migration",
            "employee-family-migration",
            "employee-grade-link-migration",
            "employee-bank-detail-migration",
            "employee-device-enroll-migration",
            "emp-temp-shift-migration",
            "emp-permanent-shift-migration",
            "employee-experience-migration",
            "employee-award-migration",
            "employee-language-migration",
            "employee-seminar-migration",
            "employee-publication-migration",
            "employee-health-migration",
            "employee-training-migration",
            "employee-job-description-migration",
            "employment-suspension-migration",
            "employee-insurance-migration",
            "skill-master-migration",
            "employee-skill-migration",
            "employee-designation-migration",
            "employee-designation-link-migration",
            "employee-emergency-contact-migration",
            "employee-termination-migration",
            "branch-department-head-migration",
            "org-master-head-link-migration",
            "employee-org-fk-backfill-migration",
            "employee-job-level-history-migration",
            "job-status-history-migration",
            "job-position-migration",
            "company-employee-contract-migration",
            "employee-job-history-migration",
            "employee-document-migration",
            "employee-project-migration",
            "job-category-migration",
            "job-categories-migration",
            "employee-leave-accumulation-migration",
            "leave-balance-migration",
            "leave-adjustment-migration",
            "leave-application-migration",
            "leave-cancellation-migration",
            "calculated-auto-leave-credit-migration",
            "calculated-ot-leave-accrual-migration",
            "employee-salary-migration",
            "payroll-opening-balance-migration",
            "month-wise-salary-migration",
            "payroll-transaction-history-migration",
            "mass-salary-adjustment-migration",
            "employee-loan-migration",
            "employee-loan-payment-migration",
            "attendance-log-migration",
            "attendance-transaction-migration",
            "attendance-forgot-migration",
            "attendance-remark-migration",
            "device-logs-migration",
            "temp-device-logs-migration",
            "old-attendance-transaction-migration",
            "work-shift-migration",
            "edited-overtime-details-migration",
            "payroll-setting-lookup-migration",
            "payroll-overtime-lookup-migration",
            "calculated-type-value-lookup-migration",
            "pay-by-online-transaction-migration",
            "company-validity-subscription-migration",
            "subscription-payment-history-migration",
            "user-license-subscription-migration",
            "user-detail-migration",
            "user-portal-link-migration",
            "vacancy-migration",
            "vacancy-newspaper-migration",
            "interview-stage-migration",
            "screening-question-migration",
            "applicant-migration",
            "screening-answer-migration",
            "applicants-transaction-migration",
            "recruiters-migration",
            "evaluation-migration",
            "notice-migration",
            "message-notice-migration",
            "company-message-notice-migration",
            "happening-notice-migration",
            "event-notice-migration",
            "notification-notice-migration",
            "notification-viewed-migration",
            "marketing-person-detail-migration",
            "pricing-estimate-email-details-migration",
            "application-module-lookup-migration");

    private MigrationSteps() {}

    public static List<String> resolve(MigrationProperties props) {
        if ("single".equalsIgnoreCase(props.getMode())) {
            String only = trim(props.getOnlyStep());
            if (only.isEmpty()) {
                throw new IllegalArgumentException("migration.mode=single requires migration.only-step");
            }
            if (!ALL.contains(only)) {
                throw new IllegalArgumentException("Unknown migration.only-step: " + only);
            }
            return List.of(only);
        }
        if ("from".equalsIgnoreCase(props.getMode())) {
            String from = trim(props.getFromStep());
            if (from.isEmpty()) {
                throw new IllegalArgumentException("migration.mode=from requires migration.from-step");
            }
            int idx = ALL.indexOf(from);
            if (idx < 0) {
                throw new IllegalArgumentException("Unknown migration.from-step: " + from);
            }
            return new ArrayList<>(ALL.subList(idx, ALL.size()));
        }
        return List.copyOf(ALL);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
