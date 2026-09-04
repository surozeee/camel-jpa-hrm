package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

@Getter
public enum PremiumFrequencyEnum {
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    ANNUAL("Annual"),
    ONE_TIME("One time");

    private final String displayName;

    PremiumFrequencyEnum(String displayName) {
        this.displayName = displayName;
    }
}
