package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyEmployee;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.user.enums.PermissionForEnum;
import com.jojolaptech.camel.model.postgres.user.enums.UserTypeEnum;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

final class UserPortalLinkMapper {

    enum PortalKind {
        EMPLOYEE,
        COMPANY,
        BRANCH,
        NONE
    }

    private UserPortalLinkMapper() {}

    static PortalKind resolvePortalKind(
            UserTypeEnum userType, PermissionForEnum roleScope, boolean employeeLinked, boolean employeeMigrated) {
        if (employeeLinked && employeeMigrated) {
            return PortalKind.EMPLOYEE;
        }
        if (userType == UserTypeEnum.SUPER_ADMIN) {
            return PortalKind.NONE;
        }
        if (roleScope == PermissionForEnum.BRANCH) {
            return PortalKind.BRANCH;
        }
        if (userType == UserTypeEnum.COMPANY_ADMIN || roleScope == PermissionForEnum.COMPANY) {
            return PortalKind.COMPANY;
        }
        if (employeeLinked) {
            return PortalKind.COMPANY;
        }
        return PortalKind.NONE;
    }

    static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    static CompanyEntity resolveCompanyByEmail(String email, Map<String, CompanyEntity> companyByEmail) {
        String normalized = normalizeEmail(email);
        if (normalized == null) {
            return null;
        }
        return companyByEmail.get(normalized);
    }

    static CompanyEntity resolveCompanyFromEmployee(
            Long employeeMysqlId,
            Map<Long, CompanyEmployee> activeCompanyEmployeeByEmployeeMysqlId,
            Map<Long, CompanyEntity> companiesByMysqlId) {
        if (employeeMysqlId == null) {
            return null;
        }
        CompanyEmployee companyEmployee = activeCompanyEmployeeByEmployeeMysqlId.get(employeeMysqlId);
        if (companyEmployee == null || companyEmployee.getCompany() == null) {
            return null;
        }
        return companiesByMysqlId.get(companyEmployee.getCompany().getId());
    }

    static UUID resolveCompanyId(
            EmployeeEntity employee,
            Map<Long, CompanyEmployee> activeCompanyEmployeeByEmployeeMysqlId,
            Map<Long, CompanyEntity> companiesByMysqlId) {
        if (employee == null) {
            return null;
        }
        CompanyEntity company = resolveCompanyFromEmployee(
                employee.getMysqlId(), activeCompanyEmployeeByEmployeeMysqlId, companiesByMysqlId);
        return company != null ? company.getId() : null;
    }

    static UUID resolveBranchId(
            EmployeeEntity employee,
            Map<Long, CompanyEmployee> activeCompanyEmployeeByEmployeeMysqlId,
            Map<Long, CompanyEntity> companiesByMysqlId,
            Map<Long, List<BranchEntity>> branchesByCompanyMysqlId) {
        if (employee != null && employee.getBranchId() != null) {
            return employee.getBranchId();
        }
        CompanyEntity company = resolveCompanyFromEmployee(
                employee != null ? employee.getMysqlId() : null,
                activeCompanyEmployeeByEmployeeMysqlId,
                companiesByMysqlId);
        if (company == null || company.getMysqlId() == null) {
            return null;
        }
        List<BranchEntity> branches = branchesByCompanyMysqlId.get(company.getMysqlId());
        if (branches == null || branches.isEmpty()) {
            return null;
        }
        return branches.getFirst().getId();
    }

    static UUID resolveBranchCompanyId(UUID branchId, Map<UUID, BranchEntity> branchesByUuid) {
        if (branchId == null) {
            return null;
        }
        BranchEntity branch = branchesByUuid.get(branchId);
        if (branch == null || branch.getCompany() == null) {
            return null;
        }
        return branch.getCompany().getId();
    }
}
