package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Document;
import com.jojolaptech.camel.model.mysql.EmployeeProject;
import com.jojolaptech.camel.model.mysql.JobCategories;
import com.jojolaptech.camel.model.mysql.JobCategory;
import com.jojolaptech.camel.model.mysql.enums.DocumentType;
import com.jojolaptech.camel.model.postgres.company.DocumentEntity;
import com.jojolaptech.camel.model.postgres.company.ExperienceEntity;
import com.jojolaptech.camel.model.postgres.company.SkillCategoryEntity;
import com.jojolaptech.camel.model.postgres.company.enums.DocumentTypeEnum;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Mapping helpers for PIMS leftover steps 22zf–22zi.
 */
final class PimsLeftoversMigrationMapper {

    static final long PROJECT_EXPERIENCE_OFFSET = 22_000_000_000_000L;
    static final long JOB_CATEGORY_OFFSET = 20_000_000_000_000L;
    static final long JOB_CATEGORIES_OFFSET = 21_000_000_000_000L;

    private PimsLeftoversMigrationMapper() {}

    static DocumentEntity fromDocument(Document source, UUID employeeId) {
        if (source == null || employeeId == null) {
            return null;
        }
        String documentNumber = OrgMigrationMapper.trimToNull(source.getDocumentNumber());
        if (documentNumber == null) {
            documentNumber = "MIGRATE-DOC-" + source.getId();
        }
        String imageUrl = OrgMigrationMapper.trimToNull(source.getMyfile());
        if (imageUrl == null) {
            imageUrl = "migrated://mysql-document/" + source.getId();
        }
        LocalDate issuedDate = toLocalDate(source.getIssueDate());
        if (issuedDate == null) {
            issuedDate = LocalDate.of(1970, 1, 1);
        }
        DocumentEntity entity = DocumentEntity.builder()
                .mysqlId(source.getId())
                .employeeId(employeeId)
                .documentNumber(documentNumber)
                .documentType(mapDocumentType(source))
                .issuedDate(issuedDate)
                .expiryDate(toLocalDate(source.getExpireDate()))
                .imageUrl(truncate(imageUrl, 1000))
                .remarks(truncate(buildDocumentRemarks(source), 1000))
                .build();
        entity.setStatus(StatusEnum.ACTIVE);
        return entity;
    }

    static ExperienceEntity fromEmployeeProject(EmployeeProject source, UUID employeeId, LocalDate hireDate) {
        String name = OrgMigrationMapper.trimToNull(source.getName());
        if (name == null || employeeId == null) {
            return null;
        }
        LocalDate joinDate = hireDate != null ? hireDate : LocalDate.of(2000, 1, 1);
        String responsibilities = truncate(OrgMigrationMapper.trimToNull(source.getDescription()), 1000);
        ExperienceEntity entity = ExperienceEntity.builder()
                .mysqlId(PROJECT_EXPERIENCE_OFFSET + source.getId())
                .employeeId(employeeId)
                .companyName(name)
                .designation("Project")
                .joinDate(joinDate)
                .leaveDate(null)
                .isCurrentJob(false)
                .responsibilities(responsibilities)
                .remarks("Migrated from employeeProject")
                .build();
        entity.setStatus(StatusEnum.ACTIVE);
        return entity;
    }

    static SkillCategoryEntity fromJobCategory(JobCategory source, UUID companyId, Long companyMysqlId) {
        String name = OrgMigrationMapper.trimToNull(source.getCategoryName());
        if (name == null || companyId == null || companyMysqlId == null) {
            return null;
        }
        SkillCategoryEntity entity = SkillCategoryEntity.builder()
                .mysqlId(jobCategoryMysqlId(source.getId(), companyMysqlId))
                .companyId(companyId)
                .code("JC-" + source.getId() + "-C" + companyMysqlId)
                .name(name)
                .description("Migrated from jobCategory")
                .build();
        entity.setStatus(StatusEnum.ACTIVE);
        return entity;
    }

    static SkillCategoryEntity fromJobCategories(JobCategories source, UUID companyId) {
        String name = OrgMigrationMapper.trimToNull(source.getJobName());
        if (name == null || companyId == null) {
            return null;
        }
        SkillCategoryEntity entity = SkillCategoryEntity.builder()
                .mysqlId(JOB_CATEGORIES_OFFSET + source.getId())
                .companyId(companyId)
                .code("JCS-" + source.getId())
                .name(name)
                .description("Migrated from jobCategories")
                .build();
        entity.setStatus(StatusEnum.ACTIVE);
        return entity;
    }

