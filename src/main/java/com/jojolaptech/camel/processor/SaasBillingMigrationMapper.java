package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyValidity;
import com.jojolaptech.camel.model.mysql.CostType;
import com.jojolaptech.camel.model.mysql.ModulePricing;
import com.jojolaptech.camel.model.mysql.ModulePricingCriteria;
import com.jojolaptech.camel.model.mysql.PayPlan;
import com.jojolaptech.camel.model.mysql.PayType;
import com.jojolaptech.camel.model.mysql.SubscriptionPayment;
import com.jojolaptech.camel.model.mysql.enums.PaymentStats;
import com.jojolaptech.camel.model.mysql.enums.PaymentStatus;
import com.jojolaptech.camel.model.postgres.master.ModulePricingPackageEntity;
import com.jojolaptech.camel.model.postgres.master.enums.BillingCycleEnum;
import com.jojolaptech.camel.model.postgres.user.CompanySubscriptionEntity;
import com.jojolaptech.camel.model.postgres.user.SubscriptionPaymentHistoryEntity;
import com.jojolaptech.camel.model.postgres.user.enums.SubscriptionBillingCycleEnum;
import com.jojolaptech.camel.model.postgres.user.enums.SubscriptionDiscountTypeEnum;
import com.jojolaptech.camel.model.postgres.user.enums.SubscriptionPaymentStatusEnum;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/** Maps MySQL SaaS billing (CostType/PayPlan/validity/payments) → ERP subscription tables. */
public final class SaasBillingMigrationMapper {

    /** PayPlan packages share module_pricing_package with CostType; offset avoids id collide. */
    public static final long PAY_PLAN_MYSQL_ID_OFFSET = 23_000_000_000_000L;

    /** companyValidity payment history rows share subscription_payment_history with SubscriptionPayment. */
    public static final long COMPANY_VALIDITY_PAYMENT_MYSQL_ID_OFFSET = 24_000_000_000_000L;

    /**
     * ModulePricing packages (user-tier amounts). Module tree / package scopes are ERP-seeded — not
     * written here.
     */
    public static final long MODULE_PRICING_MYSQL_ID_OFFSET = 26_000_000_000_000L;

    private static final String CURRENCY = "NPR";
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private SaasBillingMigrationMapper() {}

