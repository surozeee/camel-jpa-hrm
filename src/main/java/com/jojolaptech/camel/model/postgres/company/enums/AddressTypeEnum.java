package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

@Getter
public enum AddressTypeEnum {
    PERMANENT("Permanent Address"),
    TEMPORARY("Temporary Address");

    private final String displayName;

    AddressTypeEnum(String displayName) {
        this.displayName = displayName;
    }
}
