package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

@Getter
public enum BranchFiscalYearStatusEnum {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    CLOSED("Closed"),
    PENDING("Pending");

    private final String displayName;

    BranchFiscalYearStatusEnum(String displayName) {
        this.displayName = displayName;
    }
}
