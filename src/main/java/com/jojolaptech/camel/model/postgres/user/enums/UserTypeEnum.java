package com.jojolaptech.camel.model.postgres.user.enums;

import lombok.Getter;

@Getter
public enum UserTypeEnum {

    SUPER_ADMIN("SUPER_ADMIN", "Super Admin"),
    ADMIN("ADMIN", "Admin"),
    MANAGER("MANAGER", "Manager"),
    ORGANIZATION_ADMIN("ORGANIZATION_ADMIN", "Organization Admin"),
    ORGANIZATION_MANAGER("ORGANIZATION_MANAGER", "Organization Manager"),
    ORGANIZATION_USER("ORGANIZATION_USER", "Organization User"),
    COMPANY_ADMIN("COMPANY_ADMIN", "Company Admin"),
    COMPANY_MANAGER("COMPANY_MANAGER", "Company Manager"),
    COMPANY_USER("COMPANY_USER", "Company User"),
    BRANCH_ADMIN("BRANCH_ADMIN", "Branch Admin"),
    BRANCH_MANAGER("BRANCH_MANAGER", "Branch Manager"),
    BRANCH_USER("BRANCH_USER", "Branch User"),
    ACCOUNTANT("ACCOUNTANT", "Accountant"),
    EMPLOYEE("EMPLOYEE", "Employee"),
    CUSTOMER("CUSTOMER", "Customer"),
    PARTNER("PARTNER", "Partner"),
    VENDOR("VENDOR", "Vendor"),
    /** Brings companies; sees only companies linked on profile. */
    DEALER("DEALER", "Dealer"),
    /** Sets up device MAC and employee device enrollment at branch. */
    INSTALLER("INSTALLER", "Installer"),
    /** Platform support staff for tickets and help desk. */
    SUPPORT("SUPPORT", "Support"),
    /** Single navigation tree for all roles; visibility is driven by permissions. */
    SHARED("SHARED", "Shared");

    private final String code;
    private final String displayName;

    UserTypeEnum(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
}