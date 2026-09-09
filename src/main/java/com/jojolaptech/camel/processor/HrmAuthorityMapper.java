package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.postgres.user.enums.PermissionForEnum;
import com.jojolaptech.camel.model.postgres.user.enums.UserTypeEnum;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Maps legacy HRM {@code ROLE_*} authorities onto ERP User-Service seed roles / user types.
 *
 * <p>Old Camel mapper stored bare codes ({@code ADMIN}, {@code COMPANY}). ERP {@code RoleSeedService}
 * expects display names ({@code Super Admin}, {@code Company Admin}).
 */
final class HrmAuthorityMapper {

    /** Legacy authority code (no {@code ROLE_} prefix) → ERP seed role display name. */
    private static final Map<String, String> LEGACY_CODE_TO_ERP_ROLE_NAME = Map.ofEntries(
            Map.entry("SUPER_ADMIN", "Super Admin"),
            Map.entry("SUPERADMIN", "Super Admin"),
            Map.entry("ADMIN", "Super Admin"),
            Map.entry("COMPANY", "Company Admin"),
            Map.entry("COMPANY_ADMIN", "Company Admin"),
            Map.entry("EMPLOYEE", "Employee"),
            Map.entry("BRANCH", "Branch Admin"),
            Map.entry("BRANCH_ADMIN", "Branch Admin"),
            Map.entry("DEPART", "Branch Manager"),
            Map.entry("BRANCH_MANAGER", "Branch Manager"),
            Map.entry("MANAGER", "Manager"),
            Map.entry("ACCOUNT", "Accountant"),
            Map.entry("ACCOUNTANT", "Accountant"));

    private HrmAuthorityMapper() {}

    /** Strips {@code ROLE_} and returns the legacy code (e.g. {@code COMPANY}). */
    static String roleCode(String authority) {
        if (authority == null || authority.isBlank()) {
            return "UNKNOWN";
        }
        String trimmed = authority.trim();
        if (trimmed.regionMatches(true, 0, "ROLE_", 0, 5)) {
            return trimmed.substring(5);
        }
        return trimmed;
    }

    /**
     * ERP role {@code name} for this authority — seed display name for platform roles, otherwise the
     * legacy code (fine-grained sec_role rows such as {@code BRANCH_ADD}).
     */
    static String roleName(String authority) {
        String code = roleCode(authority);
        if ("UNKNOWN".equals(code)) {
            return code;
        }
        String erpName = LEGACY_CODE_TO_ERP_ROLE_NAME.get(code.toUpperCase(Locale.ROOT));
        return erpName != null ? erpName : code;
    }

    static PermissionForEnum roleScope(String authority) {
        String code = roleCode(authority).toUpperCase(Locale.ROOT);
        // ERP RoleSeedService stores platform catalog roles as SYSTEM (null company/org/branch).
        return switch (code) {
            case "ADMIN",
                    "SUPERADMIN",
                    "SUPER_ADMIN",
                    "COMPANY",
                    "COMPANY_ADMIN",
                    "EMPLOYEE",
                    "MANAGER",
                    "ACCOUNT",
                    "ACCOUNTANT",
                    "BRANCH",
                    "BRANCH_ADMIN",
                    "DEPART",
                    "BRANCH_MANAGER" -> PermissionForEnum.SYSTEM;
            default -> PermissionForEnum.COMPANY;
        };
    }

    static List<UserTypeEnum> userTypes(Collection<String> authorities, boolean employeeLinked) {
        return List.of(resolvePrimaryUserType(authorities, employeeLinked));
    }

    static UserTypeEnum resolvePrimaryUserType(Collection<String> authorities, boolean employeeLinked) {
        if (authorities != null) {
            for (String authority : authorities) {
                if (userType(authority) == UserTypeEnum.SUPER_ADMIN) {
                    return UserTypeEnum.SUPER_ADMIN;
                }
            }
            for (String authority : authorities) {
                if (userType(authority) == UserTypeEnum.COMPANY_ADMIN) {
                    return UserTypeEnum.COMPANY_ADMIN;
                }
            }
            for (String authority : authorities) {
                if (userType(authority) == UserTypeEnum.BRANCH_ADMIN) {
                    return UserTypeEnum.BRANCH_ADMIN;
                }
            }
            for (String authority : authorities) {
                if (userType(authority) == UserTypeEnum.BRANCH_MANAGER) {
                    return UserTypeEnum.BRANCH_MANAGER;
                }
            }
            for (String authority : authorities) {
                if (userType(authority) == UserTypeEnum.MANAGER) {
                    return UserTypeEnum.MANAGER;
                }
            }
            for (String authority : authorities) {
                if (userType(authority) == UserTypeEnum.BRANCH_USER) {
                    return UserTypeEnum.BRANCH_USER;
                }
            }
        }
        if (employeeLinked) {
            return UserTypeEnum.EMPLOYEE;
        }
        if (authorities != null) {
            for (String authority : authorities) {
                if (userType(authority) == UserTypeEnum.EMPLOYEE) {
                    return UserTypeEnum.EMPLOYEE;
                }
            }
        }
        return UserTypeEnum.COMPANY_USER;
    }

    static UserTypeEnum userType(String authority) {
        if (authority == null) {
            return null;
        }
        String code = roleCode(authority).toUpperCase(Locale.ROOT);
        return switch (code) {
            case "ADMIN", "SUPERADMIN", "SUPER_ADMIN" -> UserTypeEnum.SUPER_ADMIN;
            case "COMPANY", "COMPANY_ADMIN" -> UserTypeEnum.COMPANY_ADMIN;
            case "EMPLOYEE" -> UserTypeEnum.EMPLOYEE;
            case "BRANCH", "BRANCH_ADMIN" -> UserTypeEnum.BRANCH_ADMIN;
            case "DEPART", "BRANCH_MANAGER" -> UserTypeEnum.BRANCH_MANAGER;
            case "MANAGER" -> UserTypeEnum.MANAGER;
            case "ACCOUNT", "ACCOUNTANT" -> UserTypeEnum.ACCOUNTANT;
            default -> null;
        };
    }

    static List<String> splitConfigAttributes(String configAttribute) {
        if (configAttribute == null || configAttribute.isBlank()) {
            return List.of();
        }
        List<String> attributes = new ArrayList<>();
        for (String part : configAttribute.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty() && trimmed.regionMatches(true, 0, "ROLE_", 0, 5)) {
                attributes.add(trimmed);
            }
        }
        return attributes;
    }

    static String permissionCode(String url, Long mysqlId) {
        String raw = url == null || url.isBlank() ? "legacy." + mysqlId : url.trim();
        if (raw.length() <= 250) {
            return raw;
        }
        return raw.substring(0, 230) + "." + mysqlId;
    }
}
