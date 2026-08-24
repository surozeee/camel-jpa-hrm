package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.SecRole;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import com.jojolaptech.camel.model.postgres.user.RoleEntity;
import com.jojolaptech.camel.repository.postgres.user.PgRoleRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

        Set<Long> existing = roleRepository.findMysqlIdsByMysqlIdIn(
                batch.stream().map(SecRole::getId).toList());
        Set<String> names = batch.stream()
                .map(role -> HrmAuthorityMapper.roleName(role.getAuthority()).toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        Set<String> existingNames = names.isEmpty()
                ? Set.of()
                : roleRepository.findExistingNamesIgnoreCase(names);

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

            RoleEntity role = RoleEntity.builder()
                    .mysqlId(source.getId())
                    .name(roleName)
                    .description("Migrated from MySQL " + source.getAuthority())
                    .status(StatusEnum.ACTIVE)
                    .scope(HrmAuthorityMapper.roleScope(source.getAuthority()))
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
}
