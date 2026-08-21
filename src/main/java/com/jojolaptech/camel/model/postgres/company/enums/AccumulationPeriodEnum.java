package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

@Getter
public enum AccumulationPeriodEnum {
    DAILY("Daily"),
    WEEK("Week"),
    BI_WEEKLY("Bi-Weekly"),
    MONTH("Month"),
    QUARTER("Quarter"),
    SEMI_ANNUALLY("Semi-Annually"),
    YEARLY("Yearly");

    private final String displayName;

    AccumulationPeriodEnum(String displayName) {
        this.displayName = displayName;
    }
}

