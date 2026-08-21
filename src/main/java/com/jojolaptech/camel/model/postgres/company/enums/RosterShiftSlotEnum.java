package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

@Getter
public enum RosterShiftSlotEnum {
    MORNING("Morning", "06:00", "14:00"),
    DAY("Day", "09:00", "18:00"),
    EVENING("Evening", "14:00", "22:00"),
    NIGHT("Night", "22:00", "06:00"),
    OFF("Off", null, null);

    private final String displayName;
    private final String defaultStart;
    private final String defaultEnd;

    RosterShiftSlotEnum(String displayName, String defaultStart, String defaultEnd) {
        this.displayName = displayName;
        this.defaultStart = defaultStart;
        this.defaultEnd = defaultEnd;
    }
}
