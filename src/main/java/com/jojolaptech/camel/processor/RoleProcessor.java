package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.SecRole;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import com.jojolaptech.camel.model.postgres.user.RoleEntity;
import com.jojolaptech.camel.model.postgres.user.enums.PermissionForEnum;
import com.jojolaptech.camel.repository.postgres.user.PgRoleRepository;
import java.util.ArrayList;
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
public class RoleProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(RoleProcessor.class);

    private final PgRoleRepository roleRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<SecRole> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Map<Long, RoleEntity> byMysqlId = roleRepository
                .findByMysqlIdIn(batch.stream().map(SecRole::getId).toList())
                .stream()
                .collect(Collectors.toMap(RoleEntity::getMysqlId, role -> role, (a, b) -> a));

        Set<String> targetNames = batch.stream()
                .map(role -> HrmAuthorityMapper.roleName(role.getAuthority()).toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        Set<String> existingNames = targetNames.isEmpty()
                ? Set.of()
                : roleRepository.findExistingNamesIgnoreCase(targetNames);

        List<RoleEntity> toSave = new ArrayList<>();
        int updated = 0;
        for (SecRole source : batch) {
            String roleName = HrmAuthorityMapper.roleName(source.getAuthority());
            PermissionForEnum scope = HrmAuthorityMapper.roleScope(source.getAuthority());
            RoleEntity existing = byMysqlId.get(source.getId());
            if (existing != null) {
                boolean changed = false;
                if (!roleName.equals(existing.getName())) {
                    String nameKey = roleName.toLowerCase(Locale.ROOT);
                    boolean nameTakenByOther = existingNames.contains(nameKey)
                            && (existing.getName() == null
                                    || !existing.getName().equalsIgnoreCase(roleName));
                    if (nameTakenByOther) {
                        log.warn(
                                "Cannot rename secRole id={} to '{}': name already used by another role",
                                source.getId(),
                                roleName);
                    } else {
                        existing.setName(roleName);
                        changed = true;
                    }
                }
                if (existing.getScope() != scope) {
                    existing.setScope(scope);
                    changed = true;
                }
                if (changed) {
                    toSave.add(existing);
                    updated++;
                }
                continue;
            }

            if (existingNames.contains(roleName.toLowerCase(Locale.ROOT))) {
                log.info("Skipping secRole id={}, name already exists: {}", source.getId(), roleName);
                continue;
            }

            RoleEntity role = RoleEntity.builder()
                    .mysqlId(source.getId())
                    .name(roleName)
                    .description("Migrated from MySQL " + source.getAuthority())
                    .status(StatusEnum.ACTIVE)
                    .scope(scope)
                    .build();
            toSave.add(role);
            existingNames = new java.util.HashSet<>(existingNames);
            existingNames.add(roleName.toLowerCase(Locale.ROOT));
        }

        if (!toSave.isEmpty()) {
            roleRepository.saveAll(toSave);
            roleRepository.flush();
        }

        int inserted = toSave.size() - updated;
        log.info(
                "Role batch imported {} new / updated {} of {} secRole rows",
                Math.max(inserted, 0),
                updated,
                batch.size());
        exchange.setProperty("batchImported", toSave.size());
    }
}
