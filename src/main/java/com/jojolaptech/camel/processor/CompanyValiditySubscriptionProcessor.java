package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyValidity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.user.CompanySubscriptionEntity;
import com.jojolaptech.camel.model.postgres.user.SubscriptionPaymentHistoryEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.user.PgCompanySubscriptionRepository;
import com.jojolaptech.camel.repository.postgres.user.PgSubscriptionPaymentHistoryRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
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
 * Step 24a: companyValidity → company_subscription (latest validTill per company) + payment history
 * (mysql_id ≥ 24e12).
 */
@Component
@RequiredArgsConstructor
public class CompanyValiditySubscriptionProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(CompanyValiditySubscriptionProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgCompanySubscriptionRepository subscriptionRepository;
    private final PgSubscriptionPaymentHistoryRepository paymentHistoryRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<CompanyValidity> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> companyMysqlIds = batch.stream()
                .filter(v -> v.getCompany() != null)
                .map(v -> v.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companyByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, c -> c, (a, b) -> a));

        Set<Long> validityIds = batch.stream().map(CompanyValidity::getId).collect(Collectors.toSet());
        Set<Long> existingSubMysqlIds = subscriptionRepository.findMysqlIdsByMysqlIdIn(validityIds);

        Set<Long> paymentMysqlIds = validityIds.stream()
                .map(id -> SaasBillingMigrationMapper.COMPANY_VALIDITY_PAYMENT_MYSQL_ID_OFFSET + id)
                .collect(Collectors.toSet());
        Set<Long> existingPaymentMysqlIds = paymentHistoryRepository.findMysqlIdsByMysqlIdIn(paymentMysqlIds);

        Map<UUID, CompanySubscriptionEntity> existingByCompanyId = new HashMap<>();
        Set<UUID> companyUuids = companyByMysqlId.values().stream()
                .map(CompanyEntity::getId)
                .collect(Collectors.toSet());
        if (!companyUuids.isEmpty()) {
            for (CompanySubscriptionEntity existing : subscriptionRepository.findByCompanyIdIn(companyUuids)) {
                existingByCompanyId.put(existing.getCompanyId(), existing);
            }
        }

        // Prefer latest validTill within this batch for subscription upsert decisions
        List<CompanyValidity> ordered = new ArrayList<>(batch);
        ordered.sort(Comparator.comparing(
                (CompanyValidity v) -> v.getValidTill() != null ? v.getValidTill() : new Date(0),
                Comparator.reverseOrder()));

        Set<UUID> claimedCompaniesInBatch = new HashSet<>();
        List<CompanySubscriptionEntity> subscriptionsToSave = new ArrayList<>();
        List<SubscriptionPaymentHistoryEntity> paymentsToSave = new ArrayList<>();
        int imported = 0;

        for (CompanyValidity source : ordered) {
            if (source.getCompany() == null) {
                log.warn("Skipping companyValidity id={}, missing company", source.getId());
                continue;
            }
            CompanyEntity company = companyByMysqlId.get(source.getCompany().getId());
            if (company == null) {
                log.warn(
                        "Skipping companyValidity id={}, company mysqlId={} not migrated",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }

            long paymentMysqlId =
                    SaasBillingMigrationMapper.COMPANY_VALIDITY_PAYMENT_MYSQL_ID_OFFSET + source.getId();
            if (!existingPaymentMysqlIds.contains(paymentMysqlId)) {
                SubscriptionPaymentHistoryEntity payment =
                        SaasBillingMigrationMapper.paymentFromCompanyValidity(source, company.getId());
                if (payment != null) {
                    paymentsToSave.add(payment);
                    existingPaymentMysqlIds.add(paymentMysqlId);
                    imported++;
                }
            }

            if (existingSubMysqlIds.contains(source.getId())) {
                continue;
            }
            if (claimedCompaniesInBatch.contains(company.getId())) {
                continue;
            }

            CompanySubscriptionEntity existing = existingByCompanyId.get(company.getId());
            if (existing != null) {
                LocalDate sourceEnd = toLocalDate(source.getValidTill());
                // Keep existing if its end date is already later or equal
                if (sourceEnd != null
                        && existing.getSubscriptionEndDate() != null
                        && !sourceEnd.isAfter(existing.getSubscriptionEndDate())) {
                    claimedCompaniesInBatch.add(company.getId());
                    continue;
                }
                CompanySubscriptionEntity mapped =
                        SaasBillingMigrationMapper.fromCompanyValidity(source, company.getId());
                if (mapped == null) {
                    log.warn("Skipping companyValidity id={}, subscription mapping failed", source.getId());
                    continue;
                }
                existing.setMysqlId(mapped.getMysqlId());
                existing.setPackageCode(mapped.getPackageCode());
                existing.setPackageName(mapped.getPackageName());
                existing.setBillingCycle(mapped.getBillingCycle());
                existing.setMaxUsers(mapped.getMaxUsers());
                existing.setSubscriptionStartDate(mapped.getSubscriptionStartDate());
                existing.setSubscriptionEndDate(mapped.getSubscriptionEndDate());
                subscriptionsToSave.add(existing);
                claimedCompaniesInBatch.add(company.getId());
                existingSubMysqlIds.add(source.getId());
                // Upsert refresh — do not count toward batchImported (idempotent re-runs)
                continue;
            }

            CompanySubscriptionEntity mapped =
                    SaasBillingMigrationMapper.fromCompanyValidity(source, company.getId());
            if (mapped == null) {
                log.warn("Skipping companyValidity id={}, subscription mapping failed", source.getId());
                continue;
            }
            subscriptionsToSave.add(mapped);
            existingByCompanyId.put(company.getId(), mapped);
            claimedCompaniesInBatch.add(company.getId());
            existingSubMysqlIds.add(source.getId());
            imported++;
        }

        if (!paymentsToSave.isEmpty()) {
            paymentHistoryRepository.saveAll(paymentsToSave);
        }
        if (!subscriptionsToSave.isEmpty()) {
            subscriptionRepository.saveAll(subscriptionsToSave);
        }
        exchange.setProperty("batchImported", imported);
    }

    private static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
