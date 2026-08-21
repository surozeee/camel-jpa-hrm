package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

@Getter
public enum LeaveExpiryPolicyEnum {
    NO_EXPIRY("No expiry"),
    END_OF_FISCAL_YEAR("End of fiscal year"),
    AFTER_3_MONTHS("After 3 months"),
    AFTER_6_MONTHS("After 6 months"),
    CUSTOM("Custom");

    private final String displayName;

    LeaveExpiryPolicyEnum(String displayName) {
        this.displayName = displayName;
    }
}