    public static ModulePricingPackageEntity fromCostType(CostType source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        String subscription = blankToNull(source.getSubscription());
        String name = subscription != null ? subscription : "Legacy CostType " + source.getId();
        BillingCycleEnum cycle = mapBillingCycle(subscription);
        Double discount = source.getDiscount();
        String features = discount != null
                ? "Legacy CostType discount: " + formatDecimal(discount) + "%"
                : null;
        return ModulePricingPackageEntity.builder()
                .mysqlId(source.getId())
                .packageCode(truncate("LEGACY-CT-" + source.getId(), 64))
                .packageName(truncate(name, 255))
                .price(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .currencyCode(CURRENCY)
                .billingCycle(cycle)
                .maxUsers(null)
                .features(features)
                .description("Migrated from MySQL costType (subscription pricing, not org cost center)")
                .build();
    }

    public static ModulePricingPackageEntity fromPayPlan(PayPlan source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        String cycleLabel = source.getCostType() != null ? blankToNull(source.getCostType().getSubscription()) : null;
        BillingCycleEnum cycle = mapBillingCycle(cycleLabel);
        if (cycle == BillingCycleEnum.YEARLY && source.getNoOfDays() != null) {
            cycle = mapBillingCycleFromDays(source.getNoOfDays());
        }
        BigDecimal price = money(source.getNetAmount() != null ? source.getNetAmount() : source.getAmount());
        Integer maxUsers = source.getEndNo();
        String remarks = blankToNull(source.getRemarks());
        String name = remarks != null
                ? remarks
                : "PayPlan " + source.getId()
                        + (source.getStartNo() != null && source.getEndNo() != null
                                ? " (" + source.getStartNo() + "-" + source.getEndNo() + " users)"
                                : "");
        String features = buildPayPlanFeatures(source);
        return ModulePricingPackageEntity.builder()
                .mysqlId(PAY_PLAN_MYSQL_ID_OFFSET + source.getId())
                .packageCode(truncate("LEGACY-PP-" + source.getId(), 64))
                .packageName(truncate(name, 255))
                .price(price)
                .currencyCode(CURRENCY)
                .billingCycle(cycle)
                .maxUsers(maxUsers)
                .features(features)
                .description("Migrated from MySQL payPlan (priced SaaS package)")
                .build();
    }

    /**
     * ModulePricing → catalog package only. Legacy app-module name is recorded in features text;
     * {@code module_pricing_scope} / ERP {@code module} tree stay ERP-seeded.
     */
    public static ModulePricingPackageEntity fromModulePricing(ModulePricing source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        ModulePricingCriteria criteria = source.getModulePricingCriteria();
        BillingCycleEnum cycle = resolveCycleFromCriteria(criteria);
        BigDecimal price = money(source.getAmount());
        String remarks = blankToNull(source.getRemarks());
        String moduleName = criteria != null && criteria.getAppModule() != null
                ? blankToNull(criteria.getAppModule().getModuleName())
                : null;
        String name = remarks != null
                ? remarks
                : (moduleName != null ? moduleName + " pricing" : "ModulePricing " + source.getId());
        if (source.getStartNo() != null && source.getEndNo() != null) {
            name = name + " (" + source.getStartNo() + "-" + source.getEndNo() + " users)";
        }
        StringBuilder features = new StringBuilder();
        if (moduleName != null) {
            features.append("legacyAppModule=").append(moduleName);
            features.append(" (scope from ERP module seed — not migrated)");
        }
        if (source.getStartNo() != null || source.getEndNo() != null) {
            if (!features.isEmpty()) {
                features.append("; ");
            }
            features.append("users ")
                    .append(source.getStartNo() != null ? source.getStartNo() : "?")
                    .append("-")
                    .append(source.getEndNo() != null ? source.getEndNo() : "?");
        }
        if (Boolean.FALSE.equals(source.getIsActive())) {
            if (!features.isEmpty()) {
                features.append("; ");
            }
            features.append("inactive in legacy");
        }
        return ModulePricingPackageEntity.builder()
                .mysqlId(MODULE_PRICING_MYSQL_ID_OFFSET + source.getId())
                .packageCode(truncate("LEGACY-MP-" + source.getId(), 64))
                .packageName(truncate(name, 255))
                .price(price)
                .currencyCode(CURRENCY)
                .billingCycle(cycle)
                .maxUsers(source.getEndNo())
                .features(features.isEmpty() ? null : features.toString())
                .description(
                        "Migrated from MySQL modulePricing (pricing tier only; module scopes from ERP)")
                .build();
    }

    private static BillingCycleEnum resolveCycleFromCriteria(ModulePricingCriteria criteria) {
        if (criteria == null) {
            return BillingCycleEnum.YEARLY;
        }
        if (criteria.getCostType() != null) {
            return mapBillingCycle(blankToNull(criteria.getCostType().getSubscription()));
        }
        PayType payType = criteria.getPayType();
        if (payType != null) {
            if (payType.getDayCount() != null) {
                return mapBillingCycleFromDays(payType.getDayCount().intValue());
            }
            return mapBillingCycle(blankToNull(payType.getSubscriptionBasis()));
        }
        if (criteria.getNoOfDays() != null) {
            return mapBillingCycleFromDays(criteria.getNoOfDays());
        }
        return BillingCycleEnum.YEARLY;
    }

    public static CompanySubscriptionEntity fromCompanyValidity(
            CompanyValidity source, UUID companyId) {
        if (source == null || source.getId() == null || companyId == null) {
            return null;
        }
        LocalDate start = toLocalDate(source.getValidFrom());
        LocalDate end = toLocalDate(source.getValidTill());
        if (start == null || end == null) {
            return null;
        }
        String edition = blankToNull(source.getPaidEditionType());
        String subType = blankToNull(source.getPaidSubscriptionType());
        String packageName = edition != null ? edition : (subType != null ? subType : "Legacy subscription");
        return CompanySubscriptionEntity.builder()
                .mysqlId(source.getId())
                .companyId(companyId)
                .packageCode(truncate("LEGACY-CV-" + source.getId(), 64))
                .packageName(truncate(packageName, 255))
                .billingCycle(mapSubscriptionBillingCycle(subType != null ? subType : edition))
                .maxUsers(source.getTotalEmployee() != null ? source.getTotalEmployee().intValue() : null)
                .subscriptionStartDate(start)
                .subscriptionEndDate(end)
                .build();
    }

    public static SubscriptionPaymentHistoryEntity paymentFromCompanyValidity(
            CompanyValidity source, UUID companyId) {
        if (source == null || source.getId() == null || companyId == null) {
            return null;
        }
        String edition = blankToNull(source.getPaidEditionType());
        String subType = blankToNull(source.getPaidSubscriptionType());
        String packageName = edition != null ? edition : (subType != null ? subType : "Legacy subscription");
        BigDecimal amount = money(source.getPayAmount());
        BigDecimal discount = money(source.getManualDiscountAmount());
        SubscriptionDiscountTypeEnum discountType = discount.compareTo(BigDecimal.ZERO) > 0
                ? SubscriptionDiscountTypeEnum.FLAT
                : SubscriptionDiscountTypeEnum.NONE;
        BigDecimal original = amount.add(discount);
        return SubscriptionPaymentHistoryEntity.builder()
                .mysqlId(COMPANY_VALIDITY_PAYMENT_MYSQL_ID_OFFSET + source.getId())
                .companyId(companyId)
                .packageCode(truncate("LEGACY-CV-" + source.getId(), 64))
                .packageName(truncate(packageName, 255))
                .billingCycle(mapSubscriptionBillingCycle(subType != null ? subType : edition))
                .maxUsers(source.getTotalEmployee() != null ? source.getTotalEmployee().intValue() : null)
                .originalAmount(original)
                .amount(amount)
                .discountType(discountType)
                .discountValue(discount)
                .currencyCode(CURRENCY)
                .paymentMethod(source.getThirdPartyPaymentType() != null
                        ? source.getThirdPartyPaymentType().name()
                        : null)
                .paymentReference(blankToNull(source.getTransactionId()))
                .proofDocumentUrl(blankToNull(source.getVoucherPath()))
                .remarks(blankToNull(source.getVoucherNo()))
                .paymentStatus(mapPaymentStatus(source.getPaymentStatus()))
                .periodStartDate(toLocalDate(source.getValidFrom()))
                .periodEndDate(toLocalDate(source.getValidTill()))
                .verifiedAt(source.getPaymentStatus() == PaymentStatus.Verified
                        ? toLocalDateTime(source.getPayDate())
                        : null)
                .build();
    }

    public static SubscriptionPaymentHistoryEntity fromSubscriptionPayment(
            SubscriptionPayment source, UUID companyId) {
        if (source == null || source.getId() == null || companyId == null) {
            return null;
        }
        String paymentOf = blankToNull(source.getPaymentOf());
        String packageName = paymentOf != null ? paymentOf : "Legacy subscription payment";
        BigDecimal amount = money(source.getPayAmount());
        return SubscriptionPaymentHistoryEntity.builder()
                .mysqlId(source.getId())
                .companyId(companyId)
                .packageCode(truncate("LEGACY-SP-" + source.getId(), 64))
                .packageName(truncate(packageName, 255))
                .billingCycle(mapSubscriptionBillingCycleFromType(source.getSubscriptionType()))
                .maxUsers(null)
                .originalAmount(amount)
                .amount(amount)
                .discountType(SubscriptionDiscountTypeEnum.NONE)
                .discountValue(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .currencyCode(CURRENCY)
                .paymentMethod(blankToNull(source.getPaymentType()))
                .paymentReference(source.getBillNumber() != null
                        ? source.getBillNumber().toString()
                        : (source.getVoucherNumber() != null ? source.getVoucherNumber().toString() : null))
                .proofDocumentUrl(blankToNull(source.getScanImage()))
                .remarks(buildPaymentRemarks(source))
                .paymentStatus(mapPaymentStats(source.getPaymentStats()))
                .periodStartDate(toLocalDate(source.getPayDate()))
                .verifiedAt(source.getPaymentStats() == PaymentStats.VERIFIED
                        ? toLocalDateTime(source.getPayDate())
                        : null)
                .build();
    }

    public static BillingCycleEnum mapBillingCycle(String raw) {
        if (raw == null || raw.isBlank()) {
            return BillingCycleEnum.YEARLY;
        }
        String key = raw.trim().toLowerCase(Locale.ROOT).replace('_', ' ').replace('-', ' ');
        if (key.contains("10") && key.contains("year")) {
            return BillingCycleEnum.TEN_YEAR;
        }
        if (key.contains("5") && key.contains("year")) {
            return BillingCycleEnum.FIVE_YEAR;
        }
        if ((key.contains("2") || key.contains("two")) && key.contains("year")) {
            return BillingCycleEnum.TWO_YEAR;
        }
        if (key.contains("quarter")) {
            return BillingCycleEnum.QUARTERLY;
        }
        if (key.contains("month")) {
            return BillingCycleEnum.MONTHLY;
        }
        if (key.contains("year") || key.contains("annual")) {
            return BillingCycleEnum.YEARLY;
        }
        return BillingCycleEnum.YEARLY;
    }

    public static BillingCycleEnum mapBillingCycleFromDays(int days) {
        if (days <= 45) {
            return BillingCycleEnum.MONTHLY;
        }
        if (days <= 120) {
            return BillingCycleEnum.QUARTERLY;
        }
        if (days <= 500) {
            return BillingCycleEnum.YEARLY;
        }
        if (days <= 900) {
            return BillingCycleEnum.TWO_YEAR;
        }
        if (days <= 2000) {
            return BillingCycleEnum.FIVE_YEAR;
        }
        return BillingCycleEnum.TEN_YEAR;
    }

    public static SubscriptionBillingCycleEnum mapSubscriptionBillingCycle(String raw) {
        BillingCycleEnum cycle = mapBillingCycle(raw);
        return SubscriptionBillingCycleEnum.valueOf(cycle.name());
    }

    public static SubscriptionBillingCycleEnum mapSubscriptionBillingCycleFromType(Integer subscriptionType) {
        if (subscriptionType == null) {
            return SubscriptionBillingCycleEnum.YEARLY;
        }
        return switch (subscriptionType) {
            case 1 -> SubscriptionBillingCycleEnum.MONTHLY;
            case 3 -> SubscriptionBillingCycleEnum.QUARTERLY;
            case 12 -> SubscriptionBillingCycleEnum.YEARLY;
            case 24 -> SubscriptionBillingCycleEnum.TWO_YEAR;
            case 60 -> SubscriptionBillingCycleEnum.FIVE_YEAR;
            case 120 -> SubscriptionBillingCycleEnum.TEN_YEAR;
            default -> SubscriptionBillingCycleEnum.YEARLY;
        };
    }

    public static SubscriptionPaymentStatusEnum mapPaymentStatus(PaymentStatus status) {
        if (status == null) {
            return SubscriptionPaymentStatusEnum.PENDING;
        }
        return switch (status) {
            case Verified -> SubscriptionPaymentStatusEnum.VERIFIED;
            case Error -> SubscriptionPaymentStatusEnum.REJECTED;
            case Pending -> SubscriptionPaymentStatusEnum.PENDING;
        };
    }

    public static SubscriptionPaymentStatusEnum mapPaymentStats(PaymentStats stats) {
        if (stats == null) {
            return SubscriptionPaymentStatusEnum.PENDING;
        }
        return switch (stats) {
            case VERIFIED -> SubscriptionPaymentStatusEnum.VERIFIED;
            case VERIFY -> SubscriptionPaymentStatusEnum.PENDING;
        };
    }

    private static String buildPayPlanFeatures(PayPlan source) {
        StringBuilder sb = new StringBuilder();
        if (source.getStartNo() != null || source.getEndNo() != null) {
            sb.append("Users ")
                    .append(source.getStartNo() != null ? source.getStartNo() : "?")
                    .append("-")
                    .append(source.getEndNo() != null ? source.getEndNo() : "?");
        }
        if (source.getDiscountPercent() != null && source.getDiscountPercent() > 0) {
            if (!sb.isEmpty()) {
                sb.append("; ");
            }
            sb.append("Discount ").append(formatDecimal(source.getDiscountPercent())).append("%");
        }
        if (source.getIsActive() != null && !source.getIsActive()) {
            if (!sb.isEmpty()) {
                sb.append("; ");
            }
            sb.append("inactive in legacy");
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    private static String buildPaymentRemarks(SubscriptionPayment source) {
        StringBuilder sb = new StringBuilder();
        if (blankToNull(source.getPaidBy()) != null) {
            sb.append("paidBy=").append(source.getPaidBy());
        }
        if (blankToNull(source.getBankName()) != null) {
            if (!sb.isEmpty()) {
                sb.append("; ");
            }
            sb.append("bank=").append(source.getBankName());
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    private static BigDecimal money(Double value) {
        if (value == null || value.isNaN() || value.isInfinite()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private static String formatDecimal(Double value) {
        if (value == null) {
            return "0";
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZONE).toLocalDate();
    }

    private static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZONE).toLocalDateTime();
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
