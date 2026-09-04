package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyMessageCompany;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyNoticeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyNoticeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
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

/** Step 28c: companyMessageCompany → hrm_company_notice. */
@Component
@RequiredArgsConstructor
public class CompanyMessageNoticeProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(CompanyMessageNoticeProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgCompanyNoticeRepository targetRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<CompanyMessageCompany> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream()
                .map(row -> MessagingMigrationMapper.COMPANY_MESSAGE_MYSQL_ID_OFFSET + row.getId())
                .collect(Collectors.toSet());
        Set<Long> existingIds = targetRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> companyMysqlIds = batch.stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companyByMysqlId = companyMysqlIds.isEmpty()
                ? Map.of()
                : companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                        .collect(Collectors.toMap(CompanyEntity::getMysqlId, c -> c, (a, b) -> a));

        List<CompanyNoticeEntity> toSave = new ArrayList<>();
        for (CompanyMessageCompany source : batch) {
            long offsetMysqlId = MessagingMigrationMapper.COMPANY_MESSAGE_MYSQL_ID_OFFSET + source.getId();
            if (existingIds.contains(offsetMysqlId)) {
                continue;
            }
            if (source.getCompany() == null) {
                log.warn("Skipping companyMessageCompany id={}, missing company", source.getId());
                continue;
            }
            if (source.getCompanyMessage() == null) {
                log.warn("Skipping companyMessageCompany id={}, missing companyMessage", source.getId());
                continue;
            }
            CompanyEntity company = companyByMysqlId.get(source.getCompany().getId());
            if (company == null) {
                log.warn(
                        "Skipping companyMessageCompany id={}, company mysqlId={} not migrated",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }

            CompanyNoticeEntity mapped =
                    MessagingMigrationMapper.fromCompanyMessageCompany(source, company.getId());
            if (mapped == null) {
                log.warn("Skipping companyMessageCompany id={}, mapping failed", source.getId());
                continue;
            }
            toSave.add(mapped);
            existingIds.add(offsetMysqlId);
        }

        if (!toSave.isEmpty()) {
            targetRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
