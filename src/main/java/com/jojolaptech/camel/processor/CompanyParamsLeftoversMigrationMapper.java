package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyAdminParams;
import com.jojolaptech.camel.model.mysql.CompanyEmployeeParams;
import com.jojolaptech.camel.model.mysql.CompanySettingParams;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.master.MasterLookupEntity;
import com.jojolaptech.camel.model.postgres.master.PayrollRuleEntity;
import com.jojolaptech.camel.model.postgres.master.enums.MasterLookupCategoryEnum;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/** Maps company/employee EAV leftovers into master_lookup and known company/rule fields. */
public final class CompanyParamsLeftoversMigrationMapper {

    public static final long COMPANY_SETTING_PARAM_MYSQL_ID_OFFSET = 40_000_000_000_000L;
    public static final long COMPANY_ADMIN_PARAM_MYSQL_ID_OFFSET = 41_000_000_000_000L;
    public static final long COMPANY_EMPLOYEE_PARAM_MYSQL_ID_OFFSET = 42_000_000_000_000L;

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private CompanyParamsLeftoversMigrationMapper() {}

    public static MasterLookupEntity fromCompanySettingParam(CompanySettingParams source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        String name = blankToNull(source.getCompanyParamName());
        if (name == null) {
            return null;
        }
        StringBuilder desc = new StringBuilder();
        appendKv(desc, "value", source.getCompanyParamValue());
        if (source.getCompany() != null) {
            appendKv(desc, "companyMysqlId", source.getCompany().getId());
        }
        return MasterLookupEntity.builder()
                .mysqlId(COMPANY_SETTING_PARAM_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.COMPANY_SETTING_PARAM)
                .code(truncate("CSP-" + source.getId(), 64))
                .name(truncate(name, 255))
                .description(blankToNull(desc.toString()))
                .sortOrder(source.getId().intValue())
                .build();
    }

    public static MasterLookupEntity fromCompanyAdminParam(CompanyAdminParams source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        String name = blankToNull(source.getParamName());
        if (name == null) {
            return null;
        }
        StringBuilder desc = new StringBuilder();
        appendKv(desc, "value", source.getParamValue());
        appendKv(desc, "paramDate", toLocalDate(source.getParamDate()));
        if (source.getCompany() != null) {
            appendKv(desc, "companyMysqlId", source.getCompany().getId());
        }
        if (source.getAdmin() != null) {
            appendKv(desc, "adminCompanyMysqlId", source.getAdmin().getId());
        }
        return MasterLookupEntity.builder()
                .mysqlId(COMPANY_ADMIN_PARAM_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.COMPANY_ADMIN_PARAM)
                .code(truncate("CAP-" + source.getId(), 64))
                .name(truncate(name, 255))
                .description(blankToNull(desc.toString()))
                .sortOrder(source.getId().intValue())
                .build();
    }

    public static MasterLookupEntity fromCompanyEmployeeParam(CompanyEmployeeParams source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        String name = blankToNull(source.getParamName());
        if (name == null) {
            return null;
        }
        StringBuilder desc = new StringBuilder();
        appendKv(desc, "value", source.getParamValue());
        appendKv(desc, "paramDate", toLocalDate(source.getParamDate()));
        if (source.getCompany() != null) {
            appendKv(desc, "companyMysqlId", source.getCompany().getId());
        }
        if (source.getEmployee() != null) {
            appendKv(desc, "employeeMysqlId", source.getEmployee().getId());
        }
        return MasterLookupEntity.builder()
                .mysqlId(COMPANY_EMPLOYEE_PARAM_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.EMPLOYEE_SETTING_PARAM)
                .code(truncate("CEP-" + source.getId(), 64))
                .name(truncate(name, 255))
                .description(blankToNull(desc.toString()))
                .sortOrder(source.getId().intValue())
                .build();
    }

    public static String employeeSummaryMarker(Long summaryId) {
        return "[migrated-summary:" + summaryId + "]";
    }

