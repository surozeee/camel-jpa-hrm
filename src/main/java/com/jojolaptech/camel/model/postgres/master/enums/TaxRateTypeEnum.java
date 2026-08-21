package com.jojolaptech.camel.model.postgres.master.enums;

import lombok.Getter;

@Getter
public enum TaxRateTypeEnum {
    PERCENTAGE("Percentage"),
    FLAT("Flat amount");

    private final String displayName;

    TaxRateTypeEnum(String displayName) {
        this.displayName = displayName;
    }
}
