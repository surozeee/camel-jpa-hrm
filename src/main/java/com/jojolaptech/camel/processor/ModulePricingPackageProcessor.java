package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.ModulePricing;
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
 * Step 9j: modulePricing → module_pricing_package (priced tiers; mysql_id ≥ 26e12).
 * Does <strong>not</strong> write {@code module_pricing_scope} — module tree/scopes are ERP-seeded.
 */
@Component
@RequiredArgsConstructor
public class ModulePricingPackageProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(ModulePricingPackageProcessor.class);

    private final PgModulePricingPackageRepository packageRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<ModulePricing> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream()
                .map(p -> SaasBillingMigrationMapper.MODULE_PRICING_MYSQL_ID_OFFSET + p.getId())
                .collect(Collectors.toSet());
        Set<Long> existingIds = packageRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        List<ModulePricingPackageEntity> toSave = new ArrayList<>();
        for (ModulePricing source : batch) {
            long mysqlId = SaasBillingMigrationMapper.MODULE_PRICING_MYSQL_ID_OFFSET + source.getId();
            if (existingIds.contains(mysqlId)) {
                continue;
            }
            ModulePricingPackageEntity mapped = SaasBillingMigrationMapper.fromModulePricing(source);
            if (mapped == null) {
                log.warn("Skipping modulePricing id={}, mapping failed", source.getId());
                continue;
            }
            if (packageRepository.findByPackageCode(mapped.getPackageCode()).isPresent()) {
                log.warn(
                        "Skipping modulePricing id={}, package_code {} already exists",
                        source.getId(),
                        mapped.getPackageCode());
                continue;
            }
            toSave.add(mapped);
            existingIds.add(mysqlId);
        }

        if (!toSave.isEmpty()) {
            packageRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
