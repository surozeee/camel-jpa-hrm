package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyPayrollHeading;
import com.jojolaptech.camel.model.mysql.PayrollHeading;
import com.jojolaptech.camel.model.mysql.PayrollParentHeading;
import com.jojolaptech.camel.model.mysql.PayrollSystemHeading;
import com.jojolaptech.camel.model.mysql.enums.HeadingType;
import com.jojolaptech.camel.model.mysql.enums.PayrollHeadingType;
import com.jojolaptech.camel.model.mysql.enums.PayrollValueType;
import com.jojolaptech.camel.model.postgres.company.BranchSalaryBreakdownEntity;
import com.jojolaptech.camel.model.postgres.company.enums.PayrollOpeningBalanceTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.RateTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.SalaryBreakdownLineTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.SalaryBreakdownPercentBaseEnum;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

final class PayrollHeadingMigrationMapper {

    /** Distinguishes PMS payrollHeading ids from companyPayrollHeading ids in mysql_id. */
    static final long PMS_HEADING_MYSQL_ID_OFFSET = 8_000_000_000_000L;

    private PayrollHeadingMigrationMapper() {
    }

    static boolean isMigratableCompanyHeading(CompanyPayrollHeading source) {
        if (source == null || Boolean.FALSE.equals(source.getStatus())) {
            return false;
        }
        PayrollSystemHeading system = source.getPayrollHeading();
        if (system == null) {
            // Parent-only merge row without leaf system heading — skip amount line
            return source.getPayrollParentHeading() != null;
        }
        return system.getHeadingType() != PayrollHeadingType.PARENT;
    }

    static BranchSalaryBreakdownEntity fromCompanyHeading(CompanyPayrollHeading source, UUID companyId) {
        PayrollSystemHeading system = source.getPayrollHeading();
        PayrollParentHeading parent = source.getPayrollParentHeading();

        String lineName = resolveLineName(system, parent);
        LineMeta meta = system != null ? fromSystemHeading(system) : LineMeta.parentFallback();

        return BranchSalaryBreakdownEntity.builder()
                .mysqlId(source.getId())
                .companyId(companyId)
                .branchId(null)
                .lineName(lineName)
                .lineType(meta.lineType)
                .rateType(meta.rateType)
                .rateValue(meta.rateValue)
                .percentBase(meta.percentBase)
                .isBasicSalaryLine(meta.isBasic)
                .displayOrder(meta.displayOrder)
                .description(null)
                .remarks("migrated from companyPayrollHeading#" + source.getId())
                .isTaxable(meta.isTaxable)
                .appliesDuringProbation(true)
                .appliesAfterProbation(true)
                .build();
    }

    static BranchSalaryBreakdownEntity fromPmsHeading(PayrollHeading source, UUID companyId) {
        LineMeta meta = fromPmsHeadingType(source);
        return BranchSalaryBreakdownEntity.builder()
                .mysqlId(PMS_HEADING_MYSQL_ID_OFFSET + source.getId())
                .companyId(companyId)
                .branchId(null)
                .lineName(trimToNull(source.getTitle()) != null ? source.getTitle().trim() : "Heading-" + source.getId())
                .lineType(meta.lineType)
                .rateType(Boolean.TRUE.equals(source.getIsPercent()) ? RateTypeEnum.PERCENT : RateTypeEnum.FLAT)
                .rateValue(null)
                .percentBase(meta.percentBase)
                .isBasicSalaryLine(meta.isBasic)
                .displayOrder(source.getPriority())
                .remarks("migrated from payrollHeading#" + source.getId())
                .isTaxable(meta.isTaxable)
                .appliesDuringProbation(Boolean.TRUE.equals(source.getRegularPaid()))
                .appliesAfterProbation(true)
                .build();
    }

    static long pmsMysqlId(long payrollHeadingId) {
        return PMS_HEADING_MYSQL_ID_OFFSET + payrollHeadingId;
    }

