package com.jojolaptech.camel.processor;

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
import com.jojolaptech.camel.model.postgres.user.enums.UserTypeEnum;
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
import java.util.Locale;
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

        Map<Long, Long> companyMysqlIdByEmployeeId = companyEmployeeRepository.findByEmployeeIdIn(employeeMysqlIds)
                .stream()
                .filter(row -> row.getCompany() != null)
                .collect(Collectors.toMap(
                        row -> row.getEmployee().getId(),
                        row -> row.getCompany().getId(),
                        (left, right) -> left));
        Set<Long> companyMysqlIds = new HashSet<>(companyMysqlIdByEmployeeId.values());
        Map<Long, CompanyEntity> companiesByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, company -> company, (left, right) -> left));
        Map<UUID, BranchEntity> branchesByUuid = branchRepository.findByCompanyMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(BranchEntity::getId, branch -> branch, (left, right) -> left));

        Map<Long, List<SecUserSecRole>> rolesByUserId = secUserSecRoleRepository.findBySecUserIdIn(userIds).stream()
                .collect(Collectors.groupingBy(link -> link.getSecUser().getId()));

        Map<String, CompanyEntity> companyByEmail = companyRepository.findAll().stream()
                .filter(company -> company.getEmail() != null && !company.getEmail().isBlank())
                .collect(Collectors.toMap(
                        company -> company.getEmail().trim().toLowerCase(Locale.ROOT),
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

            EmployeeSecUser employeeLink = employeeLinkByUserId.get(source.getId());
            if (employeeLink != null && !linkedEmployeeUsers.contains(source.getId())) {
                EmployeeEntity employee = employeesByMysqlId.get(employeeLink.getEmployee().getId());
                if (employee != null) {
                    UUID companyId = resolveCompanyId(employee, companyMysqlIdByEmployeeId, companiesByMysqlId);
                    employeeUsers.add(EmployeeUserEntity.builder()
                            .user(user)
                            .employeeId(employee.getId())
                            .companyId(companyId)
                            .branchId(employee.getBranchId())
                            .build());
                    linkedEmployeeUsers.add(source.getId());
                    imported++;
                    continue;
                }
            }

            List<String> authorities = rolesByUserId.getOrDefault(source.getId(), List.of()).stream()
                    .map(link -> link.getSecRole().getAuthority())
                    .toList();
            UserTypeEnum userType = HrmAuthorityMapper.resolvePrimaryUserType(authorities, employeeLink != null);

            if (userType == UserTypeEnum.COMPANY_ADMIN && !linkedCompanyUsers.contains(source.getId())) {
                CompanyEntity company = resolveCompanyByEmail(source.getUsername(), companyByEmail);
                if (company != null) {
                    companyUsers.add(CompanyUserEntity.builder()
                            .user(user)
                            .companyId(company.getId())
                            .build());
                    linkedCompanyUsers.add(source.getId());
                    imported++;
                    continue;
                }
            }

            if (userType == UserTypeEnum.EMPLOYEE && employeeLink != null) {
                continue;
            }

            if (HrmAuthorityMapper.roleScope(firstAuthority(authorities)) == com.jojolaptech.camel.model.postgres.user.enums.PermissionForEnum.BRANCH
                    && !linkedBranchUsers.contains(source.getId())
                    && employeeLink != null) {
                EmployeeEntity employee = employeesByMysqlId.get(employeeLink.getEmployee().getId());
                if (employee != null && employee.getBranchId() != null) {
                    BranchEntity branch = branchesByUuid.get(employee.getBranchId());
                    UUID companyId = branch != null && branch.getCompany() != null
                            ? branch.getCompany().getId()
                            : resolveCompanyId(employee, companyMysqlIdByEmployeeId, companiesByMysqlId);
                    branchUsers.add(BranchUserEntity.builder()
                            .user(user)
                            .branchId(employee.getBranchId())
                            .companyId(companyId)
                            .build());
                    linkedBranchUsers.add(source.getId());
                    imported++;
                }
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

    private static UUID resolveCompanyId(
            EmployeeEntity employee,
            Map<Long, Long> companyMysqlIdByEmployeeId,
            Map<Long, CompanyEntity> companiesByMysqlId) {
        Long companyMysqlId = companyMysqlIdByEmployeeId.get(employee.getMysqlId());
        if (companyMysqlId == null) {
            return null;
        }
        CompanyEntity company = companiesByMysqlId.get(companyMysqlId);
        return company != null ? company.getId() : null;
    }

    private static CompanyEntity resolveCompanyByEmail(
            String username, Map<String, CompanyEntity> companyByEmail) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return companyByEmail.get(username.trim().toLowerCase(Locale.ROOT));
    }

    private static String firstAuthority(List<String> authorities) {
        return authorities.isEmpty() ? null : authorities.getFirst();
    }
}
