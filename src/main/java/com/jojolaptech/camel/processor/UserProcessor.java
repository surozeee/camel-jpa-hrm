package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.SecUser;
import com.jojolaptech.camel.model.mysql.SecUserSecRole;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import com.jojolaptech.camel.model.postgres.user.RoleEntity;
import com.jojolaptech.camel.model.postgres.user.UserEntity;
import com.jojolaptech.camel.model.postgres.user.enums.AuthProviderEnum;
import com.jojolaptech.camel.model.postgres.user.enums.UserStatusEnum;
import com.jojolaptech.camel.repository.mysql.EmployeeSecUserRepository;
import com.jojolaptech.camel.repository.mysql.SecUserSecRoleRepository;
import com.jojolaptech.camel.repository.postgres.user.PgRoleRepository;
import com.jojolaptech.camel.repository.postgres.user.PgUserRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        Set<Long> existingIds = userRepository.findMysqlIdsByMysqlIdIn(userIds);
        Set<String> emails = batch.stream()
                .map(SecUser::getUsername)
                .filter(username -> username != null && !username.isBlank())
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

        List<UserEntity> toSave = new ArrayList<>();
        Set<String> emailsInBatch = new HashSet<>();
        for (SecUser source : batch) {
            if (existingIds.contains(source.getId())) {
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

            UserStatusEnum userStatus = resolveUserStatus(source);
            UserEntity user = UserEntity.builder()
                    .mysqlId(source.getId())
                    .emailAddress(email)
                    .password(source.getPassword())
                    .accountNonExpired(!source.isAccountExpired())
                    .accountNonLocked(!source.isAccountLocked())
                    .credentialsNonExpired(!source.isPasswordExpired())
                    .enabled(source.isEnabled())
                    .userStatus(userStatus)
                    .roles(roles)
                    .userType(HrmAuthorityMapper.userTypes(authorities, employeeUserIds.contains(source.getId())))
                    .authProvider(AuthProviderEnum.DEFAULT)
                    .build();
            user.setStatus(source.isEnabled() ? StatusEnum.ACTIVE : StatusEnum.INACTIVE);
            toSave.add(user);
        }

        if (!toSave.isEmpty()) {
            userRepository.saveAll(toSave);
            userRepository.flush();
        }

        log.info("User batch imported {} of {} secUser rows", toSave.size(), batch.size());
        exchange.setProperty("batchImported", toSave.size());
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
