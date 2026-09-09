package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Employee;
import com.jojolaptech.camel.model.mysql.EmployeeSecUser;
import com.jojolaptech.camel.model.mysql.SecUser;
import com.jojolaptech.camel.model.mysql.SecUserSecRole;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import com.jojolaptech.camel.model.postgres.user.RoleEntity;
import com.jojolaptech.camel.model.postgres.user.UserEntity;
import com.jojolaptech.camel.model.postgres.user.enums.AuthProviderEnum;
import com.jojolaptech.camel.model.postgres.user.enums.UserStatusEnum;
import com.jojolaptech.camel.model.postgres.user.enums.UserTypeEnum;
import com.jojolaptech.camel.repository.mysql.EmployeeSecUserRepository;
import com.jojolaptech.camel.repository.mysql.SecUserSecRoleRepository;
import com.jojolaptech.camel.repository.postgres.user.PgRoleRepository;
import com.jojolaptech.camel.repository.postgres.user.PgUserRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(UserProcessor.class);

    private final PgUserRepository userRepository;
    private final PgRoleRepository roleRepository;
    private final SecUserSecRoleRepository secUserSecRoleRepository;
    private final EmployeeSecUserRepository employeeSecUserRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<SecUser> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        List<Long> userIds = batch.stream().map(SecUser::getId).toList();
        Map<Long, UserEntity> existingByMysqlId = userRepository.findByMysqlIdIn(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getMysqlId, user -> user, (a, b) -> a));
        // Trim before lookup — legacy usernames often have leading/trailing spaces that
        // collide with already-migrated emails after trim on insert.
        Set<String> emails = batch.stream()
                .map(SecUser::getUsername)
                .map(UserProcessor::trimToNull)
                .filter(username -> username != null)
                .map(username -> username.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        Set<String> existingEmails = emails.isEmpty()
                ? Set.of()
                : userRepository.findExistingEmailsIgnoreCase(emails);

        Map<Long, List<SecUserSecRole>> rolesByUserId = secUserSecRoleRepository.findBySecUserIdIn(userIds)
                .stream()
                .collect(Collectors.groupingBy(link -> link.getSecUser().getId()));

        Set<Long> roleMysqlIds = rolesByUserId.values().stream()
                .flatMap(List::stream)
                .map(link -> link.getSecRole().getId())
                .collect(Collectors.toSet());
        Map<Long, RoleEntity> rolesByMysqlId = roleMysqlIds.isEmpty()
                ? Map.of()
                : roleRepository.findByMysqlIdIn(roleMysqlIds).stream()
                        .collect(Collectors.toMap(RoleEntity::getMysqlId, role -> role));

        Set<Long> employeeUserIds = employeeSecUserRepository.findEmployeeUserIds(userIds);
        Map<Long, Employee> employeeByUserId = employeeSecUserRepository.findByUserIdInWithEmployee(userIds).stream()
                .collect(Collectors.toMap(
                        link -> link.getUser().getId(),
                        EmployeeSecUser::getEmployee,
                        (left, right) -> left));

        List<UserEntity> toSave = new ArrayList<>();
        Set<String> emailsInBatch = new HashSet<>();
        int updated = 0;
        for (SecUser source : batch) {
            List<SecUserSecRole> links = rolesByUserId.getOrDefault(source.getId(), List.of());
            List<RoleEntity> roles = new ArrayList<>();
            List<String> authorities = new ArrayList<>();
            for (SecUserSecRole link : links) {
                authorities.add(link.getSecRole().getAuthority());
                RoleEntity role = rolesByMysqlId.get(link.getSecRole().getId());
                if (role != null) {
                    roles.add(role);
                }
            }
            boolean employeeLinked = employeeUserIds.contains(source.getId());
            List<UserTypeEnum> userTypes = HrmAuthorityMapper.userTypes(authorities, employeeLinked);

            UserEntity existing = existingByMysqlId.get(source.getId());
            if (existing != null) {
                boolean changed = false;
                if (!Objects.equals(existing.getUserType(), userTypes)) {
                    existing.setUserType(userTypes);
                    changed = true;
                }
                Set<Long> existingRoleMysqlIds = existing.getRoles() == null
                        ? Set.of()
                        : existing.getRoles().stream()
                                .map(RoleEntity::getMysqlId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());
                Set<Long> desiredRoleMysqlIds = roles.stream()
                        .map(RoleEntity::getMysqlId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                if (!existingRoleMysqlIds.equals(desiredRoleMysqlIds)) {
                    // Mutate managed collection so Hibernate updates user_role join rows.
                    if (existing.getRoles() == null) {
                        existing.setRoles(new ArrayList<>());
                    } else {
                        existing.getRoles().clear();
                    }
                    existing.getRoles().addAll(roles);
                    changed = true;
                }
                if (changed) {
                    toSave.add(existing);
                    updated++;
                }
                continue;
            }

            String email = source.getUsername() == null ? null : source.getUsername().trim();
            if (email == null || email.isBlank()) {
                log.warn("Skipping secUser id={}, username is blank", source.getId());
                continue;
            }
            String emailKey = email.toLowerCase(Locale.ROOT);
            if (existingEmails.contains(emailKey) || !emailsInBatch.add(emailKey)) {
                log.info("Skipping secUser id={}, email already exists", source.getId());
                continue;
            }

            UserStatusEnum userStatus = resolveUserStatus(source);
            Employee employee = employeeByUserId.get(source.getId());
            String mobileNumber = employee == null ? null : trimToNull(employee.getPhone());
            UserEntity user = UserEntity.builder()
                    .mysqlId(source.getId())
                    .emailAddress(email)
                    .mobileNumber(mobileNumber)
                    .password(source.getPassword())
                    .accountNonExpired(!source.isAccountExpired())
                    .accountNonLocked(!source.isAccountLocked())
                    .credentialsNonExpired(!source.isPasswordExpired())
                    .enabled(source.isEnabled())
                    .userStatus(userStatus)
                    .roles(roles)
                    .userType(userTypes)
                    .authProvider(AuthProviderEnum.DEFAULT)
                    .build();
            user.setStatus(source.isEnabled() ? StatusEnum.ACTIVE : StatusEnum.INACTIVE);
            toSave.add(user);
        }

        if (!toSave.isEmpty()) {
            userRepository.saveAll(toSave);
            userRepository.flush();
        }

        int inserted = toSave.size() - updated;
        log.info(
                "User batch imported {} new / updated {} of {} secUser rows",
                Math.max(inserted, 0),
                updated,
                batch.size());
        exchange.setProperty("batchImported", toSave.size());
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static UserStatusEnum resolveUserStatus(SecUser source) {
        if (source.isAccountLocked()) {
            return UserStatusEnum.LOCKED;
        }
        if (!source.isEnabled()) {
            return UserStatusEnum.INACTIVE;
        }
        return UserStatusEnum.ACTIVE;
    }
}
