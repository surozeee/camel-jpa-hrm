package com.jojolaptech.camel.model.postgres.user.enums;

import lombok.Getter;

@Getter
public enum AuthProviderEnum {

    OAUTH2("OAUTH2", "Oauth2"),
    FACEBOOK("FACEBOOK", "Facebook"),
    GOOGLE("GOOGLE", "Google"),
    DEFAULT("DEFAULT", "Default");

    private final String code;
    private final String displayName;

    AuthProviderEnum(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
}
