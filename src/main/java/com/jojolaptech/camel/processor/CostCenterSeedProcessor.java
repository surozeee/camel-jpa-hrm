package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.CostCenterEntity;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCostCenterRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Step 21b: one CostCenter per migrated branch (mysqlId = branch.mysqlId).
 */
@Component
@RequiredArgsConstructor
public class CostCenterSeedProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(CostCenterSeedProcessor.class);

    private final PgBranchRepository branchRepository;
    private final PgCostCenterRepository costCenterRepository;

    @Override
    public void process(Exchange exchange) {
        List<BranchEntity> branches = branchRepository.findAll().stream()
                .filter(b -> b.getMysqlId() != null)
                .toList();
        if (branches.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        // Ensure company is available for companyId
        Set<Long> mysqlIds = branches.stream().map(BranchEntity::getMysqlId).collect(Collectors.toSet());
        Map<Long, CostCenterEntity> existingByMysqlId = costCenterRepository.findByMysqlIdIn(mysqlIds).stream()
                .collect(Collectors.toMap(CostCenterEntity::getMysqlId, c -> c, (a, b) -> a));

        List<BranchEntity> withCompany = branchRepository.findByMysqlIdIn(mysqlIds);
        Map<Long, BranchEntity> branchByMysqlId = withCompany.stream()
                .collect(Collectors.toMap(BranchEntity::getMysqlId, b -> b, (a, b) -> a));

        List<CostCenterEntity> toSave = new ArrayList<>();
        for (BranchEntity branch : branches) {
            BranchEntity resolved = branchByMysqlId.getOrDefault(branch.getMysqlId(), branch);
            if (resolved.getCompany() == null) {
                log.warn("Skipping cost center for branch mysqlId={}, missing company", branch.getMysqlId());
                continue;
            }
            long mysqlId = resolved.getMysqlId();
            String name = resolved.getName() != null && !resolved.getName().isBlank()
                    ? resolved.getName() + " Cost Center"
                    : "Cost Center " + mysqlId;
            CostCenterEntity entity = existingByMysqlId.get(mysqlId);
            if (entity == null) {
                entity = CostCenterEntity.builder()
                        .mysqlId(mysqlId)
                        .code(truncate("CC-" + mysqlId, 64))
                        .name(name)
                        .description("Migrated default cost center for branch")
                        .companyId(resolved.getCompany().getId())
                        .branchId(resolved.getId())
                        .build();
                existingByMysqlId.put(mysqlId, entity);
            } else {
                entity.setCode(truncate("CC-" + mysqlId, 64));
                entity.setName(name);
                entity.setDescription("Migrated default cost center for branch");
                entity.setCompanyId(resolved.getCompany().getId());
                entity.setBranchId(resolved.getId());
            }
            toSave.add(entity);
        }

        if (!toSave.isEmpty()) {
            costCenterRepository.saveAll(toSave);
        }

        log.info("CostCenter seed upserted {} of {} branches", toSave.size(), branches.size());
        exchange.setProperty("batchImported", toSave.size());
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