    /**
     * Applies known company enable_* / leave / OT flags. Returns true if any field changed.
     */
    public static boolean applyCompanyEnableFlag(CompanyEntity company, String paramName, String paramValue) {
        if (company == null) {
            return false;
        }
        String key = FiscalMigrationMapper.normalizeParamName(paramName);
        Boolean bool = paramValue == null ? null : FiscalMigrationMapper.parseBoolean(paramValue);
        if (bool == null && !isDisableKey(key)) {
            return false;
        }
        return switch (key) {
            case "enable_division", "enabledivision", "division" -> setIfChanged(company::getEnableDivision, company::setEnableDivision, bool);
            case "enable_team", "enableteam", "team" -> setIfChanged(company::getEnableTeam, company::setEnableTeam, bool);
            case "enable_grade", "enablegrade", "grade" -> setIfChanged(company::getEnableGrade, company::setEnableGrade, bool);
            case "enable_cost_center", "enablecostcenter", "cost_center", "costcenter" ->
                    setIfChanged(company::getEnableCostCenter, company::setEnableCostCenter, bool);
            case "enable_work_location", "enableworklocation", "work_location", "worklocation" ->
                    setIfChanged(company::getEnableWorkLocation, company::setEnableWorkLocation, bool);
            case "enable_gratuity", "enablegratuity", "gratuity" ->
                    setIfChanged(company::getEnableGratuity, company::setEnableGratuity, bool);
            case "enable_onboarding", "enableonboarding", "onboarding" ->
                    setIfChanged(company::getEnableOnboarding, company::setEnableOnboarding, bool);
            case "enable_joining_checklist", "enablejoiningchecklist", "joining_checklist" ->
                    setIfChanged(company::getEnableJoiningChecklist, company::setEnableJoiningChecklist, bool);
            case "enable_time_overtime",
                    "enabletimeovertime",
                    "enable_ot",
                    "enable_ot_calculation",
                    "allowovertime",
                    "overtime_enabled",
                    "overtimeallowed" ->
                    setIfChanged(company::getEnableTimeOvertime, company::setEnableTimeOvertime, bool);
            case "enable_travel", "enabletravel", "travel" ->
                    setIfChanged(company::getEnableTravel, company::setEnableTravel, bool);
            case "enable_assets", "enableassets", "assets" ->
                    setIfChanged(company::getEnableAssets, company::setEnableAssets, bool);
            case "enable_documents", "enabledocuments", "documents" ->
                    setIfChanged(company::getEnableDocuments, company::setEnableDocuments, bool);
            case "enable_probation", "enableprobation", "probation" ->
                    setIfChanged(company::getEnableProbation, company::setEnableProbation, bool);
            case "enable_learning", "enablelearning", "learning" ->
                    setIfChanged(company::getEnableLearning, company::setEnableLearning, bool);
            case "enable_competency", "enablecompetency", "competency" ->
                    setIfChanged(company::getEnableCompetency, company::setEnableCompetency, bool);
            case "enable_career", "enablecareer", "career" ->
                    setIfChanged(company::getEnableCareer, company::setEnableCareer, bool);
            case "enable_promotion", "enablepromotion", "promotion" ->
                    setIfChanged(company::getEnablePromotion, company::setEnablePromotion, bool);
            case "enable_employee_relations", "enableemployeerelations", "employee_relations" ->
                    setIfChanged(company::getEnableEmployeeRelations, company::setEnableEmployeeRelations, bool);
            case "enable_disciplinary", "enabledisciplinary", "disciplinary" ->
                    setIfChanged(company::getEnableDisciplinary, company::setEnableDisciplinary, bool);
            case "enable_engagement", "enableengagement", "engagement" ->
                    setIfChanged(company::getEnableEngagement, company::setEnableEngagement, bool);
            case "enable_resignation", "enableresignation", "resignation" ->
                    setIfChanged(company::getEnableResignation, company::setEnableResignation, bool);
            case "enable_exit", "enableexit", "exit" ->
                    setIfChanged(company::getEnableExit, company::setEnableExit, bool);
            case "enable_notice_period", "enablenoticeperiod", "notice_period" ->
                    setIfChanged(company::getEnableNoticePeriod, company::setEnableNoticePeriod, bool);
            case "enable_roster_shift", "enablerostershift", "roster_shift", "roster_shift_enabled" ->
                    setIfChanged(company::getEnableRosterShift, company::setEnableRosterShift, bool);
            case "enable_leave_accumulation",
                    "enableleaveaccumulation",
                    "verifyleaveaccumulation",
                    "verify_leave_accumulation",
                    "leave_accumulation_verification" ->
                    setIfChanged(company::getEnableLeaveAccumulation, company::setEnableLeaveAccumulation, bool);
            case "disable_roster_shift", "disablerostershift" ->
                    setIfChanged(
                            company::getEnableRosterShift,
                            company::setEnableRosterShift,
                            FiscalMigrationMapper.invertDisableFlag(paramValue));
            case "disable_leave_accumulation", "disableleaveaccumulation" ->
                    setIfChanged(
                            company::getEnableLeaveAccumulation,
                            company::setEnableLeaveAccumulation,
                            FiscalMigrationMapper.invertDisableFlag(paramValue));
            default -> false;
        };
    }

    /** Applies known payroll params onto a payroll rule. Returns true if key was recognized. */
    public static boolean applyKnownPayrollParam(PayrollRuleEntity rule, String paramName, String paramValue) {
        if (rule == null || blankToNull(paramName) == null) {
            return false;
        }
        String before = snapshotPayrollRule(rule);
        FiscalMigrationMapper.applyPayrollParam(rule, paramName, paramValue);
        return !before.equals(snapshotPayrollRule(rule));
    }

    private static boolean isDisableKey(String key) {
        return key != null && key.startsWith("disable");
    }

    private static boolean setIfChanged(
            java.util.function.Supplier<Boolean> getter,
            java.util.function.Consumer<Boolean> setter,
            Boolean value) {
        if (value == null) {
            return false;
        }
        Boolean current = getter.get();
        if (value.equals(current)) {
            return false;
        }
        setter.accept(value);
        return true;
    }

    private static String snapshotPayrollRule(PayrollRuleEntity rule) {
        return String.valueOf(rule.getTaxFreePercentage())
                + '|'
                + rule.getTaxFreeTotal()
                + '|'
                + rule.getTaxFreeFlatCap()
                + '|'
                + rule.getTaxFreeSalaryBase()
                + '|'
                + rule.getCitMaxLimit()
                + '|'
                + rule.getSsfMaxLimit()
                + '|'
                + rule.isFestivalAllowanceTaxable()
                + '|'
                + rule.isBonusTaxable()
                + '|'
                + rule.isOvertimeTaxable()
                + '|'
                + rule.getInsuranceAmount()
                + '|'
                + rule.isInsuranceTaxFree();
    }

    static void appendKv(StringBuilder sb, String key, Object value) {
        if (value == null) {
            return;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return;
        }
        if (!sb.isEmpty()) {
            sb.append("; ");
        }
        sb.append(key).append('=').append(text);
    }

    static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZONE).toLocalDate();
    }

    static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
