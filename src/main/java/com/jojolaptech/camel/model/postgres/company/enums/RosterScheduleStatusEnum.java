package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

@Getter
public enum RosterScheduleStatusEnum {
    DRAFT("Draft"),
    PENDING_SUPERVISOR_REVIEW("Pending Supervisor"),
    SUPERVISOR_RECOMMENDED("Recommended"),
    APPROVED("Approved"),
    PUBLISHED("Published"),
    REJECTED_BY_SUPERVISOR("Rejected by Supervisor"),
    REJECTED_BY_FINAL_APPROVER("Rejected by Final Approver"),
    CANCELLED("Cancelled"),
    ARCHIVED("Archived");

    private final String displayName;

    RosterScheduleStatusEnum(String displayName) {
        this.displayName = displayName;
    }
}
