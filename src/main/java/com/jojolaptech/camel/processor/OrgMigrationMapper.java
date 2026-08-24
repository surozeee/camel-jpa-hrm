package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Branch;
import com.jojolaptech.camel.model.mysql.Company;
import com.jojolaptech.camel.model.mysql.Department;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class OrgMigrationMapper {

    private OrgMigrationMapper() {}

    static String departmentKey(Long departmentMysqlId, Long branchMysqlId) {
        return departmentMysqlId + ":" + branchMysqlId;
    }

    static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    static String companyDescription(Company source) {
        return joinParts(source.getAddress(), source.getFax() == null ? null : "Fax: " + source.getFax());
    }

    static String branchDescription(Branch source) {
        return joinParts(source.getAddress(), source.getFaxNo() == null ? null : "Fax: " + source.getFaxNo());
    }

    static String departmentDescription(Department source) {
        return joinParts(
                source.getCode() == null ? null : "Code: " + source.getCode(),
                source.getAddress(),
                source.getPhone() == null ? null : "Phone: " + source.getPhone(),
                source.getEmail() == null ? null : "Email: " + source.getEmail());
    }

    static StatusEnum companyStatus(Company source) {
        if (Boolean.TRUE.equals(source.getIsArchive())) {
            return StatusEnum.INACTIVE;
        }
        return StatusEnum.ACTIVE;
    }

    static boolean isHeadOffice(Branch source) {
        if (source.getIsBranch() != null) {
            return !source.getIsBranch();
        }
        if (source.getParentBranch() != null && source.getParentBranch().getId().equals(source.getId())) {
            return true;
        }
        return false;
    }

    static String normalizeName(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }

    private static String joinParts(String... parts) {
        String joined = Stream.of(parts)
                .map(OrgMigrationMapper::trimToNull)
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining(" | "));
        return joined.isBlank() ? null : joined;
    }
}
