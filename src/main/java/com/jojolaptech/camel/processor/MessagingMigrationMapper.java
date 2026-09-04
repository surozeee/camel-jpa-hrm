package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyMessage;
import com.jojolaptech.camel.model.mysql.CompanyMessageCompany;
import com.jojolaptech.camel.model.mysql.Event;
import com.jojolaptech.camel.model.mysql.Happening;
import com.jojolaptech.camel.model.mysql.Message;
import com.jojolaptech.camel.model.mysql.Notice;
import com.jojolaptech.camel.model.mysql.Notification;
import com.jojolaptech.camel.model.mysql.NotificationViewed;
import com.jojolaptech.camel.model.mysql.enums.MessageCategory;
import com.jojolaptech.camel.model.postgres.company.CompanyNoticeEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeNoticeReadEntity;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Maps MySQL notices/messages/events → {@code hrm_company_notice} (+ notice read). */
public final class MessagingMigrationMapper {

    public static final long NOTICE_MYSQL_ID_OFFSET = 27_000_000_000_000L;
    public static final long MESSAGE_MYSQL_ID_OFFSET = 28_000_000_000_000L;
    public static final long COMPANY_MESSAGE_MYSQL_ID_OFFSET = 29_000_000_000_000L;
    public static final long HAPPENING_MYSQL_ID_OFFSET = 30_000_000_000_000L;
    public static final long EVENT_MYSQL_ID_OFFSET = 31_000_000_000_000L;
    public static final long NOTIFICATION_MYSQL_ID_OFFSET = 32_000_000_000_000L;

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private MessagingMigrationMapper() {}

    public static CompanyNoticeEntity fromNotice(
            Notice source, UUID companyId, List<UUID> employeeIds, UUID publishedBy) {
        if (source == null || source.getId() == null || companyId == null) {
            return null;
        }
        String body = blankToNull(source.getText());
        if (body == null) {
            body = "(empty notice)";
        }
        return baseNotice(
                        NOTICE_MYSQL_ID_OFFSET + source.getId(),
                        companyId,
                        titleFromBody("Notice", source.getId(), body),
                        body,
                        toLdt(source.getSentDate()),
                        toLdt(source.getExpiryDate()),
                        blankToNull(source.getAttachment()),
                        employeeIds,
                        publishedBy)
                .build();
    }

    public static CompanyNoticeEntity fromMessage(
            Message source, UUID companyId, List<UUID> employeeIds, UUID publishedBy) {
        if (source == null || source.getId() == null || companyId == null) {
            return null;
        }
        String body = blankToNull(source.getText());
        if (body == null) {
            body = "(empty message)";
        }
        MessageCategory category = source.getMessageCategory();
        String prefix = category != null ? "Message (" + category.name() + ")" : "Message";
        return baseNotice(
                        MESSAGE_MYSQL_ID_OFFSET + source.getId(),
                        companyId,
                        titleFromBody(prefix, source.getId(), body),
                        body,
                        toLdt(source.getSentDate()),
                        toLdt(source.getExpiryDate()),
                        blankToNull(source.getAttachment()),
                        employeeIds,
                        publishedBy)
                .build();
    }

    public static CompanyNoticeEntity fromCompanyMessageCompany(
            CompanyMessageCompany junction, UUID companyId) {
        if (junction == null || junction.getId() == null || companyId == null) {
            return null;
        }
        CompanyMessage msg = junction.getCompanyMessage();
        if (msg == null) {
            return null;
        }
        String body = blankToNull(msg.getMessage());
        if (body == null) {
            body = "(empty company message)";
        }
        CompanyNoticeEntity entity = baseNotice(
                COMPANY_MESSAGE_MYSQL_ID_OFFSET + junction.getId(),
                companyId,
                titleFromBody("Company message", msg.getId(), body),
                body,
                toLdt(msg.getStartDate()),
                toLdt(msg.getEndDate()),
                null,
                List.of(),
                null)
                .build();
        if (Boolean.FALSE.equals(msg.getIsEnable())) {
            entity.setStatus(StatusEnum.INACTIVE);
        }
        return entity;
    }

    public static CompanyNoticeEntity fromHappening(
            Happening source, UUID companyId, List<UUID> employeeIds, UUID publishedBy) {
        if (source == null || source.getId() == null || companyId == null) {
            return null;
        }
        String text = blankToNull(source.getText());
        if (text == null) {
            text = "(empty happening)";
        }
        String body = text;
        LocalDateTime happeningAt = toLdt(source.getHappeningDate());
        if (happeningAt != null) {
            body = "Happening date: " + happeningAt + "\n\n" + text;
        }
        return baseNotice(
                        HAPPENING_MYSQL_ID_OFFSET + source.getId(),
                        companyId,
                        titleFromBody("Happening", source.getId(), text),
                        body,
                        toLdt(source.getSentDate()),
                        toLdt(source.getExpiryDate()),
                        blankToNull(source.getAttachment()),
                        employeeIds,
                        publishedBy)
                .build();
    }

