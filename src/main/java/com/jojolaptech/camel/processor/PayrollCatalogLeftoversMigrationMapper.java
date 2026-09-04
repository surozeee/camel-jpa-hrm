package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.BranchPayPeriod;
import com.jojolaptech.camel.model.mysql.CalculatedTypeValue;
import com.jojolaptech.camel.model.mysql.ChildPayrollHeading;
import com.jojolaptech.camel.model.mysql.CompanyPayroll;
import com.jojolaptech.camel.model.mysql.CompanyPayrollInstitution;
import com.jojolaptech.camel.model.mysql.ParentPayrollHeading;
import com.jojolaptech.camel.model.mysql.PayByOnlineTransaction;
import com.jojolaptech.camel.model.mysql.PayPeriodSpecificHeading;
import com.jojolaptech.camel.model.mysql.PayrollHeadingCalculation;
import com.jojolaptech.camel.model.mysql.PayrollHeadingDate;
import com.jojolaptech.camel.model.mysql.PayrollHeadingPriority;
import com.jojolaptech.camel.model.mysql.PayrollHeadingTemplate;
import com.jojolaptech.camel.model.mysql.PayrollInstitution;
import com.jojolaptech.camel.model.mysql.PayrollLabel;
import com.jojolaptech.camel.model.mysql.PayrollOvertime;
import com.jojolaptech.camel.model.mysql.PayrollSetting;
import com.jojolaptech.camel.model.mysql.enums.PaymentResponseStatus;
import com.jojolaptech.camel.model.postgres.company.CompanyBankEntity;
import com.jojolaptech.camel.model.postgres.company.enums.AccountTypeEnum;
import com.jojolaptech.camel.model.postgres.master.MasterLookupEntity;
import com.jojolaptech.camel.model.postgres.master.enums.MasterLookupCategoryEnum;
import com.jojolaptech.camel.model.postgres.user.SubscriptionPaymentHistoryEntity;
import com.jojolaptech.camel.model.postgres.user.enums.SubscriptionBillingCycleEnum;
import com.jojolaptech.camel.model.postgres.user.enums.SubscriptionDiscountTypeEnum;
import com.jojolaptech.camel.model.postgres.user.enums.SubscriptionPaymentStatusEnum;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

/** Maps payroll catalog / leftover tables into master_lookup, company bank, and payment history. */
public final class PayrollCatalogLeftoversMigrationMapper {

    public static final long PAYROLL_INSTITUTION_MYSQL_ID_OFFSET = 44_000_000_000_000L;
    public static final long COMPANY_PAYROLL_BANK_MYSQL_ID_OFFSET = 45_000_000_000_000L;
    public static final long COMPANY_PAYROLL_INSTITUTION_MYSQL_ID_OFFSET = 46_000_000_000_000L;
    public static final long PARENT_PAYROLL_HEADING_MYSQL_ID_OFFSET = 47_000_000_000_000L;
    public static final long CHILD_PAYROLL_HEADING_MYSQL_ID_OFFSET = 55_000_000_000_000L;
    public static final long PAYROLL_LABEL_MYSQL_ID_OFFSET = 48_000_000_000_000L;
    public static final long PAYROLL_HEADING_PRIORITY_MYSQL_ID_OFFSET = 49_000_000_000_000L;
    public static final long PAYROLL_HEADING_TEMPLATE_MYSQL_ID_OFFSET = 50_000_000_000_000L;
    public static final long PAYROLL_HEADING_DATE_MYSQL_ID_OFFSET = 51_000_000_000_000L;
    public static final long PAYROLL_HEADING_CALC_MYSQL_ID_OFFSET = 52_000_000_000_000L;
    public static final long PAY_PERIOD_SPECIFIC_HEADING_MYSQL_ID_OFFSET = 53_000_000_000_000L;
    public static final long BRANCH_PAY_PERIOD_MYSQL_ID_OFFSET = 54_000_000_000_000L;
    public static final long PAYROLL_SETTING_MYSQL_ID_OFFSET = 56_000_000_000_000L;
    public static final long PAYROLL_OVERTIME_MYSQL_ID_OFFSET = 57_000_000_000_000L;
    public static final long CALCULATED_TYPE_VALUE_MYSQL_ID_OFFSET = 58_000_000_000_000L;
    public static final long PAY_BY_ONLINE_MYSQL_ID_OFFSET = 59_000_000_000_000L;

    private static final String CURRENCY = "NPR";
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private PayrollCatalogLeftoversMigrationMapper() {}

