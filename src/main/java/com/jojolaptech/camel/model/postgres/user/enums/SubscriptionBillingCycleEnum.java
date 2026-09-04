package com.jojolaptech.camel.model.postgres.user.enums;

import lombok.Getter;

@Getter
public enum SubscriptionBillingCycleEnum {
    MONTHLY("Monthly", 1),
    QUARTERLY("Quarterly", 3),
    YEARLY("Yearly", 12),
    TWO_YEAR("2 Year", 24),
    FIVE_YEAR("5 Year", 60),
    TEN_YEAR("10 Year", 120);

    private final String label;
    private final int months;

    SubscriptionBillingCycleEnum(String label, int months) {
        this.label = label;
        this.months = months;
    }
}
