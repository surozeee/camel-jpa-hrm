package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Taxation;
import com.jojolaptech.camel.model.postgres.company.CompanyFiscalYearEntity;
import com.jojolaptech.camel.model.postgres.master.NepaliTaxEntity;
import com.jojolaptech.camel.model.postgres.master.NepaliTaxRateEntity;
import com.jojolaptech.camel.model.postgres.master.enums.TaxMaritalStatusEnum;
import com.jojolaptech.camel.model.postgres.master.enums.TaxRateTypeEnum;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyFiscalYearRepository;
import com.jojolaptech.camel.repository.postgres.master.PgNepaliTaxRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaxationProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(TaxationProcessor.class);

    private final PgCompanyFiscalYearRepository companyFiscalYearRepository;
    private final PgNepaliTaxRepository nepaliTaxRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Taxation> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> fiscalYearMysqlIds = batch.stream()
                .map(t -> t.getFiscalYear().getId())
                .collect(Collectors.toSet());
        Map<Long, UUID> masterFyByMysqlFyId = companyFiscalYearRepository.findByMysqlIdIn(fiscalYearMysqlIds).stream()
                .collect(Collectors.toMap(
                        CompanyFiscalYearEntity::getMysqlId, CompanyFiscalYearEntity::getMasterFiscalYearId));

        Map<TaxGroupKey, List<Taxation>> grouped = new HashMap<>();
        for (Taxation row : batch) {
            UUID masterFyId = masterFyByMysqlFyId.get(row.getFiscalYear().getId());
            if (masterFyId == null) {
                log.warn("Skipping taxation id={}, fiscal year mysqlId={} not migrated",
                        row.getId(), row.getFiscalYear().getId());
                continue;
            }
            TaxMaritalStatusEnum maritalStatus = FiscalMigrationMapper.maritalStatus(row.getGender());
            grouped.computeIfAbsent(new TaxGroupKey(masterFyId, maritalStatus), key -> new ArrayList<>()).add(row);
        }

        if (grouped.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<UUID> masterFyIds = grouped.keySet().stream().map(TaxGroupKey::masterFyId).collect(Collectors.toSet());
        Map<TaxGroupKey, NepaliTaxEntity> existing = nepaliTaxRepository.findByFiscalYearIdInWithRates(masterFyIds)
                .stream()
                .collect(Collectors.toMap(
                        tax -> new TaxGroupKey(tax.getFiscalYearId(), tax.getMaritalStatus()),
                        tax -> tax,
                        (left, right) -> left));

        List<NepaliTaxEntity> toSave = new ArrayList<>();
        int imported = 0;

        for (Map.Entry<TaxGroupKey, List<Taxation>> entry : grouped.entrySet()) {
            TaxGroupKey key = entry.getKey();
            if (existing.containsKey(key)) {
                continue;
            }
            List<Taxation> slabs = entry.getValue().stream()
                    .sorted(Comparator.comparingDouble(Taxation::getStartRange))
                    .toList();

            NepaliTaxEntity tax = NepaliTaxEntity.builder()
                    .fiscalYearId(key.masterFyId())
                    .maritalStatus(key.maritalStatus())
                    .build();

            List<NepaliTaxRateEntity> rates = new ArrayList<>();
            for (Taxation slab : slabs) {
                NepaliTaxRateEntity rate = NepaliTaxRateEntity.builder()
                        .minAmount(BigDecimal.valueOf(slab.getStartRange()))
                        .maxAmount(BigDecimal.valueOf(slab.getEndRange()))
                        .rateType(TaxRateTypeEnum.PERCENTAGE)
                        .taxRate(BigDecimal.valueOf(slab.getPercent()))
                        .nepaliTax(tax)
                        .build();
                rates.add(rate);
            }
            tax.setRates(rates);
            toSave.add(tax);
            imported += rates.size();
        }

        if (!toSave.isEmpty()) {
            nepaliTaxRepository.saveAll(toSave);
        }

        exchange.setProperty("batchImported", imported);
    }

    private record TaxGroupKey(UUID masterFyId, TaxMaritalStatusEnum maritalStatus) {
    }
}