    public static MasterLookupEntity fromPayrollInstitution(PayrollInstitution source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        String name = blankToNull(source.getInstitutionName());
        if (name == null) {
            return null;
        }
        String code = blankToNull(source.getInstitutionCode());
        StringBuilder desc = new StringBuilder();
        appendKv(desc, "code", code);
        return MasterLookupEntity.builder()
                .mysqlId(PAYROLL_INSTITUTION_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.PAYROLL_INSTITUTION)
                .code(truncate(code != null ? code : ("PI-" + source.getId()), 64))
                .name(truncate(name, 255))
                .description(blankToNull(desc.toString()))
                .sortOrder(source.getId().intValue())
                .build();
    }

    public static CompanyBankEntity fromCompanyPayroll(
            CompanyPayroll source, UUID companyId, UUID bankId, boolean makePrimary) {
        if (source == null || source.getId() == null || companyId == null || bankId == null) {
            return null;
        }
        String accountNumber = blankToNull(source.getAccountNumber());
        if (accountNumber == null) {
            return null;
        }
        StringBuilder remarks = new StringBuilder();
        appendKv(remarks, "mobaletId", source.getMobaletId());
        appendKv(remarks, "branchCode", source.getBranchCode());
        appendKv(remarks, "bankCode", source.getBankCode());
        return CompanyBankEntity.builder()
                .mysqlId(COMPANY_PAYROLL_BANK_MYSQL_ID_OFFSET + source.getId())
                .companyId(companyId)
                .bankId(bankId)
                .accountNumber(truncate(accountNumber, 255))
                .bankBranch(blankToNull(source.getBranchCode()))
                .accountHolderName(blankToNull(source.getAccountName()))
                .accountType(AccountTypeEnum.SALARY)
                .isPrimary(makePrimary)
                .remarks(truncate(blankToNull(remarks.toString()), 500))
                .build();
    }

    /** Archives company–institution link without password. */
    public static MasterLookupEntity fromCompanyPayrollInstitution(CompanyPayrollInstitution source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        String identity = blankToNull(source.getCompanyIdentity());
        if (identity == null) {
            return null;
        }
        StringBuilder desc = new StringBuilder();
        appendKv(desc, "companyIdentity", identity);
        if (source.getCompany() != null) {
            appendKv(desc, "companyMysqlId", source.getCompany().getId());
        }
        String instName = null;
        String instCode = null;
        if (source.getPayrollInstitution() != null) {
            instName = blankToNull(source.getPayrollInstitution().getInstitutionName());
            instCode = blankToNull(source.getPayrollInstitution().getInstitutionCode());
            appendKv(desc, "institutionName", instName);
            appendKv(desc, "institutionCode", instCode);
            appendKv(desc, "institutionMysqlId", source.getPayrollInstitution().getId());
        }
        String name = instName != null ? instName : ("Institution link " + source.getId());
        return MasterLookupEntity.builder()
                .mysqlId(COMPANY_PAYROLL_INSTITUTION_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.COMPANY_PAYROLL_INSTITUTION)
                .code(truncate("CPI-" + source.getId(), 64))
                .name(truncate(name, 255))
                .description(blankToNull(desc.toString()))
                .sortOrder(source.getId().intValue())
                .build();
    }

    public static MasterLookupEntity fromParentPayrollHeading(ParentPayrollHeading source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        String name = blankToNull(source.getHeadingName());
        if (name == null) {
            return null;
        }
        StringBuilder desc = new StringBuilder();
        appendKv(desc, "status", source.getStatus());
        if (source.getCompany() != null) {
            appendKv(desc, "companyMysqlId", source.getCompany().getId());
        }
        return MasterLookupEntity.builder()
                .mysqlId(PARENT_PAYROLL_HEADING_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.PARENT_PAYROLL_HEADING)
                .code(truncate("PPH-" + source.getId(), 64))
                .name(truncate(name, 255))
                .description(blankToNull(desc.toString()))
                .sortOrder(source.getId().intValue())
                .build();
    }

    public static MasterLookupEntity fromChildPayrollHeading(ChildPayrollHeading source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        String name = null;
        StringBuilder desc = new StringBuilder();
        if (source.getPayrollSystemHeading() != null) {
            name = blankToNull(source.getPayrollSystemHeading().getHeadingName());
            appendKv(desc, "systemHeadingMysqlId", source.getPayrollSystemHeading().getId());
        }
        if (source.getParentPayrollHeading() != null) {
            appendKv(desc, "parentMysqlId", source.getParentPayrollHeading().getId());
            appendKv(desc, "parentName", source.getParentPayrollHeading().getHeadingName());
            if (name == null) {
                name = blankToNull(source.getParentPayrollHeading().getHeadingName());
            }
        }
        if (name == null) {
            name = "Child heading " + source.getId();
        }
        return MasterLookupEntity.builder()
                .mysqlId(CHILD_PAYROLL_HEADING_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.CHILD_PAYROLL_HEADING)
                .code(truncate("CPH-" + source.getId(), 64))
                .name(truncate(name, 255))
                .description(blankToNull(desc.toString()))
                .sortOrder(source.getId().intValue())
                .build();
    }

