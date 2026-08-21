package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

@Getter
public enum LeaveRoundRuleEnum {
    UP("Up"),
    DOWN("Down"),
    NEAREST_HALF("Nearest 0.5");

    private final String displayName;

    LeaveRoundRuleEnum(String displayName) {
        this.displayName = displayName;
    }
}
