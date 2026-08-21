package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

/**
 * Leave accumulation rule scope. Lower {@link #priority} wins when multiple rules match.
 */
@Getter
public enum LeaveAccumulationScopeEnum {
    EMPLOYEE(1),
    GRADE(2),
    DESIGNATION(3),
    DEPARTMENT(4),
    BRANCH(5),
    COMPANY(6);

    private final int priority;

    LeaveAccumulationScopeEnum(int priority) {
        this.priority = priority;
    }
}
