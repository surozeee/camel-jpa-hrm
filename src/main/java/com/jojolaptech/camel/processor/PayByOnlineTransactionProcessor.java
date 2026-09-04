package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.PayByOnlineTransaction;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.user.SubscriptionPaymentHistoryEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.user.PgSubscriptionPaymentHistoryRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Step 23z: payByOnlineTransaction → enrich existing companyValidity payment history
 * (24e12+validityId), or create new payment history at 59e12+id when none exists.
 */
@Component
@RequiredArgsConstructor
public class PayByOnlineTransactionProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(PayByOnlineTransactionProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgSubscriptionPaymentHistoryRepository paymentHistoryRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<PayByOnlineTransaction> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> validityPaymentMysqlIds = batch.stream()
                .filter(s -> s.getCompanyValidityId() != null)
                .map(s -> SaasBillingMigrationMapper.COMPANY_VALIDITY_PAYMENT_MYSQL_ID_OFFSET
                        + s.getCompanyValidityId().getId())
                .collect(Collectors.toSet());
        Set<Long> onlineMysqlIds = batch.stream()
                .map(s -> PayrollCatalogLeftoversMigrationMapper.PAY_BY_ONLINE_MYSQL_ID_OFFSET + s.getId())
                .collect(Collectors.toSet());
        Set<Long> lookupIds = new HashSet<>();
        lookupIds.addAll(validityPaymentMysqlIds);
        lookupIds.addAll(onlineMysqlIds);

        Map<Long, SubscriptionPaymentHistoryEntity> paymentsByMysqlId =
                paymentHistoryRepository.findByMysqlIdIn(lookupIds).stream()
                        .collect(Collectors.toMap(
                                SubscriptionPaymentHistoryEntity::getMysqlId, Function.identity(), (a, b) -> a));

        Set<Long> companyMysqlIds = batch.stream()
                .filter(s -> s.getCompanyValidityId() != null && s.getCompanyValidityId().getCompany() != null)
                .map(s -> s.getCompanyValidityId().getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companies = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, Function.identity(), (a, b) -> a));

        List<SubscriptionPaymentHistoryEntity> toSave = new ArrayList<>();
        int imported = 0;

        for (PayByOnlineTransaction source : batch) {
            if (source.getCompanyValidityId() == null || source.getCompanyValidityId().getCompany() == null) {
                log.warn("Skipping payByOnlineTransaction id={}, missing companyValidity/company", source.getId());
                continue;
            }
            long validityPaymentMysqlId =
                    SaasBillingMigrationMapper.COMPANY_VALIDITY_PAYMENT_MYSQL_ID_OFFSET
                            + source.getCompanyValidityId().getId();
            SubscriptionPaymentHistoryEntity existing = paymentsByMysqlId.get(validityPaymentMysqlId);
            if (existing != null) {
                boolean changed = enrichPayment(existing, source);
                if (changed) {
                    toSave.add(existing);
                    imported++;
                }
                continue;
            }

            long onlineMysqlId =
                    PayrollCatalogLeftoversMigrationMapper.PAY_BY_ONLINE_MYSQL_ID_OFFSET + source.getId();
            if (paymentsByMysqlId.containsKey(onlineMysqlId)) {
                continue;
            }

            CompanyEntity company = companies.get(source.getCompanyValidityId().getCompany().getId());
            if (company == null) {
                log.warn(
                        "Skipping payByOnlineTransaction id={}, company mysqlId={} not migrated",
                        source.getId(),
                        source.getCompanyValidityId().getCompany().getId());
                continue;
            }
            SubscriptionPaymentHistoryEntity created =
                    PayrollCatalogLeftoversMigrationMapper.paymentFromOnlineTransaction(source, company.getId());
            if (created == null) {
                log.warn("Skipping payByOnlineTransaction id={}, mapping failed", source.getId());
                continue;
            }
            toSave.add(created);
            paymentsByMysqlId.put(onlineMysqlId, created);
            imported++;
        }

        if (!toSave.isEmpty()) {
            paymentHistoryRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", imported);
    }

    private static boolean enrichPayment(
            SubscriptionPaymentHistoryEntity payment, PayByOnlineTransaction source) {
        boolean changed = false;
        String txn = PayrollCatalogLeftoversMigrationMapper.blankToNull(source.getTransactionId());
        if (txn != null
                && (payment.getPaymentReference() == null || payment.getPaymentReference().isBlank())) {
            payment.setPaymentReference(txn);
            changed = true;
        }
        var status = PayrollCatalogLeftoversMigrationMapper.mapOnlineStatus(source.getPaymentResponseStatus());
        if (status != null && payment.getPaymentStatus() != status) {
            payment.setPaymentStatus(status);
            changed = true;
        }
        String marker = "[migrated-pbo:" + source.getId() + "]";
        String remarks = payment.getRemarks() != null ? payment.getRemarks() : "";
        if (!remarks.contains(marker)) {
            StringBuilder sb = new StringBuilder(remarks);
            if (!sb.isEmpty()) {
                sb.append("; ");
            }
            sb.append(marker);
            if (txn != null) {
                sb.append(" transactionId=").append(txn);
            }
            if (source.getPaymentResponseStatus() != null) {
                sb.append(" status=").append(source.getPaymentResponseStatus());
            }
            payment.setRemarks(sb.toString());
            changed = true;
        }
        return changed;
    }
}
