package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.UserLicense;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.user.CompanySubscriptionEntity;
import com.jojolaptech.camel.model.postgres.user.SubscriptionPaymentHistoryEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.user.PgCompanySubscriptionRepository;
import com.jojolaptech.camel.repository.postgres.user.PgSubscriptionPaymentHistoryRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

/**
 * Step 24c: userLicense → company_subscription (fill/extend) + payment history (mysql_id ≥ 35e12).
 */
@Component
@RequiredArgsConstructor
public class UserLicenseSubscriptionProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(UserLicenseSubscriptionProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgCompanySubscriptionRepository subscriptionRepository;
    private final PgSubscriptionPaymentHistoryRepository paymentHistoryRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<UserLicense> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> companyMysqlIds = batch.stream()
                .filter(u -> u.getCompany() != null)
                .map(u -> u.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companyByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, c -> c, (a, b) -> a));

        Set<Long> paymentMysqlIds = batch.stream()
                .map(u -> PlatformSaasLeftoversMigrationMapper.USER_LICENSE_PAYMENT_MYSQL_ID_OFFSET + u.getId())
                .collect(Collectors.toSet());
        Set<Long> existingPaymentIds = paymentHistoryRepository.findMysqlIdsByMysqlIdIn(paymentMysqlIds);

        Set<UUID> companyUuids = companyByMysqlId.values().stream()
                .map(CompanyEntity::getId)
                .collect(Collectors.toSet());
        Map<UUID, CompanySubscriptionEntity> existingByCompany = new HashMap<>();
        if (!companyUuids.isEmpty()) {
            for (CompanySubscriptionEntity sub : subscriptionRepository.findByCompanyIdIn(companyUuids)) {
                existingByCompany.put(sub.getCompanyId(), sub);
            }
        }

        List<CompanySubscriptionEntity> subscriptionsToSave = new ArrayList<>();
        List<SubscriptionPaymentHistoryEntity> paymentsToSave = new ArrayList<>();
        Set<UUID> claimed = new HashSet<>();
        int imported = 0;

        for (UserLicense source : batch) {
            if (source.getCompany() == null) {
                log.warn("Skipping userLicense id={}, missing company", source.getId());
                continue;
            }
            CompanyEntity company = companyByMysqlId.get(source.getCompany().getId());
            if (company == null) {
                log.warn(
                        "Skipping userLicense id={}, company mysqlId={} not migrated",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }

            long paymentMysqlId =
                    PlatformSaasLeftoversMigrationMapper.USER_LICENSE_PAYMENT_MYSQL_ID_OFFSET + source.getId();
            if (!existingPaymentIds.contains(paymentMysqlId)) {
                SubscriptionPaymentHistoryEntity payment =
                        PlatformSaasLeftoversMigrationMapper.paymentFromUserLicense(source, company.getId());
                if (payment != null) {
                    paymentsToSave.add(payment);
                    existingPaymentIds.add(paymentMysqlId);
                    imported++;
                }
            }

            CompanySubscriptionEntity mapped =
                    PlatformSaasLeftoversMigrationMapper.fromUserLicense(source, company.getId());
            if (mapped == null) {
                continue;
            }

            CompanySubscriptionEntity existing = existingByCompany.get(company.getId());
            if (existing == null) {
                if (claimed.contains(company.getId())) {
                    continue;
                }
                subscriptionsToSave.add(mapped);
                existingByCompany.put(company.getId(), mapped);
                claimed.add(company.getId());
                imported++;
                continue;
            }

            // Extend / enrich existing subscription when license is newer or has higher user count
            // Do not overwrite mysqlId from companyValidity rows
            boolean changed = false;
            if (mapped.getSubscriptionEndDate() != null
                    && (existing.getSubscriptionEndDate() == null
                            || mapped.getSubscriptionEndDate().isAfter(existing.getSubscriptionEndDate()))) {
                existing.setSubscriptionEndDate(mapped.getSubscriptionEndDate());
                changed = true;
            }
            if (mapped.getSubscriptionStartDate() != null
                    && (existing.getSubscriptionStartDate() == null
                            || mapped.getSubscriptionStartDate().isBefore(existing.getSubscriptionStartDate()))) {
                existing.setSubscriptionStartDate(mapped.getSubscriptionStartDate());
                changed = true;
            }
            if (mapped.getMaxUsers() != null
                    && (existing.getMaxUsers() == null || mapped.getMaxUsers() > existing.getMaxUsers())) {
                existing.setMaxUsers(mapped.getMaxUsers());
                changed = true;
            }
            if (changed) {
                subscriptionsToSave.add(existing);
                // Enrichment only — do not count toward batchImported (pipeline QA = inserts)
            }
        }

        if (!paymentsToSave.isEmpty()) {
            paymentHistoryRepository.saveAll(paymentsToSave);
        }
        if (!subscriptionsToSave.isEmpty()) {
            subscriptionRepository.saveAll(subscriptionsToSave);
        }
        exchange.setProperty("batchImported", imported);
    }
}
