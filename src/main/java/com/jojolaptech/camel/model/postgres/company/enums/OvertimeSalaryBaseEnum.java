package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

/**
 * Payroll base used with {@code overtimeRateMultiplier} when computing overtime pay.
 */
@Getter
public enum OvertimeSalaryBaseEnum {
    BASIC("Basic salary"),
    GROSS("Gross salary"),
    BASIC_GRADE("Basic and Grade salary");

    private final String displayName;

    OvertimeSalaryBaseEnum(String displayName) {
        this.displayName = displayName;
    }
}