    public static MasterLookupEntity fromPayrollLabel(PayrollLabel source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        String name = blankToNull(source.getLabelName());
        if (name == null) {
            return null;
        }
        StringBuilder desc = new StringBuilder();
        appendKv(desc, "individualName", source.getIndividualName());
        appendKv(desc, "sortingNumber", source.getSortingNumber());
        appendKv(desc, "beforeTax", source.getBeforeTax());
        appendKv(desc, "status", source.getStatus());
        return MasterLookupEntity.builder()
                .mysqlId(PAYROLL_LABEL_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.PAYROLL_LABEL)
                .code(truncate("PL-" + source.getId(), 64))
                .name(truncate(name, 255))
                .description(blankToNull(desc.toString()))
                .sortOrder(source.getSortingNumber() != null ? source.getSortingNumber() : source.getId().intValue())
                .build();
    }

    public static MasterLookupEntity fromPayrollHeadingPriority(PayrollHeadingPriority source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        String name = blankToNull(source.getTitle());
        if (name == null) {
            return null;
        }
        StringBuilder desc = new StringBuilder();
        appendKv(desc, "priority", source.getPriority());
        appendKv(desc, "isPercent", source.getIsPercent());
        appendKv(desc, "regularPaid", source.getRegularPaid());
        return MasterLookupEntity.builder()
                .mysqlId(PAYROLL_HEADING_PRIORITY_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.PAYROLL_HEADING_PRIORITY)
                .code(truncate("PHP-" + source.getId(), 64))
                .name(truncate(name, 255))
                .description(blankToNull(desc.toString()))
                .sortOrder(source.getPriority() != null ? source.getPriority() : source.getId().intValue())
                .build();
    }

    public static MasterLookupEntity fromPayrollHeadingTemplate(PayrollHeadingTemplate source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        String name = blankToNull(source.getTemplateName());
        if (name == null) {
            return null;
        }
        StringBuilder desc = new StringBuilder();
        appendKv(desc, "status", source.getStatus());
        return MasterLookupEntity.builder()
                .mysqlId(PAYROLL_HEADING_TEMPLATE_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.PAYROLL_HEADING_TEMPLATE)
                .code(truncate("PHT-" + source.getId(), 64))
                .name(truncate(name, 255))
                .description(blankToNull(desc.toString()))
                .sortOrder(source.getId().intValue())
                .build();
    }

    public static MasterLookupEntity fromPayrollHeadingDate(PayrollHeadingDate source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        StringBuilder desc = new StringBuilder();
        appendKv(desc, "startDate", toLocalDate(source.getStartDate()));
        appendKv(desc, "endDate", toLocalDate(source.getEndDate()));
        if (source.getCompanyPayrollHeading() != null) {
            appendKv(desc, "companyPayrollHeadingMysqlId", source.getCompanyPayrollHeading().getId());
        }
        if (source.getCompanyBranchPayrollHeading() != null) {
            appendKv(desc, "companyBranchPayrollHeadingMysqlId", source.getCompanyBranchPayrollHeading().getId());
        }
        return MasterLookupEntity.builder()
                .mysqlId(PAYROLL_HEADING_DATE_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.LEGACY_PAYROLL_HEADING_DATE)
                .code(truncate("PHD-" + source.getId(), 64))
                .name(truncate("Heading date " + source.getId(), 255))
                .description(blankToNull(desc.toString()))
                .sortOrder(source.getId().intValue())
                .build();
    }

