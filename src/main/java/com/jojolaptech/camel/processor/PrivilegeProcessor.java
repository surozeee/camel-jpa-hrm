package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Requestmap;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import com.jojolaptech.camel.model.postgres.user.PermissionEntity;
import com.jojolaptech.camel.model.postgres.user.enums.PermissionForEnum;
import com.jojolaptech.camel.repository.postgres.user.PgPermissionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PrivilegeProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(PrivilegeProcessor.class);

    private final PgPermissionRepository permissionRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Requestmap> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> existing = permissionRepository.findMysqlIdsByMysqlIdIn(
                batch.stream().map(Requestmap::getId).toList());
        List<String> codes = batch.stream()
                .map(source -> HrmAuthorityMapper.permissionCode(source.getUrl(), source.getId()))
                .toList();
        Set<String> existingCodes = permissionRepository.findExistingCodes(codes);

        List<PermissionEntity> toSave = new ArrayList<>();
        for (Requestmap source : batch) {
            if (existing.contains(source.getId())) {
                continue;
            }
            String code = HrmAuthorityMapper.permissionCode(source.getUrl(), source.getId());
            if (existingCodes.contains(code)) {
                log.info("Skipping requestmap id={}, code already exists", source.getId());
                continue;
            }
            PermissionEntity permission = PermissionEntity.builder()
                    .mysqlId(source.getId())
                    .name(source.getUrl())
                    .code(code)
                    .description(source.getConfigAttribute())
                    .status(StatusEnum.ACTIVE)
                    .permissionFor(List.of(PermissionForEnum.SYSTEM, PermissionForEnum.COMPANY))
                    .build();
            toSave.add(permission);
        }

        if (!toSave.isEmpty()) {
            permissionRepository.saveAll(toSave);
            permissionRepository.flush();
        }

        log.info("Privilege batch imported {} of {} requestmap rows", toSave.size(), batch.size());
        exchange.setProperty("batchImported", toSave.size());
    }
}
