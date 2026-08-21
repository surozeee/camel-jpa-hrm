package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

/**
 * Stepper steps for company creation flow.
 * Stored on company entity to track progress in DB.
 */
@Getter
public enum CompanyStepperEnum {
    COMPANY_DETAIL("Company detail"),
    BRANCH("Branch"),
    DOCUMENT("Document"),
    REVIEW("Review"),
    COMPLETE("Complete");

    private final String displayName;

    CompanyStepperEnum(String displayName) {
        this.displayName = displayName;
    }
}
