package com.jojolaptech.camel.model.postgres.master.enums;

import lombok.Getter;

@Getter
public enum BillingCycleEnum {
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    YEARLY("Yearly"),
    TWO_YEAR("2 Year"),
    FIVE_YEAR("5 Year"),
    TEN_YEAR("10 Year");

    private final String label;

    BillingCycleEnum(String label) {
        this.label = label;
    }
}
