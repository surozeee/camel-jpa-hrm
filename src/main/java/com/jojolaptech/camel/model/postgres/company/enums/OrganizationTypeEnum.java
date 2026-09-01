package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

@Getter
public enum OrganizationTypeEnum {
    COMPANY("Company", "Private or commercial business organization"),
    GOVERNMENT("Government Organization", "Government ministries, departments, and agencies"),
    NGO("Non-Governmental Organization", "Local non-governmental organization"),
    NON_PROFIT("Non-Profit Organization", "Charitable or non-profit entity"),
    COOPERATIVE("Cooperative", "Member-owned cooperative organization"),
    EDUCATIONAL("Educational Institution", "School, college, university, or training center"),
    HEALTHCARE("Healthcare Organization", "Hospital, clinic, or healthcare provider"),
    OTHER("Other", "Custom organization type");

    private final String displayName;
    private final String description;

    OrganizationTypeEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
