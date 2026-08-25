package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.postgres.company.enums.FyClosingParameterTypeEnum;
import com.jojolaptech.camel.model.postgres.enums.FiscalYearTypeEnum;
import com.jojolaptech.camel.model.postgres.master.enums.TaxMaritalStatusEnum;
import com.jojolaptech.camel.model.postgres.master.enums.TaxRateTypeEnum;
import com.jojolaptech.camel.model.postgres.master.enums.SalaryBaseEnum;
import com.jojolaptech.camel.model.postgres.master.PayrollRuleEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

final class FiscalMigrationMapper {

    private FiscalMigrationMapper() {
    }

    static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    static String branchFiscalYearKey(Long fiscalYearMysqlId, Long branchMysqlId) {
        return fiscalYearMysqlId + ":" + branchMysqlId;
    }

    static String branchFiscalYearLabel(Long companyMysqlId, Long branchMysqlId, String fiscalYearName) {
        return companyMysqlId + "-" + branchMysqlId + "-" + fiscalYearName;
    }

    static TaxMaritalStatusEnum maritalStatus(String gender) {
        String normalized = gender == null ? "" : gender.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "married", "female", "f" -> TaxMaritalStatusEnum.MARRIED;
            default -> TaxMaritalStatusEnum.SINGLE;
        };
    }

    static FiscalYearTypeEnum branchFiscalYearType() {
        return FiscalYearTypeEnum.BIKRAM_SAMBAT;
    }

    static FyClosingParameterTypeEnum closingParameterType(String parameterType) {
        String normalized = parameterType == null ? "" : parameterType.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ENCASHMENT", "LEAVE_ENCASHMENT" -> FyClosingParameterTypeEnum.ENCASHMENT;
            case "PAYROLL_CUTOVER", "PAYROLL" -> FyClosingParameterTypeEnum.PAYROLL_CUTOVER;
            default -> FyClosingParameterTypeEnum.LEAVE_BALANCE;
        };
    }

    static boolean parseBoolean(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("true")
                || normalized.equals("yes")
                || normalized.equals("y")
                || normalized.equals("1")
                || normalized.equals("on");
    }

    static Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    static BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    static String normalizeParamName(String paramName) {
        if (paramName == null) {
            return "";
        }
        return paramName.trim()
                .toLowerCase(Locale.ROOT)
                .replace('/', '_')
                .replace(' ', '_')
                .replace('-', '_')
                .replaceAll("_+", "_");
    }

    static Boolean invertDisableFlag(String paramValue) {
        Boolean disabled = parseBoolean(paramValue);
        return disabled == null ? null : !disabled;
    }

    static Long parseLongId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    static void applyPayrollParam(PayrollRuleEntity rule, String paramName, String paramValue) {
        String key = normalizeParamName(paramName);
        BigDecimal decimal = parseDecimal(paramValue);
        Boolean bool = paramValue == null ? null : parseBoolean(paramValue);
        switch (key) {
            case "taxfreepercentage", "tax_free_percentage" -> {
                if (decimal != null) {
                    rule.setTaxFreePercentage(decimal);
                    rule.setTaxFreeTotal(decimal);
                }
            }
            case "taxfreeflatcap", "tax_free_flat_cap" -> {
                if (decimal != null) {
                    rule.setTaxFreeFlatCap(decimal);
                }
            }
            case "taxfreesalarybase", "tax_free_salary_base" -> {
                SalaryBaseEnum base = parseSalaryBase(paramValue);
                if (base != null) {
                    rule.setTaxFreeSalaryBase(base);
                }
            }
            case "citmaxlimit", "cit_max_limit" -> {
                if (decimal != null) {
                    rule.setCitMaxLimit(decimal);
                }
            }
            case "ssfmaxlimit", "ssf_max_limit" -> {
                if (decimal != null) {
                    rule.setSsfMaxLimit(decimal);
                }
            }
            case "festivalallowancetaxable", "festival_allowance_taxable" -> {
                if (bool != null) {
                    rule.setFestivalAllowanceTaxable(bool);
                }
            }
            case "bonustaxable", "bonus_taxable" -> {
                if (bool != null) {
                    rule.setBonusTaxable(bool);
                }
            }
            case "overtimetaxable", "overtime_taxable" -> {
                if (bool != null) {
                    rule.setOvertimeTaxable(bool);
                }
            }
            case "insuranceamount", "insurance_amount" -> {
                if (decimal != null) {
                    rule.setInsuranceAmount(decimal);
                }
            }
            case "insurancetaxfree", "insurance_tax_free" -> {
                if (bool != null) {
                    rule.setInsuranceTaxFree(bool);
                }
            }
            default -> {
                // Unknown legacy payroll param — ignored
            }
        }
    }

    private static SalaryBaseEnum parseSalaryBase(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return SalaryBaseEnum.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    static PayrollRuleEntity defaultPayrollRule(java.util.UUID masterFiscalYearId) {
        return PayrollRuleEntity.builder()
                .fiscalYearId(masterFiscalYearId)
                .taxFreeTotalType(TaxRateTypeEnum.PERCENTAGE)
                .taxFreeTotal(BigDecimal.ZERO)
                .build();
    }
}
