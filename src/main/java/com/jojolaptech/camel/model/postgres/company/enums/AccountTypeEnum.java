package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

@Getter
public enum AccountTypeEnum {
    SAVINGS("Savings Account"),
    CURRENT("Current Account"),
    FIXED_DEPOSIT("Fixed Deposit"),
    RECURRING_DEPOSIT("Recurring Deposit"),
    SALARY("Salary Account"),
    JOINT("Joint Account"),
    CORPORATE("Corporate Account"),
    OTHER("Other");

    private final String displayName;

    AccountTypeEnum(String displayName) {
        this.displayName = displayName;
    }
}

