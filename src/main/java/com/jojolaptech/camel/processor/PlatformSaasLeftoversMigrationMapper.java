package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.ApplicationModule;
import com.jojolaptech.camel.model.mysql.MarketingPersonDetail;
import com.jojolaptech.camel.model.mysql.PayType;
import com.jojolaptech.camel.model.mysql.PricingEstimateEmailDetails;
import com.jojolaptech.camel.model.mysql.UserLicense;
import com.jojolaptech.camel.model.postgres.master.MasterLookupEntity;
import com.jojolaptech.camel.model.postgres.master.ModulePricingPackageEntity;
import com.jojolaptech.camel.model.postgres.master.enums.BillingCycleEnum;
import com.jojolaptech.camel.model.postgres.master.enums.MasterLookupCategoryEnum;
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
import java.util.UUID;

/** Maps remaining SaaS/marketing leftovers into packages, subscriptions, and master_lookup. */
public final class PlatformSaasLeftoversMigrationMapper {

    public static final long PAY_TYPE_MYSQL_ID_OFFSET = 34_000_000_000_000L;
    public static final long USER_LICENSE_PAYMENT_MYSQL_ID_OFFSET = 35_000_000_000_000L;
    /** company_subscription rows created from userLicense (avoid collide with companyValidity ids). */
    public static final long USER_LICENSE_SUBSCRIPTION_MYSQL_ID_OFFSET = 39_000_000_000_000L;
    public static final long MARKETING_PERSON_MYSQL_ID_OFFSET = 36_000_000_000_000L;
    public static final long PRICING_ESTIMATE_MYSQL_ID_OFFSET = 37_000_000_000_000L;
    public static final long LEGACY_APP_MODULE_MYSQL_ID_OFFSET = 38_000_000_000_000L;

    private static final String CURRENCY = "NPR";
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private PlatformSaasLeftoversMigrationMapper() {}

