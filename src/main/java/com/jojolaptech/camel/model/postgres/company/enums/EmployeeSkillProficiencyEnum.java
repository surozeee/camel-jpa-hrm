package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

@Getter
public enum EmployeeSkillProficiencyEnum {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
    EXPERT("Expert");

    private final String displayName;

    EmployeeSkillProficiencyEnum(String displayName) {
        this.displayName = displayName;
    }
}
