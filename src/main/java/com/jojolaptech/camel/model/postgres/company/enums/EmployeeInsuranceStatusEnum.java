package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

@Getter
public enum EmployeeInsuranceStatusEnum {
    ACTIVE("Active"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled");

    private final String displayName;

    EmployeeInsuranceStatusEnum(String displayName) {
        this.displayName = displayName;
    }
}
