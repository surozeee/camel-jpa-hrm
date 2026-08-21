package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

@Getter
public enum FiscalCalendarTypeEnum {
    AD("Gregorian (AD)"),
    BS("Nepali (BS)");

    private final String displayName;

    FiscalCalendarTypeEnum(String displayName) {
        this.displayName = displayName;
    }
}
