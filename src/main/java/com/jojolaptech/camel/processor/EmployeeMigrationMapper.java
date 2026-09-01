package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyEmployee;
import com.jojolaptech.camel.model.mysql.Employee;
import com.jojolaptech.camel.model.mysql.EmployeeBranch;
import com.jojolaptech.camel.model.mysql.EmployeeBranchDepartment;
import com.jojolaptech.camel.model.mysql.SecUser;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.DepartmentEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class EmployeeMigrationMapper {

    private EmployeeMigrationMapper() {
    }

    static EmployeeEntity toEmployee(
            Employee source,
            CompanyEmployee companyEmployee,
            EmployeeBranchDepartment branchDepartment,
            EmployeeBranch employeeBranch,
            Map<Long, BranchEntity> branchByMysqlId,
            Map<Long, List<BranchEntity>> branchesByCompanyMysqlId,
            Map<String, DepartmentEntity> departmentByKey,
            Map<Long, SecUser> secUserByEmployeeId,
            Set<String> reservedEmails,
            Set<String> reservedEmployeeCodes) {
        BranchEntity branch = resolveBranch(
                branchDepartment, employeeBranch, companyEmployee, branchByMysqlId, branchesByCompanyMysqlId);
        DepartmentEntity department = resolveDepartment(branchDepartment, branch, departmentByKey);
        String employeeCode = resolveEmployeeCode(source.getId(), companyEmployee, reservedEmployeeCodes);
        String email = resolveEmail(source.getId(), employeeCode, secUserByEmployeeId.get(source.getId()), reservedEmails);

        return EmployeeEntity.builder()
                .mysqlId(source.getId())
                .employeeCode(employeeCode)
                .enrollId(companyEmployee != null && companyEmployee.getEnrollId() != null
                        ? String.valueOf(companyEmployee.getEnrollId())
                        : null)
                .firstName(defaultName(source.getName(), "Employee"))
                .middleName(trimToNull(source.getMiddleName()))
                .lastName(defaultName(source.getLastname(), String.valueOf(source.getId())))
                .email(email)
                .phoneNumber(defaultPhone(source.getPhone()))
                .dateOfBirth(toLocalDate(source.getBirthday()))
                .hireDate(resolveHireDate(source, companyEmployee, branchDepartment, employeeBranch))
                .terminationDate(companyEmployee != null ? toLocalDate(companyEmployee.getTerminationDate()) : null)
                .branchId(branch != null ? branch.getId() : null)
                .departmentId(department != null ? department.getId() : null)
                .notes(buildNotes(source, branch, department))
                .build();
    }

    static CompanyEmployee pickActiveCompanyEmployee(List<CompanyEmployee> rows) {
        return rows.stream()
                .filter(row -> row.getCompany() != null)
                .sorted(Comparator.comparing(CompanyEmployee::isActive).reversed()
                        .thenComparing(CompanyEmployee::getJoinDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .findFirst()
                .orElse(null);
    }

    static EmployeeBranchDepartment pickActiveBranchDepartment(List<EmployeeBranchDepartment> rows) {
        return rows.stream()
                .sorted(Comparator.comparing(EmployeeBranchDepartment::isActive).reversed()
                        .thenComparing(EmployeeBranchDepartment::getStartDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .findFirst()
                .orElse(null);
    }

    static EmployeeBranch pickActiveBranch(List<EmployeeBranch> rows) {
        return rows.stream()
                .filter(row -> row.getBranch() != null)
                .sorted(Comparator.comparing(
                                (EmployeeBranch row) -> Boolean.TRUE.equals(row.getIsActive()),
                                Comparator.reverseOrder())
                        .thenComparing(EmployeeBranch::getStartDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .findFirst()
                .orElse(null);
    }

    static String departmentKey(Long departmentMysqlId, Long branchMysqlId) {
        return departmentMysqlId + ":" + branchMysqlId;
    }

    private static BranchEntity resolveBranch(
            EmployeeBranchDepartment branchDepartment,
            EmployeeBranch employeeBranch,
            CompanyEmployee companyEmployee,
            Map<Long, BranchEntity> branchByMysqlId,
            Map<Long, List<BranchEntity>> branchesByCompanyMysqlId) {
        if (branchDepartment != null && branchDepartment.getBranch() != null) {
            BranchEntity branch = branchByMysqlId.get(branchDepartment.getBranch().getId());
            if (branch != null) {
                return branch;
            }
        }
        if (employeeBranch != null && employeeBranch.getBranch() != null) {
            BranchEntity branch = branchByMysqlId.get(employeeBranch.getBranch().getId());
            if (branch != null) {
                return branch;
            }
        }
        return fallbackCompanyBranch(companyEmployee, branchesByCompanyMysqlId);
    }

    private static BranchEntity fallbackCompanyBranch(
            CompanyEmployee companyEmployee, Map<Long, List<BranchEntity>> branchesByCompanyMysqlId) {
        if (companyEmployee == null || companyEmployee.getCompany() == null) {
            return null;
        }
        List<BranchEntity> branches =
                branchesByCompanyMysqlId.getOrDefault(companyEmployee.getCompany().getId(), List.of());
        return branches.isEmpty() ? null : branches.getFirst();
    }

    private static DepartmentEntity resolveDepartment(
            EmployeeBranchDepartment branchDepartment,
            BranchEntity branch,
            Map<String, DepartmentEntity> departmentByKey) {
        if (branchDepartment == null
                || branchDepartment.getDepartment() == null
                || branch == null) {
            return null;
        }
        return departmentByKey.get(departmentKey(
                branchDepartment.getDepartment().getId(), branch.getMysqlId()));
    }

    private static String resolveEmployeeCode(
            Long employeeId, CompanyEmployee companyEmployee, Set<String> reservedEmployeeCodes) {
        String base;
        if (companyEmployee != null && companyEmployee.getOrganizationId() != null) {
            String orgId = companyEmployee.getOrganizationId().trim();
            base = orgId.isEmpty() ? "EMP-" + employeeId : orgId;
        } else {
            base = "EMP-" + employeeId;
        }
        String candidate = base;
        int suffix = 1;
        while (!reservedEmployeeCodes.add(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private static String resolveEmail(
            Long employeeId,
            String employeeCode,
            SecUser secUser,
            Set<String> reservedEmails) {
        if (secUser != null && secUser.getUsername() != null) {
            String username = secUser.getUsername().trim().toLowerCase(Locale.ROOT);
            if (username.contains("@") && reservedEmails.add(username)) {
                return username;
            }
        }
        String base = (employeeCode + "@migrated.local").toLowerCase(Locale.ROOT).replace(' ', '-');
        String candidate = base;
        int suffix = 1;
        while (!reservedEmails.add(candidate)) {
            candidate = base.replace("@", "+" + suffix + "@");
            suffix++;
        }
        return candidate;
    }

    private static LocalDate resolveHireDate(
            Employee source,
            CompanyEmployee companyEmployee,
            EmployeeBranchDepartment branchDepartment,
            EmployeeBranch employeeBranch) {
        if (source.getHireDate() != null) {
            return toLocalDate(source.getHireDate());
        }
        if (companyEmployee != null && companyEmployee.getJoinDate() != null) {
            return toLocalDate(companyEmployee.getJoinDate());
        }
        if (branchDepartment != null && branchDepartment.getStartDate() != null) {
            return toLocalDate(branchDepartment.getStartDate());
        }
        if (employeeBranch != null && employeeBranch.getStartDate() != null) {
            return toLocalDate(employeeBranch.getStartDate());
        }
        return null;
    }

    private static String buildNotes(Employee source, BranchEntity branch, DepartmentEntity department) {
        List<String> parts = new ArrayList<>();
        appendNote(parts, trimToNull(source.getSummary()));
        appendNote(parts, source.getGender() != null ? "gender=" + source.getGender().trim() : null);
        appendNote(parts, source.getStatus() != null ? "status=" + source.getStatus().trim() : null);
        appendNote(parts, trimToNull(source.getPhoto()) != null ? "photo=" + source.getPhoto().trim() : null);
        appendNote(parts, trimToNull(source.getNepaliName()) != null ? "nepaliName=" + source.getNepaliName().trim() : null);
        appendNote(parts, trimToNull(source.getCitizenNumber()) != null ? "citizenNumber=" + source.getCitizenNumber().trim() : null);
        appendNote(parts, trimToNull(source.getPassportNo()) != null ? "passportNo=" + source.getPassportNo().trim() : null);
        appendNote(parts, trimToNull(source.getJobType()) != null ? "jobType=" + source.getJobType().trim() : null);
        appendNote(parts, source.getTitle() != null ? "title=" + source.getTitle().name() : null);
        appendNote(parts, source.getMaritialStatus() != null ? "maritalStatus=" + source.getMaritialStatus().name() : null);
        if (branch == null) {
            appendNote(parts, "branchAssignment=fallback-or-unassigned");
        }
        if (department == null && branch != null) {
            appendNote(parts, "departmentAssignment=unassigned");
        }
        if (parts.isEmpty()) {
            return "Migrated from legacy employee id=" + source.getId();
        }
        return String.join("; ", parts);
    }

    private static void appendNote(List<String> parts, String value) {
        if (value != null && !value.isBlank()) {
            parts.add(value);
        }
    }

    private static String defaultName(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed != null ? trimmed : fallback;
    }

    private static String defaultPhone(String phone) {
        String trimmed = trimToNull(phone);
        return trimmed != null ? trimmed : "0000000000";
    }

    private static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
