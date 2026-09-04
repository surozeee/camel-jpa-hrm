package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Message;
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

/** Step 28b: message → hrm_company_notice. */
@Component
@RequiredArgsConstructor
public class MessageNoticeProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(MessageNoticeProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgEmployeeRepository employeeRepository;
    private final PgCompanyNoticeRepository targetRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Message> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream()
                .map(row -> MessagingMigrationMapper.MESSAGE_MYSQL_ID_OFFSET + row.getId())
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
        for (Message row : batch) {
            if (row.getReceiverEmployee() != null) {
                employeeMysqlIds.add(row.getReceiverEmployee().getId());
            }
            employeeMysqlIds.addAll(MessagingMigrationMapper.parseEmployeeMysqlIds(row.getSenderEmployee()));
        }
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeMysqlIds.isEmpty()
                ? Map.of()
                : employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                        .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        List<CompanyNoticeEntity> toSave = new ArrayList<>();
        for (Message source : batch) {
            long offsetMysqlId = MessagingMigrationMapper.MESSAGE_MYSQL_ID_OFFSET + source.getId();
            if (existingIds.contains(offsetMysqlId)) {
                continue;
            }
            if (source.getCompany() == null) {
                log.warn("Skipping message id={}, missing company", source.getId());
                continue;
            }
            CompanyEntity company = companyByMysqlId.get(source.getCompany().getId());
            if (company == null) {
                log.warn(
                        "Skipping message id={}, company mysqlId={} not migrated",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }
            if (source.getReceiverEmployee() == null) {
                log.warn("Skipping message id={}, missing receiverEmployee", source.getId());
                continue;
            }
            EmployeeEntity receiver = employeeByMysqlId.get(source.getReceiverEmployee().getId());
            if (receiver == null) {
                log.warn(
                        "Skipping message id={}, receiverEmployee mysqlId={} not migrated",
                        source.getId(),
                        source.getReceiverEmployee().getId());
                continue;
            }

            UUID publishedBy = null;
            List<Long> senderIds = MessagingMigrationMapper.parseEmployeeMysqlIds(source.getSenderEmployee());
            if (!senderIds.isEmpty()) {
                EmployeeEntity sender = employeeByMysqlId.get(senderIds.get(0));
                if (sender != null) {
                    publishedBy = sender.getId();
                }
            }

            CompanyNoticeEntity mapped = MessagingMigrationMapper.fromMessage(
                    source, company.getId(), List.of(receiver.getId()), publishedBy);
            if (mapped == null) {
                log.warn("Skipping message id={}, mapping failed", source.getId());
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
