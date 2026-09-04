package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CostType;
import com.jojolaptech.camel.model.postgres.master.ModulePricingPackageEntity;
import com.jojolaptech.camel.repository.postgres.master.PgModulePricingPackageRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Step 9h: costType → module_pricing_package (billing-cycle catalog; not hrm_cost_center).
 */
@Component
@RequiredArgsConstructor
public class CostTypePackageProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(CostTypePackageProcessor.class);

    private final PgModulePricingPackageRepository packageRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<CostType> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> existingIds = packageRepository.findMysqlIdsByMysqlIdIn(
                batch.stream().map(CostType::getId).collect(Collectors.toSet()));

        List<ModulePricingPackageEntity> toSave = new ArrayList<>();
        for (CostType source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            ModulePricingPackageEntity mapped = SaasBillingMigrationMapper.fromCostType(source);
            if (mapped == null) {
                log.warn("Skipping costType id={}, mapping failed", source.getId());
                continue;
            }
            if (packageRepository.findByPackageCode(mapped.getPackageCode()).isPresent()) {
                log.warn(
                        "Skipping costType id={}, package_code {} already exists",
                        source.getId(),
                        mapped.getPackageCode());
                continue;
            }
            toSave.add(mapped);
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            packageRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
