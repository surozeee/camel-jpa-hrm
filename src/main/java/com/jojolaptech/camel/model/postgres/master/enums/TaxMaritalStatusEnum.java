package com.jojolaptech.camel.model.postgres.master.enums;

import lombok.Getter;

/** Marital status for Nepali income tax slab assessment. */
@Getter
public enum TaxMaritalStatusEnum {
    SINGLE("Single"),
    MARRIED("Married");

    private final String displayName;

    TaxMaritalStatusEnum(String displayName) {
        this.displayName = displayName;
    }
}
