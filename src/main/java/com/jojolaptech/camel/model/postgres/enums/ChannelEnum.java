package com.jojolaptech.camel.model.postgres.enums;

import lombok.Getter;

@Getter
public enum ChannelEnum {

    SMS("SMS", "Channel via text messaging (Short Message Service)"),
    EMAIL("Email", "Channel via electronic mail"),
    NOTIFICATION("Notification", "Channel via app or system notifications"),
    PUSH_NOTIFICATION("Push Notification", "Channel via push notifications to devices");

    private final String displayName;
    private final String description;

    ChannelEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