    public static CompanyNoticeEntity fromEvent(
            Event source, UUID companyId, List<UUID> employeeIds, UUID publishedBy) {
        if (source == null || source.getId() == null || companyId == null) {
            return null;
        }
        String text = blankToNull(source.getText());
        if (text == null) {
            text = "(empty event)";
        }
        String body = text;
        LocalDateTime eventAt = toLdt(source.getEventDate());
        if (eventAt != null) {
            body = "Event date: " + eventAt + "\n\n" + text;
        }
        return baseNotice(
                        EVENT_MYSQL_ID_OFFSET + source.getId(),
                        companyId,
                        titleFromBody("Event", source.getId(), text),
                        body,
                        toLdt(source.getSentDate()),
                        toLdt(source.getExpiryDate()),
                        blankToNull(source.getAttachment()),
                        employeeIds,
                        publishedBy)
                .build();
    }

    public static CompanyNoticeEntity fromNotification(
            Notification source, UUID companyId, List<UUID> employeeIds) {
        if (source == null || source.getId() == null || companyId == null) {
            return null;
        }
        String body = blankToNull(source.getMessage());
        if (body == null) {
            body = "(empty notification)";
        }
        String type = source.getNotificationReceivedType() != null
                ? source.getNotificationReceivedType().name()
                : "All";
        return baseNotice(
                        NOTIFICATION_MYSQL_ID_OFFSET + source.getId(),
                        companyId,
                        titleFromBody("Notification (" + type + ")", source.getId(), body),
                        body,
                        toLdt(source.getCreatedDate()),
                        null,
                        null,
                        employeeIds,
                        null)
                .sendNotification(Boolean.TRUE)
                .sendEmail(Boolean.FALSE)
                .build();
    }

    public static EmployeeNoticeReadEntity fromNotificationViewed(
            NotificationViewed source, UUID userId, UUID noticeId) {
        if (source == null || source.getId() == null || userId == null || noticeId == null) {
            return null;
        }
        if (Boolean.FALSE.equals(source.getIsViewed())) {
            return null;
        }
        LocalDateTime readAt = toLdt(source.getViewedDate());
        if (readAt == null) {
            readAt = LocalDateTime.now();
        }
        return EmployeeNoticeReadEntity.builder()
                .mysqlId(source.getId())
                .userId(userId)
                .noticeId(noticeId)
                .readAt(readAt)
                .build();
    }

    /** Parse CSV / space / semicolon separated MySQL employee ids from legacy string columns. */
    public static List<Long> parseEmployeeMysqlIds(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        Set<Long> ids = new HashSet<>();
        for (String token : raw.split("[,;\\s]+")) {
            String t = token.trim();
            if (t.isEmpty()) {
                continue;
            }
            try {
                ids.add(Long.parseLong(t));
            } catch (NumberFormatException ignored) {
                // skip non-numeric tokens
            }
        }
        return new ArrayList<>(ids);
    }

    private static CompanyNoticeEntity.CompanyNoticeEntityBuilder baseNotice(
            long mysqlId,
            UUID companyId,
            String title,
            String message,
            LocalDateTime publishedAt,
            LocalDateTime expiresAt,
            String actionUrl,
            List<UUID> employeeIds,
            UUID publishedBy) {
        List<UUID> audience = employeeIds != null ? new ArrayList<>(employeeIds) : new ArrayList<>();
        return CompanyNoticeEntity.builder()
                .mysqlId(mysqlId)
                .companyId(companyId)
                .title(truncate(title, 300))
                .message(message)
                .publishedAt(publishedAt)
                .publishedBy(publishedBy)
                .expiresAt(expiresAt)
                .actionUrl(truncate(actionUrl, 500))
                .employeeIds(audience)
                .branchIds(new ArrayList<>())
                .departmentIds(new ArrayList<>())
                .sendEmail(Boolean.TRUE)
                .sendSms(Boolean.FALSE)
                .sendNotification(Boolean.FALSE)
                .emailSubject(truncate(title, 500))
                .emailBody(message);
    }

    private static String titleFromBody(String prefix, Long id, String body) {
        String firstLine = body.lines().findFirst().orElse(body).trim();
        if (firstLine.length() > 80) {
            firstLine = firstLine.substring(0, 77) + "...";
        }
        if (firstLine.isEmpty()) {
            return prefix + " #" + id;
        }
        return prefix + ": " + firstLine;
    }

    private static LocalDateTime toLdt(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZONE).toLocalDateTime();
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
