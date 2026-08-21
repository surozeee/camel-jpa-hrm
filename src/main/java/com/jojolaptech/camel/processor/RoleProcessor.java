package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Requestmap;
import com.jojolaptech.camel.model.mysql.SecRole;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import com.jojolaptech.camel.model.postgres.user.PermissionEntity;
import com.jojolaptech.camel.model.postgres.user.RoleEntity;
import com.jojolaptech.camel.repository.mysql.RequestmapRepository;
import com.jojolaptech.camel.repository.postgres.user.PgPermissionRepository;
import com.jojolaptech.camel.repository.postgres.user.PgRoleRepository;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final PgPermissionRepository permissionRepository;
    private final RequestmapRepository requestmapRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<SecRole> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> existing = roleRepository.findMysqlIdsByMysqlIdIn(
                batch.stream().map(SecRole::getId).toList());
        Set<String> names = batch.stream()
                .map(role -> HrmAuthorityMapper.roleName(role.getAuthority()).toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        Set<String> existingNames = names.isEmpty()
                ? Set.of()
                : roleRepository.findExistingNamesIgnoreCase(names);

        Map<String, List<Long>> authorityToRequestmapIds = loadAuthorityToRequestmapIds();
        Set<Long> permissionMysqlIds = authorityToRequestmapIds.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        Map<Long, PermissionEntity> permissionsByMysqlId = permissionMysqlIds.isEmpty()
                ? Map.of()
                : permissionRepository.findByMysqlIdIn(permissionMysqlIds)
                        .stream()
                        .collect(Collectors.toMap(PermissionEntity::getMysqlId, permission -> permission));

        List<RoleEntity> toSave = new ArrayList<>();
        for (SecRole source : batch) {
            if (existing.contains(source.getId())) {
                continue;
            }
            String roleName = HrmAuthorityMapper.roleName(source.getAuthority());
            if (existingNames.contains(roleName.toLowerCase(Locale.ROOT))) {
                log.info("Skipping secRole id={}, name already exists", source.getId());
                continue;
            }

            List<PermissionEntity> permissions = new ArrayList<>();
            for (Long requestmapId : authorityToRequestmapIds.getOrDefault(
                    source.getAuthority() == null ? "" : source.getAuthority().toUpperCase(Locale.ROOT),
                    List.of())) {
                PermissionEntity permission = permissionsByMysqlId.get(requestmapId);
                if (permission != null) {
                    permissions.add(permission);
                }
            }

            RoleEntity role = RoleEntity.builder()
                    .mysqlId(source.getId())
                    .name(roleName)
                    .description("Migrated from MySQL " + source.getAuthority())
                    .status(StatusEnum.ACTIVE)
                    .scope(HrmAuthorityMapper.roleScope(source.getAuthority()))
                    .permissions(permissions)
                    .build();
            toSave.add(role);
        }

        if (!toSave.isEmpty()) {
            roleRepository.saveAll(toSave);
            roleRepository.flush();
        }

        log.info("Role batch imported {} of {} secRole rows", toSave.size(), batch.size());
        exchange.setProperty("batchImported", toSave.size());
    }

    private Map<String, List<Long>> loadAuthorityToRequestmapIds() {
        Map<String, List<Long>> mapping = new HashMap<>();
        for (Requestmap requestmap : requestmapRepository.findAll()) {
            for (String authority : HrmAuthorityMapper.splitConfigAttributes(requestmap.getConfigAttribute())) {
                mapping.computeIfAbsent(authority.toUpperCase(Locale.ROOT), key -> new ArrayList<>())
                        .add(requestmap.getId());
            }
        }
        return mapping;
    }
}