    public static MasterLookupEntity fromPayrollHeadingCalculation(PayrollHeadingCalculation source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        StringBuilder desc = new StringBuilder();
        appendKv(desc, "calculationType", source.getCalculationType());
        appendKv(desc, "calculationValue", source.getCalculationValue());
        appendKv(desc, "alternativeValue", source.getAlternativeValue());
        appendKv(desc, "comparisionMethod", source.getComparisionMethod());
        appendKv(desc, "startDate", toLocalDate(source.getStartDate()));
        appendKv(desc, "endDate", toLocalDate(source.getEndDate()));
        if (source.getCompanyPayrollHeading() != null) {
            appendKv(desc, "companyPayrollHeadingMysqlId", source.getCompanyPayrollHeading().getId());
        }
        if (source.getCompanyBranchPayrollHeading() != null) {
            appendKv(desc, "companyBranchPayrollHeadingMysqlId", source.getCompanyBranchPayrollHeading().getId());
        }
        String name = blankToNull(source.getCalculationType());
        if (name == null) {
            name = "Heading calc " + source.getId();
        }
        return MasterLookupEntity.builder()
                .mysqlId(PAYROLL_HEADING_CALC_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.LEGACY_PAYROLL_HEADING_CALC)
                .code(truncate("PHC-" + source.getId(), 64))
                .name(truncate(name, 255))
                .description(blankToNull(desc.toString()))
                .sortOrder(source.getId().intValue())
                .build();
    }

    public static MasterLookupEntity fromPayPeriodSpecificHeading(PayPeriodSpecificHeading source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        StringBuilder desc = new StringBuilder();
        if (source.getPayPeriod() != null) {
            appendKv(desc, "payPeriodMysqlId", source.getPayPeriod().getId());
        }
        if (source.getCompanyPayrollHeading() != null) {
            appendKv(desc, "companyPayrollHeadingMysqlId", source.getCompanyPayrollHeading().getId());
        }
        if (source.getFiscalYear() != null) {
            appendKv(desc, "fiscalYearMysqlId", source.getFiscalYear().getId());
        }
        return MasterLookupEntity.builder()
                .mysqlId(PAY_PERIOD_SPECIFIC_HEADING_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.LEGACY_PAY_PERIOD_HEADING)
                .code(truncate("PPSH-" + source.getId(), 64))
                .name(truncate("Pay-period heading " + source.getId(), 255))
                .description(blankToNull(desc.toString()))
                .sortOrder(source.getId().intValue())
                .build();
    }

    public static MasterLookupEntity fromBranchPayPeriod(BranchPayPeriod source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        StringBuilder desc = new StringBuilder();
        if (source.getPayPeriod() != null) {
            appendKv(desc, "payPeriodMysqlId", source.getPayPeriod().getId());
        }
        if (source.getBranch() != null) {
            appendKv(desc, "branchMysqlId", source.getBranch().getId());
        }
        return MasterLookupEntity.builder()
                .mysqlId(BRANCH_PAY_PERIOD_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.LEGACY_BRANCH_PAY_PERIOD)
                .code(truncate("BPP-" + source.getId(), 64))
                .name(truncate("Branch pay period " + source.getId(), 255))
                .description(blankToNull(desc.toString()))
                .sortOrder(source.getId().intValue())
                .build();
    }

    public static MasterLookupEntity fromPayrollSetting(PayrollSetting source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        String name = blankToNull(source.getTitle());
        if (name == null) {
            name = "Payroll setting " + source.getId();
        }
        StringBuilder desc = new StringBuilder();
        appendKv(desc, "priority", source.getPriority());
        appendKv(desc, "amount", source.getAmount());
        if (source.getEmployee() != null) {
            appendKv(desc, "employeeMysqlId", source.getEmployee().getId());
        }
        if (source.getPayrollheading() != null) {
            appendKv(desc, "payrollHeadingMysqlId", source.getPayrollheading().getId());
        }
        if (source.getPayrollMonth() != null) {
            appendKv(desc, "payrollMonthMysqlId", source.getPayrollMonth().getId());
        }
        return MasterLookupEntity.builder()
                .mysqlId(PAYROLL_SETTING_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.LEGACY_PAYROLL_SETTING)
                .code(truncate("PS-" + source.getId(), 64))
                .name(truncate(name, 255))
                .description(blankToNull(desc.toString()))
                .sortOrder(source.getPriority() != null ? source.getPriority() : source.getId().intValue())
                .build();
    }

    public static MasterLookupEntity fromPayrollOvertime(PayrollOvertime source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        StringBuilder desc = new StringBuilder();
        appendKv(desc, "overTimeType", source.getOverTimeType());
        appendKv(desc, "overTime", source.getOverTime());
        appendKv(desc, "overTimeValue", source.getOverTimeValue());
        appendKv(desc, "status", source.getStatus());
        if (source.getEmployee() != null) {
            appendKv(desc, "employeeMysqlId", source.getEmployee().getId());
        }
        if (source.getPayPeriod() != null) {
            appendKv(desc, "payPeriodMysqlId", source.getPayPeriod().getId());
        }
        String name = source.getOverTimeType() != null
                ? source.getOverTimeType().name()
                : ("Payroll OT " + source.getId());
        return MasterLookupEntity.builder()
                .mysqlId(PAYROLL_OVERTIME_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.LEGACY_PAYROLL_OVERTIME)
                .code(truncate("PO-" + source.getId(), 64))
                .name(truncate(name, 255))
                .description(blankToNull(desc.toString()))
                .sortOrder(source.getId().intValue())
                .build();
    }

