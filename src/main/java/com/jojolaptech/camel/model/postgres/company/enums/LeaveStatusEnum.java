package com.jojolaptech.camel.model.postgres.company.enums;

public enum LeaveStatusEnum {
    PENDING_SUPERVISOR_REVIEW,
    SUPERVISOR_RECOMMENDED,
    WITHDRAWAL_REQUESTED,
    REJECTED_BY_SUPERVISOR,
    REJECTED_BY_FINAL_APPROVER,
    APPROVED,
    CANCELLED
}
