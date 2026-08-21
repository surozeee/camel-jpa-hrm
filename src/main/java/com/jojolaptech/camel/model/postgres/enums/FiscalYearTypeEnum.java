package com.jojolaptech.camel.model.postgres.enums;

import lombok.Getter;

@Getter
public enum FiscalYearTypeEnum {

    CALENDAR_YEAR("January – December"),
    JULY_YEAR("July – June"),
    APRIL_YEAR("April – March"),
    OCTOBER_YEAR("October – September"),
    BIKRAM_SAMBAT("Baisakh – Chaitra"),
    NEPALI_YEAR("Shrawan – Asar"),
    CHINESE("January – December"),
    RUSSIAN("January – December");

    private final String displayName;

    FiscalYearTypeEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    
}

