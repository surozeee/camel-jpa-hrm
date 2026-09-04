package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.PayType;
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

/** Step 9k: payType → module_pricing_package (mysql_id ≥ 34e12). */
@Component
@RequiredArgsConstructor
public class PayTypePackageProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(PayTypePackageProcessor.class);

    private final PgModulePricingPackageRepository packageRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<PayType> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream()
                .map(p -> PlatformSaasLeftoversMigrationMapper.PAY_TYPE_MYSQL_ID_OFFSET + p.getId())
                .collect(Collectors.toSet());
        Set<Long> existingIds = packageRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        List<ModulePricingPackageEntity> toSave = new ArrayList<>();
        for (PayType source : batch) {
            long mysqlId = PlatformSaasLeftoversMigrationMapper.PAY_TYPE_MYSQL_ID_OFFSET + source.getId();
            if (existingIds.contains(mysqlId)) {
                continue;
            }
            ModulePricingPackageEntity mapped = PlatformSaasLeftoversMigrationMapper.fromPayType(source);
            if (mapped == null) {
                log.warn("Skipping payType id={}, mapping failed", source.getId());
                continue;
            }
            if (packageRepository.findByPackageCode(mapped.getPackageCode()).isPresent()) {
                log.warn("Skipping payType id={}, package_code {} exists", source.getId(), mapped.getPackageCode());
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
