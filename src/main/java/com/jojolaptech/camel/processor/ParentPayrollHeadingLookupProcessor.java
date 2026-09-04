package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.ParentPayrollHeading;
import com.jojolaptech.camel.model.postgres.master.MasterLookupEntity;
import com.jojolaptech.camel.repository.postgres.master.PgMasterLookupRepository;
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

/** Step 9o: parentPayrollHeading → master_lookup (PARENT_PAYROLL_HEADING). */
@Component
@RequiredArgsConstructor
public class ParentPayrollHeadingLookupProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(ParentPayrollHeadingLookupProcessor.class);

    private final PgMasterLookupRepository lookupRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<ParentPayrollHeading> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream()
                .map(s -> PayrollCatalogLeftoversMigrationMapper.PARENT_PAYROLL_HEADING_MYSQL_ID_OFFSET + s.getId())
                .collect(Collectors.toSet());
        Set<Long> existing = lookupRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        List<MasterLookupEntity> toSave = new ArrayList<>();
        for (ParentPayrollHeading source : batch) {
            long mysqlId =
                    PayrollCatalogLeftoversMigrationMapper.PARENT_PAYROLL_HEADING_MYSQL_ID_OFFSET + source.getId();
            if (existing.contains(mysqlId)) {
                continue;
            }
            MasterLookupEntity mapped =
                    PayrollCatalogLeftoversMigrationMapper.fromParentPayrollHeading(source);
            if (mapped == null) {
                log.warn("Skipping parentPayrollHeading id={}, mapping failed", source.getId());
                continue;
            }
            if (lookupRepository.findByCategoryAndCode(mapped.getCategory(), mapped.getCode()).isPresent()) {
                log.warn("Skipping parentPayrollHeading id={}, code exists", source.getId());
                continue;
            }
            toSave.add(mapped);
            existing.add(mysqlId);
        }

        if (!toSave.isEmpty()) {
            lookupRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
