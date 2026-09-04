package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Notification;
import com.jojolaptech.camel.model.mysql.enums.NotificationReceivedType;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyNoticeEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyNoticeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import java.util.ArrayList;
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

/** Step 28f: notification → hrm_company_notice. */
@Component
@RequiredArgsConstructor
public class NotificationNoticeProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(NotificationNoticeProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgEmployeeRepository employeeRepository;
    private final PgCompanyNoticeRepository targetRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Notification> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream()
                .map(row -> MessagingMigrationMapper.NOTIFICATION_MYSQL_ID_OFFSET + row.getId())
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

        Set<Long> employeeMysqlIds = new HashSet<>();
        for (Notification row : batch) {
            if (row.getNotificationReceivedType() == NotificationReceivedType.Single
                    && row.getReceiverId() != null) {
                employeeMysqlIds.add(row.getReceiverId());
            }
            if (row.getSenderId() != null) {
                employeeMysqlIds.add(row.getSenderId());
            }
        }
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeMysqlIds.isEmpty()
                ? Map.of()
                : employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                        .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        List<CompanyNoticeEntity> toSave = new ArrayList<>();
        for (Notification source : batch) {
            long offsetMysqlId = MessagingMigrationMapper.NOTIFICATION_MYSQL_ID_OFFSET + source.getId();
            if (existingIds.contains(offsetMysqlId)) {
                continue;
            }
            if (source.getCompany() == null) {
                log.warn("Skipping notification id={}, missing company", source.getId());
                continue;
            }
            CompanyEntity company = companyByMysqlId.get(source.getCompany().getId());
            if (company == null) {
                log.warn(
                        "Skipping notification id={}, company mysqlId={} not migrated",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }

            List<UUID> employeeIds = List.of();
            if (source.getNotificationReceivedType() == NotificationReceivedType.Single
                    && source.getReceiverId() != null) {
                EmployeeEntity receiver = employeeByMysqlId.get(source.getReceiverId());
                if (receiver != null) {
                    employeeIds = List.of(receiver.getId());
                } else {
                    log.warn(
                            "Skipping notification id={}, receiverId={} not migrated as employee",
                            source.getId(),
                            source.getReceiverId());
                    continue;
                }
            }

            CompanyNoticeEntity mapped =
                    MessagingMigrationMapper.fromNotification(source, company.getId(), employeeIds);
            if (mapped == null) {
                log.warn("Skipping notification id={}, mapping failed", source.getId());
                continue;
            }

            if (source.getSenderId() != null) {
                EmployeeEntity sender = employeeByMysqlId.get(source.getSenderId());
                if (sender != null) {
                    mapped.setPublishedBy(sender.getId());
                }
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
