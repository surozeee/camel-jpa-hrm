package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Bank;
import com.jojolaptech.camel.model.postgres.master.BankEntity;
import com.jojolaptech.camel.repository.postgres.master.PgBankRepository;
import java.util.ArrayList;
import java.util.HashSet;
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

/** Migrates mysql bank → master bank. */
@Component
@RequiredArgsConstructor
public class LegacyBankProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(LegacyBankProcessor.class);

    private final PgBankRepository bankRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Bank> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> existingIds = bankRepository.findMysqlIdsByMysqlIdIn(
                batch.stream().map(Bank::getId).collect(Collectors.toSet()));

        Set<String> namesInBatch = batch.stream()
                .filter(b -> b.getName() != null)
                .map(b -> b.getName().trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        Set<String> existingNames = new HashSet<>(bankRepository.findExistingNamesLowerCase(namesInBatch));

        List<BankEntity> toSave = new ArrayList<>();
        for (Bank source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            String name = source.getName() != null ? source.getName().trim() : null;
            if (name == null || name.isEmpty()) {
                log.warn("Skipping bank id={}, blank name", source.getId());
                continue;
            }
            String nameKey = name.toLowerCase(Locale.ROOT);
            if (existingNames.contains(nameKey)) {
                log.warn("Skipping bank id={}, name already exists: {}", source.getId(), name);
                continue;
            }

            String code = source.getCode() != null && !source.getCode().isBlank()
                    ? source.getCode().trim()
                    : (source.getShortCode() != null && !source.getShortCode().isBlank()
                            ? source.getShortCode().trim()
                            : "BANK-" + source.getId());

            toSave.add(BankEntity.builder()
                    .mysqlId(source.getId())
                    .name(name)
                    .code(code)
                    .bankType(source.getType() != null ? source.getType().name() : null)
                    .address(source.getAddress())
                    .build());
            existingIds.add(source.getId());
            existingNames.add(nameKey);
        }

        if (!toSave.isEmpty()) {
            bankRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
