package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

/** Unit for accumulation amount. */
@Getter
public enum LeaveAccumulationUnitEnum {
    DAYS("Days"),
    HOURS("Hours");

    private final String displayName;

    LeaveAccumulationUnitEnum(String displayName) {
        this.displayName = displayName;
    }
}
