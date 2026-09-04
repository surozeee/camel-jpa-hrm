package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.SubscriptionPayment;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.user.SubscriptionPaymentHistoryEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.user.PgSubscriptionPaymentHistoryRepository;
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

/** Step 24b: subscriptionPayment → subscription_payment_history. */
@Component
@RequiredArgsConstructor
public class SubscriptionPaymentHistoryProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionPaymentHistoryProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgSubscriptionPaymentHistoryRepository paymentHistoryRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<SubscriptionPayment> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(SubscriptionPayment::getId).collect(Collectors.toSet());
        Set<Long> existingIds = paymentHistoryRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> companyMysqlIds = batch.stream()
                .filter(p -> p.getCompany() != null)
                .map(p -> p.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companyByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, c -> c, (a, b) -> a));

        List<SubscriptionPaymentHistoryEntity> toSave = new ArrayList<>();
        for (SubscriptionPayment source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getCompany() == null) {
                log.warn("Skipping subscriptionPayment id={}, missing company", source.getId());
                continue;
            }
            CompanyEntity company = companyByMysqlId.get(source.getCompany().getId());
            if (company == null) {
                log.warn(
                        "Skipping subscriptionPayment id={}, company mysqlId={} not migrated",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }
            SubscriptionPaymentHistoryEntity mapped =
                    SaasBillingMigrationMapper.fromSubscriptionPayment(source, company.getId());
            if (mapped == null) {
                log.warn("Skipping subscriptionPayment id={}, mapping failed", source.getId());
                continue;
            }
            toSave.add(mapped);
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            paymentHistoryRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