    static long jobCategoryMysqlId(long jobCategoryId, long companyMysqlId) {
        return JOB_CATEGORY_OFFSET + jobCategoryId * 1_000_000L + companyMysqlId;
    }

    static DocumentTypeEnum mapDocumentType(Document source) {
        if (source.getDocumentType() != null) {
            DocumentTypeEnum fromEnum = mapMysqlDocumentType(source.getDocumentType());
            if (fromEnum != DocumentTypeEnum.OTHER || source.getDocumentType() == DocumentType.Others) {
                return fromEnum;
            }
        }
        DocumentTypeEnum fromTypeString = mapDocumentTypeString(source.getType());
        if (fromTypeString != null) {
            return fromTypeString;
        }
        if (source.getDocumentType() != null) {
            return mapMysqlDocumentType(source.getDocumentType());
        }
        return DocumentTypeEnum.OTHER;
    }

    private static DocumentTypeEnum mapMysqlDocumentType(DocumentType type) {
        return switch (type) {
            case Citizenship -> DocumentTypeEnum.CITIZENSHIP;
            case Passport -> DocumentTypeEnum.PASSPORT;
            case License -> DocumentTypeEnum.DRIVING_LICENSE;
            case Photo, Signature, Others -> DocumentTypeEnum.OTHER;
        };
    }

    private static DocumentTypeEnum mapDocumentTypeString(String raw) {
        String normalized = OrgMigrationMapper.trimToNull(raw);
        if (normalized == null) {
            return null;
        }
        String key = normalized.toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        if (key.contains("citizen")) {
            return DocumentTypeEnum.CITIZENSHIP;
        }
        if (key.contains("passport")) {
            return DocumentTypeEnum.PASSPORT;
        }
        if (key.contains("license") || key.contains("licence") || key.contains("driving")) {
            return DocumentTypeEnum.DRIVING_LICENSE;
        }
        if (key.contains("pan")) {
            return DocumentTypeEnum.PAN;
        }
        if (key.contains("aadhar") || key.contains("aadhaar")) {
            return DocumentTypeEnum.AADHAR;
        }
        if (key.contains("voter")) {
            return DocumentTypeEnum.VOTER_ID;
        }
        if (key.contains("birth")) {
            return DocumentTypeEnum.BIRTH_CERTIFICATE;
        }
        if (key.contains("marriage")) {
            return DocumentTypeEnum.MARRIAGE_CERTIFICATE;
        }
        if (key.contains("educat") || key.contains("degree") || key.contains("certificate")) {
            return DocumentTypeEnum.EDUCATIONAL_CERTIFICATE;
        }
        if (key.contains("experience")) {
            return DocumentTypeEnum.EXPERIENCE_CERTIFICATE;
        }
        if (key.contains("contract")) {
            return DocumentTypeEnum.EMPLOYMENT_CONTRACT;
        }
        if (key.contains("offer")) {
            return DocumentTypeEnum.OFFER_LETTER;
        }
        if (key.contains("tax")) {
            return DocumentTypeEnum.TAX_FORM;
        }
        if (key.contains("payslip") || key.contains("pay_slip")) {
            return DocumentTypeEnum.PAYSLIP;
        }
        if (key.contains("photo") || key.contains("signature") || key.contains("other")) {
            return DocumentTypeEnum.OTHER;
        }
        return null;
    }

    private static String buildDocumentRemarks(Document source) {
        List<String> parts = new ArrayList<>();
        addPart(parts, "name", source.getName());
        addPart(parts, "nationality", source.getNationality());
        addPart(parts, "issuePlace", source.getIssuePlace());
        addPart(parts, "issueCounrtry", source.getIssueCounrtry());
        addPart(parts, "description", source.getDescription());
        if (source.getLicenseType() != null) {
            parts.add("licenseType=" + source.getLicenseType().name());
        }
        if (source.getDocumentType() != null) {
            parts.add("documentType=" + source.getDocumentType().name());
        }
        addPart(parts, "type", source.getType());
        return parts.isEmpty() ? null : String.join("; ", parts);
    }

    private static void addPart(List<String> parts, String label, String value) {
        String trimmed = OrgMigrationMapper.trimToNull(value);
        if (trimmed != null) {
            parts.add(label + "=" + trimmed);
        }
    }

    private static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
