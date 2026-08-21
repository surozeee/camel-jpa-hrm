package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

@Getter
public enum RosterScheduleTypeEnum {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    BIWEEKLY("Bi-Weekly"),
    MONTHLY("Monthly"),
    CUSTOM("Custom Date Range");

    private final String displayName;

    RosterScheduleTypeEnum(String displayName) {
        this.displayName = displayName;
    }
}
