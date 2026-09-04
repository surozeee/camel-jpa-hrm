package com.jojolaptech.camel.model.postgres.user.enums;

import lombok.Getter;

@Getter
public enum SubscriptionDiscountTypeEnum {
    NONE("No discount"),
    FLAT("Flat amount"),
    PERCENTAGE("Percentage");

    private final String label;

    SubscriptionDiscountTypeEnum(String label) {
        this.label = label;
    }
}