    static PayrollOpeningBalanceTypeEnum inferOpeningBalanceType(String title, HeadingType headingType) {
        String normalized = title == null ? "" : title.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("pf")
                || normalized.contains("provident")
                || normalized.contains("cit")
                || normalized.contains("ssf")
                || normalized.contains("social security")) {
            return PayrollOpeningBalanceTypeEnum.TAX_FREE_REDEMPTION;
        }
        if (normalized.contains("tax") && (normalized.contains("withheld") || normalized.contains("paid"))) {
            return PayrollOpeningBalanceTypeEnum.TAX_WITHHELD;
        }
        if (headingType == HeadingType.basic || normalized.contains("basic")) {
            return PayrollOpeningBalanceTypeEnum.BASIC;
        }
        if (normalized.contains("gross")) {
            return PayrollOpeningBalanceTypeEnum.GROSS;
        }
        if (normalized.contains("taxable")) {
            return PayrollOpeningBalanceTypeEnum.TAXABLE_EARNINGS;
        }
        return PayrollOpeningBalanceTypeEnum.OTHER;
    }

    static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String resolveLineName(PayrollSystemHeading system, PayrollParentHeading parent) {
        if (parent != null && trimToNull(parent.getHeadingName()) != null) {
            return parent.getHeadingName().trim();
        }
        if (system != null && trimToNull(system.getHeadingName()) != null) {
            return system.getHeadingName().trim();
        }
        return "Payroll line";
    }

    private static LineMeta fromSystemHeading(PayrollSystemHeading system) {
        PayrollHeadingType type = system.getHeadingType();
        boolean isBasic = type == PayrollHeadingType.BASIC_AMOUNT;
        SalaryBreakdownLineTypeEnum lineType = switch (type == null ? PayrollHeadingType.ADDITION : type) {
            case DEDUCTION, DEDUCTION_AT, EXEMPTED_DEDUCTION -> SalaryBreakdownLineTypeEnum.DEDUCTION;
            case PARENT, BASIC_AMOUNT, ADDITION, EXEMPTED, ADDITION_AT -> SalaryBreakdownLineTypeEnum.EARNING;
        };
        boolean isTaxable = type != PayrollHeadingType.EXEMPTED && type != PayrollHeadingType.EXEMPTED_DEDUCTION;
        if (Boolean.FALSE.equals(system.getBeforeTax()) && lineType == SalaryBreakdownLineTypeEnum.EARNING) {
            // beforeTax=false often means taxed / after-tax presentation; keep taxable true for earnings
            isTaxable = true;
        }
        if (type == PayrollHeadingType.EXEMPTED || type == PayrollHeadingType.EXEMPTED_DEDUCTION) {
            isTaxable = false;
        }

        RateTypeEnum rateType = system.getValueType() == PayrollValueType.PERCENTAGE
                ? RateTypeEnum.PERCENT
                : RateTypeEnum.FLAT;
        SalaryBreakdownPercentBaseEnum percentBase = null;
        if (rateType == RateTypeEnum.PERCENT) {
            percentBase = SalaryBreakdownPercentBaseEnum.BASIC;
            if (system.getCalculatedOn() != null
                    && system.getCalculatedOn().getHeadingType() != PayrollHeadingType.BASIC_AMOUNT) {
                percentBase = SalaryBreakdownPercentBaseEnum.GROSS;
            }
        }

        BigDecimal rateValue = system.getFlatMaxAmount();
        if (rateValue == null) {
            rateValue = system.getMaxValue();
        }

        return new LineMeta(
                lineType,
                rateType,
                rateValue,
                percentBase,
                isBasic,
                system.getSortingNo() != null ? system.getSortingNo() : system.getGroupSortingNo(),
                isTaxable);
    }

    private static LineMeta fromPmsHeadingType(PayrollHeading source) {
        HeadingType type = source.getHeadingType();
        boolean isBasic = type == HeadingType.basic;
        SalaryBreakdownLineTypeEnum lineType;
        SalaryBreakdownPercentBaseEnum percentBase = null;
        boolean isTaxable = true;

        if (type == null) {
            lineType = SalaryBreakdownLineTypeEnum.EARNING;
        } else {
            lineType = switch (type) {
                case deducOnBasic, deducOnBase, deducOnGross, nonExemDeduc, deducOnEAT ->
                        SalaryBreakdownLineTypeEnum.DEDUCTION;
                case rebate -> SalaryBreakdownLineTypeEnum.DEDUCTION;
                default -> SalaryBreakdownLineTypeEnum.EARNING;
            };
            percentBase = switch (type) {
                case addOnBasic, deducOnBasic, deducOnBase, addOnBase -> SalaryBreakdownPercentBaseEnum.BASIC;
                case addOnGross, deducOnGross -> SalaryBreakdownPercentBaseEnum.GROSS;
                default -> null;
            };
            if (type == HeadingType.noDeduction || type == HeadingType.rebate) {
                isTaxable = false;
            }
        }
        return new LineMeta(
                lineType,
                Boolean.TRUE.equals(source.getIsPercent()) ? RateTypeEnum.PERCENT : RateTypeEnum.FLAT,
                null,
                percentBase,
                isBasic,
                source.getPriority(),
                isTaxable);
    }

    private record LineMeta(
            SalaryBreakdownLineTypeEnum lineType,
            RateTypeEnum rateType,
            BigDecimal rateValue,
            SalaryBreakdownPercentBaseEnum percentBase,
            boolean isBasic,
            Integer displayOrder,
            boolean isTaxable) {

        static LineMeta parentFallback() {
            return new LineMeta(
                    SalaryBreakdownLineTypeEnum.EARNING,
                    RateTypeEnum.FLAT,
                    null,
                    null,
                    false,
                    null,
                    true);
        }
    }
}