    public static MasterLookupEntity fromCalculatedTypeValue(CalculatedTypeValue source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        StringBuilder desc = new StringBuilder();
        appendKv(desc, "calculatedHeadingType", source.getCalculatedHeadingType());
        appendKv(desc, "calculatedValue", source.getCalculatedValue());
        appendKv(desc, "finalValue", source.getFinalValue());
        if (source.getEmployee() != null) {
            appendKv(desc, "employeeMysqlId", source.getEmployee().getId());
        }
        if (source.getPayPeriod() != null) {
            appendKv(desc, "payPeriodMysqlId", source.getPayPeriod().getId());
        }
        String name = source.getCalculatedHeadingType() != null
                ? source.getCalculatedHeadingType().name()
                : ("Calculated type " + source.getId());
        return MasterLookupEntity.builder()
                .mysqlId(CALCULATED_TYPE_VALUE_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.LEGACY_CALCULATED_TYPE_VALUE)
                .code(truncate("CTV-" + source.getId(), 64))
                .name(truncate(name, 255))
                .description(blankToNull(desc.toString()))
                .sortOrder(source.getId().intValue())
                .build();
    }

    public static SubscriptionPaymentHistoryEntity paymentFromOnlineTransaction(
            PayByOnlineTransaction source, UUID companyId) {
        if (source == null || source.getId() == null || companyId == null) {
            return null;
        }
        SubscriptionPaymentStatusEnum status = mapOnlineStatus(source.getPaymentResponseStatus());
        StringBuilder remarks = new StringBuilder("Migrated from payByOnlineTransaction");
        appendKv(remarks, "transactionId", source.getTransactionId());
        appendKv(remarks, "status", source.getPaymentResponseStatus());
        if (source.getCompanyValidityId() != null) {
            appendKv(remarks, "companyValidityMysqlId", source.getCompanyValidityId().getId());
        }
        return SubscriptionPaymentHistoryEntity.builder()
                .mysqlId(PAY_BY_ONLINE_MYSQL_ID_OFFSET + source.getId())
                .companyId(companyId)
                .packageCode(truncate("LEGACY-PBO-" + source.getId(), 64))
                .packageName("Online payment")
                .billingCycle(SubscriptionBillingCycleEnum.YEARLY)
                .originalAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .amount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .discountType(SubscriptionDiscountTypeEnum.NONE)
                .discountValue(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .currencyCode(CURRENCY)
                .paymentMethod("ONLINE")
                .paymentReference(blankToNull(source.getTransactionId()))
                .remarks(blankToNull(remarks.toString()))
                .paymentStatus(status)
                .build();
    }

    public static SubscriptionPaymentStatusEnum mapOnlineStatus(PaymentResponseStatus status) {
        if (status == null) {
            return SubscriptionPaymentStatusEnum.PENDING;
        }
        return switch (status) {
            case Success -> SubscriptionPaymentStatusEnum.VERIFIED;
            case Failed -> SubscriptionPaymentStatusEnum.REJECTED;
            case Processing -> SubscriptionPaymentStatusEnum.PENDING;
        };
    }

    /** Parses previousOverTime string to whole minutes when numeric (hours or minutes). */
    public static Integer parseOvertimeToMinutes(String previousOverTime) {
        String text = blankToNull(previousOverTime);
        if (text == null) {
            return null;
        }
        try {
            if (text.contains(":")) {
                String[] parts = text.split(":");
                int hours = Integer.parseInt(parts[0].trim());
                int mins = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
                return hours * 60 + mins;
            }
            BigDecimal value = new BigDecimal(text.replace(",", "").trim());
            // Values under 24 treated as hours; otherwise minutes.
            if (value.abs().compareTo(BigDecimal.valueOf(24)) <= 0 && text.contains(".")) {
                return value.multiply(BigDecimal.valueOf(60)).setScale(0, RoundingMode.HALF_UP).intValue();
            }
            return value.setScale(0, RoundingMode.HALF_UP).intValue();
        } catch (NumberFormatException ex) {
            return null;
        }
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
