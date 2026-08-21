package com.jojolaptech.camel.model.postgres.user.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {

    ACTIVE("Active"),
    BLOCKED("Blocked"),
    INACTIVE("Inactive"),
    DELETED("Deleted"),
    LOCKED("Locked");

    private final String status;

    UserStatusEnum(String status) {
        this.status = status;
    }
}
