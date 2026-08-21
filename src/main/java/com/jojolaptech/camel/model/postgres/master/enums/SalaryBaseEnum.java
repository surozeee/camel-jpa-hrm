package com.jojolaptech.camel.model.postgres.master.enums;

import lombok.Getter;

@Getter
public enum SalaryBaseEnum {
    GROSS("Gross amount"),
    BASIC("Basic amount"),
    NET("Net amount");

    private final String displayName;

    SalaryBaseEnum(String displayName) {
        this.displayName = displayName;
    }
}
