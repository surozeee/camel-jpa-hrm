package com.jojolaptech.camel.model.postgres.user.enums;

/**
 * Scope at which a permission applies. A permission may apply to multiple scopes.
 */
public enum PermissionForEnum {
    SYSTEM,
    ORGANIZATION,
    COMPANY,
    BRANCH
}