    public static ModulePricingPackageEntity fromPayType(PayType source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        String basis = blankToNull(source.getSubscriptionBasis());
        String name = basis != null ? basis : "PayType " + source.getId();
        BillingCycleEnum cycle = basis != null
                ? SaasBillingMigrationMapper.mapBillingCycle(basis)
                : (source.getDayCount() != null
                        ? SaasBillingMigrationMapper.mapBillingCycleFromDays(source.getDayCount().intValue())
                        : BillingCycleEnum.YEARLY);
        String features = "Legacy PayType discount="
                + (source.getDiscount() != null ? source.getDiscount() : 0)
                + "%; dayCount="
                + (source.getDayCount() != null ? source.getDayCount() : "?");
        return ModulePricingPackageEntity.builder()
                .mysqlId(PAY_TYPE_MYSQL_ID_OFFSET + source.getId())
                .packageCode(truncate("LEGACY-PT-" + source.getId(), 64))
                .packageName(truncate(name, 255))
                .price(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .currencyCode(CURRENCY)
                .billingCycle(cycle)
                .maxUsers(null)
                .features(features)
                .description("Migrated from MySQL payType (subscription basis catalog)")
                .build();
    }

    public static CompanySubscriptionEntity fromUserLicense(UserLicense source, UUID companyId) {
        if (source == null || source.getId() == null || companyId == null) {
            return null;
        }
        LocalDate start = toLocalDate(source.getStartDate());
        LocalDate end = toLocalDate(source.getValidTill());
        if (start == null || end == null) {
            return null;
        }
        String packageName = blankToNull(source.getModalId());
        if (packageName == null) {
            packageName = "User license";
        }
        return CompanySubscriptionEntity.builder()
                .mysqlId(USER_LICENSE_SUBSCRIPTION_MYSQL_ID_OFFSET + source.getId())
                .companyId(companyId)
                .packageCode(truncate("LEGACY-UL-" + source.getId(), 64))
                .packageName(truncate(packageName, 255))
                .billingCycle(SubscriptionBillingCycleEnum.YEARLY)
                .maxUsers(source.getUserCount())
                .subscriptionStartDate(start)
                .subscriptionEndDate(end)
                .build();
    }

    public static SubscriptionPaymentHistoryEntity paymentFromUserLicense(
            UserLicense source, UUID companyId) {
        if (source == null || source.getId() == null || companyId == null) {
            return null;
        }
        String packageName = blankToNull(source.getModalId());
        if (packageName == null) {
            packageName = "User license";
        }
        LocalDateTime paidAt = toLocalDateTime(source.getPaidDate());
        return SubscriptionPaymentHistoryEntity.builder()
                .mysqlId(USER_LICENSE_PAYMENT_MYSQL_ID_OFFSET + source.getId())
                .companyId(companyId)
                .packageCode(truncate("LEGACY-UL-" + source.getId(), 64))
                .packageName(truncate(packageName, 255))
                .billingCycle(SubscriptionBillingCycleEnum.YEARLY)
                .maxUsers(source.getUserCount())
                .originalAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .amount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .discountType(SubscriptionDiscountTypeEnum.NONE)
                .discountValue(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .currencyCode(CURRENCY)
                .paymentMethod(source.getPaidBy() != null ? source.getPaidBy().name() : null)
                .remarks(blankToNull(source.getRemarkValidTill()))
                .paymentStatus(SubscriptionPaymentStatusEnum.VERIFIED)
                .periodStartDate(toLocalDate(source.getStartDate()))
                .periodEndDate(toLocalDate(source.getValidTill()))
                .verifiedAt(paidAt)
                .build();
    }

    public static MasterLookupEntity fromMarketingPerson(MarketingPersonDetail source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        String name = blankToNull(source.getFullname());
        if (name == null) {
            return null;
        }
        String desc = "email=" + nullToEmpty(source.getEmail()) + "; phone=" + nullToEmpty(source.getPhone());
        return MasterLookupEntity.builder()
                .mysqlId(MARKETING_PERSON_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.MARKETING_PERSON)
                .code(truncate("MKT-" + source.getId(), 64))
                .name(truncate(name, 255))
                .description(desc)
                .sortOrder(source.getId().intValue())
                .build();
    }

    public static MasterLookupEntity fromPricingEstimate(PricingEstimateEmailDetails source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        String email = blankToNull(source.getEmail());
        if (email == null) {
            return null;
        }
        StringBuilder name = new StringBuilder();
        if (blankToNull(source.getFirstName()) != null) {
            name.append(source.getFirstName().trim());
        }
        if (blankToNull(source.getLastName()) != null) {
            if (!name.isEmpty()) {
                name.append(' ');
            }
            name.append(source.getLastName().trim());
        }
        if (name.isEmpty()) {
            name.append(email);
        }
        StringBuilder desc = new StringBuilder();
        appendKv(desc, "email", email);
        appendKv(desc, "company", source.getCompany());
        appendKv(desc, "companyUrl", source.getCompanyUrl());
        appendKv(desc, "title", source.getTitle());
        appendKv(desc, "phone", source.getPhone());
        appendKv(desc, "fax", source.getFax());
        appendKv(desc, "estimateParameter", source.getEstimateParameter());
        return MasterLookupEntity.builder()
                .mysqlId(PRICING_ESTIMATE_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.PRICING_ESTIMATE_LEAD)
                .code(truncate("PEL-" + source.getId(), 64))
                .name(truncate(name.toString(), 255))
                .description(desc.toString())
                .sortOrder(source.getId().intValue())
                .build();
    }

    public static MasterLookupEntity fromApplicationModule(ApplicationModule source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        String name = blankToNull(source.getModuleName());
        if (name == null) {
            return null;
        }
        StringBuilder desc = new StringBuilder();
        appendKv(desc, "freeDays", source.getFreeDays());
        appendKv(desc, "freeUsers", source.getFreeUsers());
        appendKv(desc, "discount", source.getDiscount());
        appendKv(desc, "mainModule", source.getMainModule());
        appendKv(desc, "remarks", source.getRemarks());
        desc.append(desc.isEmpty() ? "" : "; ").append("ERP module tree/scopes seeded separately");
        return MasterLookupEntity.builder()
                .mysqlId(LEGACY_APP_MODULE_MYSQL_ID_OFFSET + source.getId())
                .category(MasterLookupCategoryEnum.LEGACY_APP_MODULE)
                .code(truncate("APP-MOD-" + source.getId(), 64))
                .name(truncate(name, 255))
                .description(desc.toString())
                .sortOrder(source.getId().intValue())
                .build();
    }

    private static void appendKv(StringBuilder sb, String key, Object value) {
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

    private static String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
