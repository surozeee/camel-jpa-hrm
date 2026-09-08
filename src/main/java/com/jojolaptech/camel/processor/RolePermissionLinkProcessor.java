package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Requestmap;
import com.jojolaptech.camel.model.postgres.user.PermissionEntity;
import com.jojolaptech.camel.model.postgres.user.RoleEntity;
import com.jojolaptech.camel.repository.postgres.user.PgPermissionRepository;
import com.jojolaptech.camel.repository.postgres.user.PgRoleRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Links migrated {@code permission} rows to {@code role} via legacy {@code requestmap.config_attribute}
 * (comma-separated ROLE_* authorities). Populates {@code role_permission}.
 */
@Component
public class RolePermissionLinkProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(RolePermissionLinkProcessor.class);

    private final PgPermissionRepository permissionRepository;
    private final PgRoleRepository roleRepository;
    private final JdbcTemplate postgresJdbc;

    public RolePermissionLinkProcessor(
            PgPermissionRepository permissionRepository,
            PgRoleRepository roleRepository,
            @Qualifier("postgresJdbcTemplate") JdbcTemplate postgresJdbc) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.postgresJdbc = postgresJdbc;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Requestmap> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Map<Long, UUID> permissionIdByMysqlId = permissionRepository
                .findByMysqlIdIn(batch.stream().map(Requestmap::getId).toList())
                .stream()
                .collect(Collectors.toMap(PermissionEntity::getMysqlId, PermissionEntity::getId, (a, b) -> a));

        Map<String, UUID> roleIdByLookup = new java.util.HashMap<>();
        for (RoleEntity role : roleRepository.findAll()) {
            if (role.getName() == null || role.getName().isBlank()) {
                continue;
            }
            roleIdByLookup.put(role.getName().toLowerCase(Locale.ROOT), role.getId());
        }

        Set<String> existingPairs = loadExistingPairs(permissionIdByMysqlId.values(), roleIdByLookup.values());

        int linked = 0;
        int skippedMissing = 0;
        Set<String> insertedInBatch = new HashSet<>();
        for (Requestmap source : batch) {
            UUID permissionId = permissionIdByMysqlId.get(source.getId());
            if (permissionId == null) {
                skippedMissing++;
                continue;
            }
            List<String> authorities = HrmAuthorityMapper.splitConfigAttributes(source.getConfigAttribute());
            if (authorities.isEmpty()) {
                continue;
            }
            for (String authority : authorities) {
                UUID roleId = resolveRoleId(roleIdByLookup, authority);
                if (roleId == null) {
                    skippedMissing++;
                    continue;
                }
                String pairKey = roleId + "|" + permissionId;
                if (existingPairs.contains(pairKey) || !insertedInBatch.add(pairKey)) {
                    continue;
                }
                postgresJdbc.update(
                        "INSERT INTO role_permission (role_id, permission_id) VALUES (?, ?)",
                        roleId,
                        permissionId);
                linked++;
            }
        }

        log.info(
                "Role-permission batch linked {} pairs (requestmap rows={}, missing role/perm skips={})",
                linked,
                batch.size(),
                skippedMissing);
        exchange.setProperty("batchImported", linked);
    }

    private static UUID resolveRoleId(Map<String, UUID> roleIdByLookup, String authority) {
        UUID byErpName = roleIdByLookup.get(HrmAuthorityMapper.roleName(authority).toLowerCase(Locale.ROOT));
        if (byErpName != null) {
            return byErpName;
        }
        // Tolerate already-migrated rows that still use bare legacy codes (ADMIN, COMPANY, …).
        return roleIdByLookup.get(HrmAuthorityMapper.roleCode(authority).toLowerCase(Locale.ROOT));
    }

    private Set<String> loadExistingPairs(Iterable<UUID> permissionIds, Iterable<UUID> roleIds) {
        List<UUID> permList = new ArrayList<>();
        permissionIds.forEach(permList::add);
        List<UUID> roleList = new ArrayList<>();
        roleIds.forEach(roleList::add);
        if (permList.isEmpty() || roleList.isEmpty()) {
            return Set.of();
        }
        String permPlaceholders = permList.stream().map(id -> "?").collect(Collectors.joining(","));
        String rolePlaceholders = roleList.stream().map(id -> "?").collect(Collectors.joining(","));
        Object[] args = new Object[roleList.size() + permList.size()];
        int i = 0;
        for (UUID roleId : roleList) {
            args[i++] = roleId;
        }
        for (UUID permId : permList) {
            args[i++] = permId;
        }
        return new HashSet<>(postgresJdbc.query(
                "SELECT role_id, permission_id FROM role_permission WHERE role_id IN ("
                        + rolePlaceholders
                        + ") AND permission_id IN ("
                        + permPlaceholders
                        + ")",
                (rs, rowNum) -> rs.getObject("role_id", UUID.class) + "|" + rs.getObject("permission_id", UUID.class),
                args));
    }
}
