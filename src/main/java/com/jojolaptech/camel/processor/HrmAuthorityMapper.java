package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.postgres.user.enums.PermissionForEnum;
import com.jojolaptech.camel.model.postgres.user.enums.UserTypeEnum;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

final class HrmAuthorityMapper {

    private HrmAuthorityMapper() {}

    static String roleName(String authority) {
        if (authority == null || authority.isBlank()) {
            return "UNKNOWN";
        }
        String trimmed = authority.trim();
        if (trimmed.regionMatches(true, 0, "ROLE_", 0, 5)) {
            return trimmed.substring(5);
        }
        return trimmed;
    }

    static PermissionForEnum roleScope(String authority) {
        String name = roleName(authority).toUpperCase(Locale.ROOT);
        return switch (name) {
            case "ADMIN", "SUPERADMIN", "SUPER_ADMIN" -> PermissionForEnum.SYSTEM;
            case "DEPART" -> PermissionForEnum.BRANCH;
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
        String name = roleName(authority).toUpperCase(Locale.ROOT);
        return switch (name) {
            case "ADMIN", "SUPERADMIN", "SUPER_ADMIN" -> UserTypeEnum.SUPER_ADMIN;
            case "COMPANY" -> UserTypeEnum.COMPANY_ADMIN;
            case "EMPLOYEE" -> UserTypeEnum.EMPLOYEE;
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
