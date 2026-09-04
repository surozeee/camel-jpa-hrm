package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

@Getter
public enum EmployeeInsuranceTypeEnum {
    LIFE("Life"),
    HEALTH("Health"),
    ACCIDENT("Accident"),
    MEDICAL("Medical"),
    OTHER("Other");

    private final String displayName;

    EmployeeInsuranceTypeEnum(String displayName) {
        this.displayName = displayName;
    }
}
