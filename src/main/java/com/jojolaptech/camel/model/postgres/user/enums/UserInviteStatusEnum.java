package com.jojolaptech.camel.model.postgres.user.enums;

/**
 * Employee portal invite lifecycle (stronger than legacy Code-only join link).
 */
public enum UserInviteStatusEnum {
    PENDING,
    ACCEPTED,
    EXPIRED,
    REVOKED
}
