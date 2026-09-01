package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyEmployee;
import com.jojolaptech.camel.model.mysql.EmployeeSecUser;
import com.jojolaptech.camel.model.mysql.SecUser;
import com.jojolaptech.camel.model.mysql.SecUserSecRole;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.user.BranchUserEntity;
import com.jojolaptech.camel.model.postgres.user.CompanyUserEntity;
import com.jojolaptech.camel.model.postgres.user.EmployeeUserEntity;
import com.jojolaptech.camel.model.postgres.user.UserEntity;
import com.jojolaptech.camel.model.postgres.user.enums.PermissionForEnum;
import com.jojolaptech.camel.model.postgres.user.enums.UserTypeEnum;
import com.jojolaptech.camel.processor.UserPortalLinkMapper.PortalKind;
import com.jojolaptech.camel.repository.mysql.CompanyEmployeeRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeSecUserRepository;
import com.jojolaptech.camel.repository.mysql.SecUserSecRoleRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.user.PgBranchUserRepository;
import com.jojolaptech.camel.repository.postgres.user.PgCompanyUserRepository;
import com.jojolaptech.camel.repository.postgres.user.PgEmployeeUserRepository;
import com.jojolaptech.camel.repository.postgres.user.PgUserRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPortalLinkProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(UserPortalLinkProcessor.class);

    private final PgUserRepository userRepository;
    private final PgEmployeeRepository employeeRepository;
    private final PgCompanyRepository companyRepository;
    private final PgBranchRepository branchRepository;
    private final EmployeeSecUserRepository employeeSecUserRepository;
    private final SecUserSecRoleRepository secUserSecRoleRepository;
    private final CompanyEmployeeRepository companyEmployeeRepository;
    private final PgEmployeeUserRepository employeeUserRepository;
    private final PgCompanyUserRepository companyUserRepository;
    private final PgBranchUserRepository branchUserRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<SecUser> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        List<Long> userIds = batch.stream().map(SecUser::getId).toList();
        Map<Long, UserEntity> usersByMysqlId = userRepository.findByMysqlIdIn(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getMysqlId, user -> user, (left, right) -> left));
        if (usersByMysqlId.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> linkedEmployeeUsers = employeeUserRepository.findLinkedUserMysqlIds(userIds);
        Set<Long> linkedCompanyUsers = companyUserRepository.findLinkedUserMysqlIds(userIds);
        Set<Long> linkedBranchUsers = branchUserRepository.findLinkedUserMysqlIds(userIds);

        Map<Long, EmployeeSecUser> employeeLinkByUserId = employeeSecUserRepository.findByUserIdInWithEmployee(userIds)
                .stream()
                .collect(Collectors.toMap(link -> link.getUser().getId(), link -> link, (left, right) -> left));
        Set<Long> employeeMysqlIds = employeeLinkByUserId.values().stream()
                .map(link -> link.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeesByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, employee -> employee, (left, right) -> left));

        Map<Long, CompanyEmployee> activeCompanyEmployeeByEmployeeMysqlId =
                companyEmployeeRepository.findByEmployeeIdIn(employeeMysqlIds).stream()
                        .collect(Collectors.groupingBy(row -> row.getEmployee().getId()))
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> EmployeeMigrationMapper.pickActiveCompanyEmployee(entry.getValue())));

        Set<Long> companyMysqlIds = activeCompanyEmployeeByEmployeeMysqlId.values().stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companiesByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, company -> company, (left, right) -> left));
        Map<Long, List<BranchEntity>> branchesByCompanyMysqlId =
                branchRepository.findByCompanyMysqlIdIn(companyMysqlIds).stream()
                        .collect(Collectors.groupingBy(branch -> branch.getCompany().getMysqlId()));
        Map<UUID, BranchEntity> branchesByUuid = branchesByCompanyMysqlId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(BranchEntity::getId, branch -> branch, (left, right) -> left));

        Map<Long, List<SecUserSecRole>> rolesByUserId = secUserSecRoleRepository.findBySecUserIdIn(userIds).stream()
                .collect(Collectors.groupingBy(link -> link.getSecUser().getId()));

        Set<String> lookupEmails = new HashSet<>();
        for (SecUser source : batch) {
            UserEntity user = usersByMysqlId.get(source.getId());
            if (user != null) {
                addEmail(lookupEmails, user.getEmailAddress());
            }
            addEmail(lookupEmails, source.getUsername());
        }
        Map<String, CompanyEntity> companyByEmail = companyRepository.findByEmailIgnoreCaseIn(lookupEmails).stream()
                .collect(Collectors.toMap(
                        company -> UserPortalLinkMapper.normalizeEmail(company.getEmail()),
                        company -> company,
                        (left, right) -> left));

        List<EmployeeUserEntity> employeeUsers = new ArrayList<>();
        List<CompanyUserEntity> companyUsers = new ArrayList<>();
        List<BranchUserEntity> branchUsers = new ArrayList<>();
        int imported = 0;

        for (SecUser source : batch) {
            UserEntity user = usersByMysqlId.get(source.getId());
            if (user == null) {
                continue;
            }

            List<String> authorities = rolesByUserId.getOrDefault(source.getId(), List.of()).stream()
                    .map(link -> link.getSecRole().getAuthority())
                    .toList();
            EmployeeSecUser employeeLink = employeeLinkByUserId.get(source.getId());
            EmployeeEntity employee = employeeLink == null
                    ? null
                    : employeesByMysqlId.get(employeeLink.getEmployee().getId());
            UserTypeEnum userType = HrmAuthorityMapper.resolvePrimaryUserType(authorities, employeeLink != null);
            PermissionForEnum roleScope = HrmAuthorityMapper.roleScope(firstAuthority(authorities));
            PortalKind portalKind = UserPortalLinkMapper.resolvePortalKind(
                    userType, roleScope, employeeLink != null, employee != null);

            switch (portalKind) {
                case EMPLOYEE -> {
                    if (linkedEmployeeUsers.contains(source.getId())) {
                        continue;
                    }
                    if (employee == null) {
                        log.warn(
                                "Skipping employee portal link for secUser id={}, employee mysqlId={} not migrated",
                                source.getId(),
                                employeeLink.getEmployee().getId());
                        continue;
                    }
                    UUID companyId = UserPortalLinkMapper.resolveCompanyId(
                            employee, activeCompanyEmployeeByEmployeeMysqlId, companiesByMysqlId);
                    UUID branchId = UserPortalLinkMapper.resolveBranchId(
                            employee,
                            activeCompanyEmployeeByEmployeeMysqlId,
                            companiesByMysqlId,
                            branchesByCompanyMysqlId);
                    employeeUsers.add(EmployeeUserEntity.builder()
                            .user(user)
                            .employeeId(employee.getId())
                            .companyId(companyId)
                            .branchId(branchId)
                            .build());
                    linkedEmployeeUsers.add(source.getId());
                    imported++;
                }
                case COMPANY -> {
                    if (linkedCompanyUsers.contains(source.getId())) {
                        continue;
                    }
                    CompanyEntity company = resolveCompany(user, source, employeeLink, companyByEmail, activeCompanyEmployeeByEmployeeMysqlId, companiesByMysqlId);
                    if (company == null) {
                        log.warn("Skipping company portal link for secUser id={}, company not resolved", source.getId());
                        continue;
                    }
                    companyUsers.add(CompanyUserEntity.builder()
                            .user(user)
                            .companyId(company.getId())
                            .build());
                    linkedCompanyUsers.add(source.getId());
                    imported++;
                }
                case BRANCH -> {
                    if (linkedBranchUsers.contains(source.getId())) {
                        continue;
                    }
                    UUID branchId = UserPortalLinkMapper.resolveBranchId(
                            employee,
                            activeCompanyEmployeeByEmployeeMysqlId,
                            companiesByMysqlId,
                            branchesByCompanyMysqlId);
                    if (branchId == null) {
                        log.warn("Skipping branch portal link for secUser id={}, branch not resolved", source.getId());
                        continue;
                    }
                    UUID companyId = UserPortalLinkMapper.resolveBranchCompanyId(branchId, branchesByUuid);
                    if (companyId == null) {
                        companyId = UserPortalLinkMapper.resolveCompanyId(
                                employee, activeCompanyEmployeeByEmployeeMysqlId, companiesByMysqlId);
                    }
                    branchUsers.add(BranchUserEntity.builder()
                            .user(user)
                            .branchId(branchId)
                            .companyId(companyId)
                            .build());
                    linkedBranchUsers.add(source.getId());
                    imported++;
                }
                case NONE -> log.debug("No portal link for secUser id={}, userType={}", source.getId(), userType);
            }
        }

        if (!employeeUsers.isEmpty()) {
            employeeUserRepository.saveAll(employeeUsers);
        }
        if (!companyUsers.isEmpty()) {
            companyUserRepository.saveAll(companyUsers);
        }
        if (!branchUsers.isEmpty()) {
            branchUserRepository.saveAll(branchUsers);
        }
        exchange.setProperty("batchImported", imported);
    }

    private static CompanyEntity resolveCompany(
            UserEntity user,
            SecUser source,
            EmployeeSecUser employeeLink,
            Map<String, CompanyEntity> companyByEmail,
            Map<Long, CompanyEmployee> activeCompanyEmployeeByEmployeeMysqlId,
            Map<Long, CompanyEntity> companiesByMysqlId) {
        CompanyEntity company = UserPortalLinkMapper.resolveCompanyByEmail(user.getEmailAddress(), companyByEmail);
        if (company != null) {
            return company;
        }
        company = UserPortalLinkMapper.resolveCompanyByEmail(source.getUsername(), companyByEmail);
        if (company != null) {
            return company;
        }
        if (employeeLink != null) {
            return UserPortalLinkMapper.resolveCompanyFromEmployee(
                    employeeLink.getEmployee().getId(),
                    activeCompanyEmployeeByEmployeeMysqlId,
                    companiesByMysqlId);
        }
        return null;
    }

    private static void addEmail(Set<String> emails, String value) {
        String normalized = UserPortalLinkMapper.normalizeEmail(value);
        if (normalized != null) {
            emails.add(normalized);
        }
    }

    private static String firstAuthority(List<String> authorities) {
        return authorities.isEmpty() ? null : authorities.getFirst();
    }
}
