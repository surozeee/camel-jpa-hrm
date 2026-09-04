package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.NotificationViewed;
import com.jojolaptech.camel.model.postgres.company.CompanyNoticeEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeNoticeReadEntity;
import com.jojolaptech.camel.model.postgres.user.UserEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyNoticeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeNoticeReadRepository;
import com.jojolaptech.camel.repository.postgres.user.PgUserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Step 28g: notificationViewed → hrm_employee_notice_read. */
@Component
@RequiredArgsConstructor
public class NotificationViewedProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(NotificationViewedProcessor.class);

    private final PgCompanyNoticeRepository noticeRepository;
    private final PgUserRepository userRepository;
    private final PgEmployeeNoticeReadRepository targetRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<NotificationViewed> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(NotificationViewed::getId).collect(Collectors.toSet());
        Set<Long> existingIds = targetRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> noticeMysqlIds = batch.stream()
                .filter(row -> row.getNotification() != null && row.getNotification().getId() != null)
                .map(row -> MessagingMigrationMapper.NOTIFICATION_MYSQL_ID_OFFSET
                        + row.getNotification().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyNoticeEntity> noticeByMysqlId = noticeMysqlIds.isEmpty()
                ? Map.of()
                : noticeRepository.findByMysqlIdIn(noticeMysqlIds).stream()
                        .collect(Collectors.toMap(CompanyNoticeEntity::getMysqlId, n -> n, (a, b) -> a));

        Set<Long> userMysqlIds = batch.stream()
                .map(NotificationViewed::getViewedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, UserEntity> userByMysqlId = userMysqlIds.isEmpty()
                ? Map.of()
                : userRepository.findByMysqlIdIn(userMysqlIds).stream()
                        .collect(Collectors.toMap(UserEntity::getMysqlId, u -> u, (a, b) -> a));

        List<EmployeeNoticeReadEntity> toSave = new ArrayList<>();
        for (NotificationViewed source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (Boolean.FALSE.equals(source.getIsViewed())) {
                continue;
            }
            if (source.getNotification() == null || source.getNotification().getId() == null) {
                log.warn("Skipping notificationViewed id={}, missing notification", source.getId());
                continue;
            }
            long noticeMysqlId = MessagingMigrationMapper.NOTIFICATION_MYSQL_ID_OFFSET
                    + source.getNotification().getId();
            CompanyNoticeEntity notice = noticeByMysqlId.get(noticeMysqlId);
            if (notice == null) {
                log.warn(
                        "Skipping notificationViewed id={}, notice mysqlId={} not migrated",
                        source.getId(),
                        noticeMysqlId);
                continue;
            }
            if (source.getViewedBy() == null) {
                log.warn("Skipping notificationViewed id={}, missing viewedBy", source.getId());
                continue;
            }
            UserEntity user = userByMysqlId.get(source.getViewedBy());
            if (user == null) {
                log.warn(
                        "Skipping notificationViewed id={}, user mysqlId={} not migrated",
                        source.getId(),
                        source.getViewedBy());
                continue;
            }

            EmployeeNoticeReadEntity mapped =
                    MessagingMigrationMapper.fromNotificationViewed(source, user.getId(), notice.getId());
            if (mapped == null) {
                continue;
            }
            toSave.add(mapped);
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            targetRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
