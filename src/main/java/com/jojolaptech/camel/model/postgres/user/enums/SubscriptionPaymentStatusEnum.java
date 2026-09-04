package com.jojolaptech.camel.model.postgres.user.enums;

import lombok.Getter;

@Getter
public enum SubscriptionPaymentStatusEnum {
    PENDING("Pending verification"),
    VERIFIED("Verified"),
    REJECTED("Rejected");

    private final String label;

    SubscriptionPaymentStatusEnum(String label) {
        this.label = label;
    }
}
