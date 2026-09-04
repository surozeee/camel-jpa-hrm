package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Employee;
import com.jojolaptech.camel.model.mysql.EmployeeAddress;
import com.jojolaptech.camel.model.mysql.EmployeeAward;
import com.jojolaptech.camel.model.mysql.EmployeeContact;
import com.jojolaptech.camel.model.mysql.EmployeeEducation;
import com.jojolaptech.camel.model.mysql.EmployeeExperience;
import com.jojolaptech.camel.model.mysql.EmployeeHealth;
import com.jojolaptech.camel.model.mysql.EmployeeInsurance;
import com.jojolaptech.camel.model.mysql.EmployeeLanguage;
import com.jojolaptech.camel.model.mysql.EmployeePublication;
import com.jojolaptech.camel.model.mysql.EmployeeSeminar;
import com.jojolaptech.camel.model.mysql.EmployeeSkill;
import com.jojolaptech.camel.model.mysql.EmployeeTraining;
import com.jojolaptech.camel.model.mysql.EmploymentSuspension;
import com.jojolaptech.camel.model.mysql.Family;
import com.jojolaptech.camel.model.mysql.JobDescription;
import com.jojolaptech.camel.model.mysql.JobTitle;
import com.jojolaptech.camel.model.mysql.enums.AddressType;
import com.jojolaptech.camel.model.mysql.enums.EmployeeRelation;
import com.jojolaptech.camel.model.mysql.enums.HealthCondition;
import com.jojolaptech.camel.model.mysql.enums.LanguageLevel;
import com.jojolaptech.camel.model.mysql.enums.PremiumFrequency;
import com.jojolaptech.camel.model.mysql.enums.PublicationType;
import com.jojolaptech.camel.model.mysql.enums.SeminarFundedBy;
import com.jojolaptech.camel.model.postgres.company.EmployeeAddressEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeAwardEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeDesignationEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeDetailEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEducationEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeFamilyDetailEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeHealthEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeInsuranceEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeJobDescriptionEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeLanguageEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeePublicationEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeSeminarEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeSkillEntity;
import com.jojolaptech.camel.model.postgres.company.EmploymentSuspensionEntity;
import com.jojolaptech.camel.model.postgres.company.ExperienceEntity;
import com.jojolaptech.camel.model.postgres.company.SkillEntity;
import com.jojolaptech.camel.model.postgres.company.TrainingEntity;
import com.jojolaptech.camel.model.postgres.company.enums.AddressTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.EmployeeHealthStatusEnum;
import com.jojolaptech.camel.model.postgres.company.enums.EmployeeInsuranceStatusEnum;
import com.jojolaptech.camel.model.postgres.company.enums.EmployeeInsuranceTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.EmployeeSkillProficiencyEnum;
import com.jojolaptech.camel.model.postgres.company.enums.HealthConditionEnum;
import com.jojolaptech.camel.model.postgres.company.enums.JobDescriptionLevelEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LanguageProficiencyEnum;
import com.jojolaptech.camel.model.postgres.company.enums.PremiumFrequencyEnum;
import com.jojolaptech.camel.model.postgres.company.enums.PublicationTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.RelationshipEnum;
import com.jojolaptech.camel.model.postgres.company.enums.SeminarFundedByEnum;
import com.jojolaptech.camel.model.postgres.enums.CountryEnum;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class EmployeeProfileMigrationMapper {

    /** Synthetic mysql_id base for addresses migrated from employee master columns. */
    static final long EMPLOYEE_MASTER_ADDRESS_MYSQL_ID_OFFSET = 9_000_000_000_000L;

    private EmployeeProfileMigrationMapper() {}

    static EmployeeAddressEntity fromEmployeeAddress(EmployeeAddress source, java.util.UUID employeeId) {
        String street = firstNonBlank(
                OrgMigrationMapper.trimToNull(source.getStreet()),
                OrgMigrationMapper.trimToNull(source.getAddress()));
        if (street == null) {
            return null;
        }
        EmployeeAddressEntity address = EmployeeAddressEntity.builder()
                .mysqlId(source.getId())
                .employeeId(employeeId)
                .addressType(mapAddressType(source.getAddressType()))
                .streetAddress(street)
                .streetAddress2(OrgMigrationMapper.trimToNull(source.getLocality()))
                .ward(OrgMigrationMapper.trimToNull(source.getWardNo()))
                .postalCode(OrgMigrationMapper.trimToNull(source.getZipcode()))
                .country(resolveCountry(source.getNation()))
                .additionalInfo(buildAddressAdditionalInfo(source))
                .build();
        address.setStatus(StatusEnum.ACTIVE);
        return address;
    }

    static List<EmployeeAddressEntity> fromEmployeeMaster(Employee source, java.util.UUID employeeId) {
        List<EmployeeAddressEntity> addresses = new ArrayList<>();
        addMasterAddress(addresses, source, employeeId, source.getPermanentAdd(), AddressTypeEnum.PERMANENT, 1L);
        addMasterAddress(addresses, source, employeeId, source.getTemperoryAdd(), AddressTypeEnum.TEMPORARY, 2L);
        return addresses;
    }

    static EmployeeEducationEntity fromEmployeeEducation(EmployeeEducation source, java.util.UUID employeeId) {
        String institution = OrgMigrationMapper.trimToNull(source.getName_of_institute());
        String level = OrgMigrationMapper.trimToNull(source.getLevel());
        if (institution == null || level == null) {
            return null;
        }
        EmployeeEducationEntity education = EmployeeEducationEntity.builder()
                .mysqlId(source.getId())
                .employeeId(employeeId)
                .institution(institution)
                .educationLevel(level)
                .fieldOfStudy(firstNonBlank(
                        OrgMigrationMapper.trimToNull(source.getFaculty()),
                        OrgMigrationMapper.trimToNull(source.getCourseName()),
                        OrgMigrationMapper.trimToNull(source.getMajorSubject())))
                .startDate(toLocalDate(source.getDates()))
                .endDate(toLocalDate(source.getPassedYear()))
                .isCurrent(source.getIsRunning())
                .grade(OrgMigrationMapper.trimToNull(source.getGradePercentage()))
                .remarks(buildEducationRemarks(source))
                .build();
        education.setStatus(StatusEnum.ACTIVE);
        return education;
    }

    static EmployeeFamilyDetailEntity fromFamily(Family source, java.util.UUID employeeId) {
        String fullName = OrgMigrationMapper.trimToNull(source.getName());
        if (fullName == null) {
            return null;
        }
        EmployeeFamilyDetailEntity family = EmployeeFamilyDetailEntity.builder()
                .mysqlId(source.getId())
                .employeeId(employeeId)
                .fullName(fullName)
                .dateOfBirth(toLocalDate(source.getDob()))
                .relationship(mapRelationship(source.getRelation()))
                .contactNumber(OrgMigrationMapper.trimToNull(source.getContactNumber()))
                .occupation(OrgMigrationMapper.trimToNull(source.getOccupation()))
                .isDependent(source.getIsDependent())
                .remarks(buildFamilyRemarks(source))
                .build();
        family.setStatus(StatusEnum.ACTIVE);
        return family;
    }

    static ExperienceEntity fromEmployeeExperience(EmployeeExperience source, UUID employeeId) {
        LocalDate joinDate = toLocalDate(source.getStart());
        if (joinDate == null) {
            return null;
        }
        String companyName = firstNonBlank(
                OrgMigrationMapper.trimToNull(source.getName()), "Unknown company");
        String responsibilities = truncate(OrgMigrationMapper.trimToNull(source.getJobResponsibility()), 1000);
        ExperienceEntity entity = ExperienceEntity.builder()
                .mysqlId(source.getId())
                .employeeId(employeeId)
                .companyName(companyName)
                .designation(OrgMigrationMapper.trimToNull(source.getPosition()))
                .joinDate(joinDate)
                .leaveDate(toLocalDate(source.getEnd()))
                .isCurrentJob(source.getEnd() == null)
                .jobDescription(responsibilities)
                .responsibilities(responsibilities)
                .location(OrgMigrationMapper.trimToNull(source.getAddress()))
                .reasonForLeaving(truncate(OrgMigrationMapper.trimToNull(source.getReasonForLeaving()), 500))
                .remarks(truncate(buildExperienceRemarks(source), 500))
                .build();
        entity.setStatus(StatusEnum.ACTIVE);
        return entity;
    }

    static EmployeeAwardEntity fromEmployeeAward(EmployeeAward source, UUID employeeId) {
        String award = OrgMigrationMapper.trimToNull(source.getAward());
        String awardedBy = OrgMigrationMapper.trimToNull(source.getAwardedby());
        if (award == null || awardedBy == null) {
            return null;
        }
        EmployeeAwardEntity entity = EmployeeAwardEntity.builder()
                .mysqlId(source.getId())
                .employeeId(employeeId)
                .award(award)
                .awardedBy(awardedBy)
                .awardDate(toLocalDate(source.getDate()))
                .build();
        entity.setStatus(StatusEnum.ACTIVE);
        return entity;
    }

    static EmployeeLanguageEntity fromEmployeeLanguage(EmployeeLanguage source, UUID employeeId) {
        String name = OrgMigrationMapper.trimToNull(source.getName());
        if (name == null) {
            return null;
        }
        EmployeeLanguageEntity entity = EmployeeLanguageEntity.builder()
                .mysqlId(source.getId())
                .employeeId(employeeId)
                .name(name)
                .speaking(mapLanguageProficiency(source.getSpeaking()))
                .reading(mapLanguageProficiency(source.getReading()))
                .writing(mapLanguageProficiency(source.getWriting()))
                .listening(mapLanguageProficiency(source.getListining()))
                .remarks(truncate(part("document", source.getMyFile()), 500))
                .build();
        entity.setStatus(StatusEnum.ACTIVE);
        return entity;
    }

    static EmployeeSeminarEntity fromEmployeeSeminar(EmployeeSeminar source, UUID employeeId) {
        String name = OrgMigrationMapper.trimToNull(source.getName());
        if (name == null) {
            return null;
        }
        EmployeeSeminarEntity entity = EmployeeSeminarEntity.builder()
                .mysqlId(source.getId())
                .employeeId(employeeId)
                .name(name)
                .startDate(toLocalDate(source.getStartDate()))
                .endDate(toLocalDate(source.getEndDate()))
                .organizer(OrgMigrationMapper.trimToNull(source.getOrganizer()))
                .place(OrgMigrationMapper.trimToNull(source.getPlace()))
                .country(OrgMigrationMapper.trimToNull(source.getCountry()))
                .fundedBy(mapSeminarFundedBy(source.getSeminarFundedBy()))
                .remarks(truncate(part("document", source.getMyFile()), 500))
                .build();
        entity.setStatus(StatusEnum.ACTIVE);
        return entity;
    }

    static EmployeePublicationEntity fromEmployeePublication(EmployeePublication source, UUID employeeId) {
        String publicationName = OrgMigrationMapper.trimToNull(source.getPublicationName());
        if (publicationName == null) {
            return null;
        }
        EmployeePublicationEntity entity = EmployeePublicationEntity.builder()
                .mysqlId(source.getId())
                .employeeId(employeeId)
                .publicationName(publicationName)
                .publicationType(mapPublicationType(source.getPublicationType()))
                .publisher(OrgMigrationMapper.trimToNull(source.getPublisher()))
                .country(OrgMigrationMapper.trimToNull(source.getCountry()))
                .publishedDate(toLocalDate(source.getPublishedDate()))
                .remarks(truncate(part("document", source.getMyFile()), 500))
                .build();
        entity.setStatus(StatusEnum.ACTIVE);
        return entity;
    }

    static EmployeeHealthEntity fromEmployeeHealth(EmployeeHealth source, UUID employeeId) {
        EmployeeHealthEntity entity = EmployeeHealthEntity.builder()
                .mysqlId(source.getId())
                .employeeId(employeeId)
                .healthStatus(mapHealthStatus(source.getHealthStatus()))
                .healthCondition(mapHealthCondition(source.getHealthCondition()))
                .diagnosed(Boolean.TRUE.equals(source.getDiagnosed()))
                .diagnosedDate(toLocalDate(source.getDiagonosedDate()))
                .hospital(OrgMigrationMapper.trimToNull(source.getHospital()))
                .doctorName(OrgMigrationMapper.trimToNull(source.getDoctorName()))
                .ongoingTreatment(Boolean.TRUE.equals(source.getOnGoingTreatment()))
                .hospitalAddress(truncate(OrgMigrationMapper.trimToNull(source.getHospitalAddress()), 500))
                .doctorNumber(OrgMigrationMapper.trimToNull(source.getDoctorNumber()))
                .remarks(truncate(buildHealthRemarks(source), 500))
                .build();
        entity.setStatus(StatusEnum.ACTIVE);
        return entity;
    }

    static TrainingEntity fromEmployeeTraining(EmployeeTraining source, UUID employeeId) {
        LocalDate startDate = toLocalDate(source.getStartDate());
        String trainingName = OrgMigrationMapper.trimToNull(source.getName());
        if (startDate == null || trainingName == null) {
            return null;
        }
        TrainingEntity entity = TrainingEntity.builder()
                .mysqlId(source.getId())
                .employeeId(employeeId)
                .trainingName(trainingName)
                .trainingProvider(OrgMigrationMapper.trimToNull(source.getInstituteName()))
                .trainingType(source.getTrainingType() != null ? source.getTrainingType().name() : null)
                .startDate(startDate)
                .endDate(toLocalDate(source.getEndDate()))
                .description(truncate(buildTrainingRemarks(source), 1000))
                .remarks(truncate(buildTrainingRemarks(source), 500))
                .build();
        entity.setStatus(StatusEnum.ACTIVE);
        return entity;
    }

    static EmployeeJobDescriptionEntity fromJobDescription(JobDescription source, UUID employeeId) {
        String position = OrgMigrationMapper.trimToNull(source.getPosition());
        if (position == null) {
            return null;
        }
        EmployeeJobDescriptionEntity entity = EmployeeJobDescriptionEntity.builder()
                .mysqlId(source.getId())
                .employeeId(employeeId)
                .effectiveDate(toLocalDate(source.getEffectiveDate()))
                .careerLevel(mapJobDescriptionLevel(source.getJobLevel()))
                .position(position)
                .functionalTitle(OrgMigrationMapper.trimToNull(source.getFunctionaltitle()))
                .location(OrgMigrationMapper.trimToNull(source.getLocation()))
                .description(OrgMigrationMapper.trimToNull(source.getDescription()))
                .branchName(OrgMigrationMapper.trimToNull(source.getBranch()))
                .previousBranchName(OrgMigrationMapper.trimToNull(source.getPrevBranch()))
                .active(source.getIsActive() == null || source.getIsActive())
                .build();
        entity.setStatus(StatusEnum.ACTIVE);
        return entity;
    }

    static EmploymentSuspensionEntity fromEmploymentSuspension(EmploymentSuspension source, UUID employeeId) {
        LocalDate fromDate = toLocalDate(source.getFromDate());
        if (fromDate == null) {
            return null;
        }
        EmploymentSuspensionEntity entity = EmploymentSuspensionEntity.builder()
                .mysqlId(source.getId())
                .employeeId(employeeId)
                .fromDate(fromDate)
                .toDate(toLocalDate(source.getToDate()))
                .unpaid(Boolean.TRUE)
                .build();
        entity.setStatus(StatusEnum.ACTIVE);
        return entity;
    }

    static EmployeeInsuranceEntity fromEmployeeInsurance(EmployeeInsurance source, UUID employeeId) {
        String providerName = source.getInsuranceCompany() != null
                ? OrgMigrationMapper.trimToNull(source.getInsuranceCompany().getName())
                : null;
        if (providerName == null) {
            providerName = "Unknown provider";
        }
        String policyNumber = OrgMigrationMapper.trimToNull(source.getPolicyNumber());
        if (policyNumber == null) {
            policyNumber = "MIGRATE-" + source.getId();
        }
        LocalDate endDate = toLocalDate(source.getPolicyEndDate());
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        EmployeeInsuranceStatusEnum status = (endDate == null || !endDate.isBefore(today))
                ? EmployeeInsuranceStatusEnum.ACTIVE
                : EmployeeInsuranceStatusEnum.EXPIRED;

        EmployeeInsuranceEntity entity = EmployeeInsuranceEntity.builder()
                .mysqlId(source.getId())
                .employeeId(employeeId)
                .policyType(Boolean.TRUE.equals(source.getIsHealthInsurance())
                        ? EmployeeInsuranceTypeEnum.HEALTH
                        : EmployeeInsuranceTypeEnum.OTHER)
                .policyNumber(policyNumber)
                .providerName(providerName)
                .coverageAmount(toBigDecimal(source.getPolicyAmount()))
                .premiumAmount(toBigDecimal(source.getPremiumAmount()))
                .premiumFrequency(mapPremiumFrequency(source.getPremiumFrequency()))
                .startDate(toLocalDate(source.getPolicyStartDate()))
                .endDate(endDate)
                .policyStatus(status)
                .remarks(truncate(buildInsuranceRemarks(source), 500))
                .build();
        entity.setStatus(StatusEnum.ACTIVE);
        return entity;
    }

    static SkillEntity newSkill(UUID companyId, String skillName, Long mysqlIdHint) {
        String name = OrgMigrationMapper.trimToNull(skillName);
        if (name == null || companyId == null) {
            return null;
        }
        SkillEntity entity = SkillEntity.builder()
                .mysqlId(mysqlIdHint)
                .companyId(companyId)
                .name(name)
                .code(skillCode(name))
                .build();
        entity.setStatus(StatusEnum.ACTIVE);
        return entity;
    }

    static EmployeeSkillEntity fromEmployeeSkill(
            EmployeeSkill source, UUID employeeId, UUID skillId) {
        if (skillId == null) {
            return null;
        }
        EmployeeSkillEntity entity = EmployeeSkillEntity.builder()
                .mysqlId(source.getId())
                .employeeId(employeeId)
                .skillId(skillId)
                .proficiency(mapSkillProficiency(source.getLevel()))
                .build();
        entity.setStatus(StatusEnum.ACTIVE);
        return entity;
    }

    static EmployeeDesignationEntity fromJobTitle(JobTitle source, UUID branchId) {
        String name = OrgMigrationMapper.trimToNull(source.getTitleOfJob());
        if (name == null || branchId == null) {
            return null;
        }
        EmployeeDesignationEntity entity = EmployeeDesignationEntity.builder()
                .mysqlId(source.getId())
                .name(name)
                .branchId(branchId)
                .build();
        entity.setStatus(StatusEnum.ACTIVE);
        return entity;
    }

    static EmployeeDetailEntity newEmployeeDetail(
            EmployeeContact source, UUID employeeId) {
        String name = OrgMigrationMapper.trimToNull(source.getEmergencyContactName());
        String phone = firstNonBlank(
                OrgMigrationMapper.trimToNull(source.getEmergencyContactPhone()),
                OrgMigrationMapper.trimToNull(source.getEmergencyContactMobile()),
                OrgMigrationMapper.trimToNull(source.getMobile()),
                OrgMigrationMapper.trimToNull(source.getPhone()));
        if (name == null && phone == null) {
            return null;
        }
        EmployeeDetailEntity entity = EmployeeDetailEntity.builder()
                .mysqlId(source.getId())
                .employeeId(employeeId)
                .emergencyContactName(name)
                .emergencyContactPhone(phone)
                .emergencyContactRelation(OrgMigrationMapper.trimToNull(source.getEmergencyContactRelation()))
                .build();
        entity.setStatus(StatusEnum.ACTIVE);
        return entity;
    }

    static void applyEmergencyContact(EmployeeDetailEntity detail, EmployeeContact source) {
        if (detail == null || source == null) {
            return;
        }
        String name = OrgMigrationMapper.trimToNull(source.getEmergencyContactName());
        String phone = firstNonBlank(
                OrgMigrationMapper.trimToNull(source.getEmergencyContactPhone()),
                OrgMigrationMapper.trimToNull(source.getEmergencyContactMobile()),
                OrgMigrationMapper.trimToNull(source.getMobile()),
                OrgMigrationMapper.trimToNull(source.getPhone()));
        String relation = OrgMigrationMapper.trimToNull(source.getEmergencyContactRelation());
        if (isBlank(detail.getEmergencyContactName()) && name != null) {
            detail.setEmergencyContactName(name);
        }
        if (isBlank(detail.getEmergencyContactPhone()) && phone != null) {
            detail.setEmergencyContactPhone(phone);
        }
        if (isBlank(detail.getEmergencyContactRelation()) && relation != null) {
            detail.setEmergencyContactRelation(relation);
        }
        if (detail.getMysqlId() == null) {
            detail.setMysqlId(source.getId());
        }
    }

    static boolean isEmergencyContact(EmployeeContact source) {
        if (source == null) {
            return false;
        }
        if (source.getContactType() == com.jojolaptech.camel.model.mysql.enums.ContactType.Emergency) {
            return true;
        }
        return OrgMigrationMapper.trimToNull(source.getEmergencyContactName()) != null
                || OrgMigrationMapper.trimToNull(source.getEmergencyContactPhone()) != null
                || OrgMigrationMapper.trimToNull(source.getEmergencyContactMobile()) != null;
    }

    static long masterAddressMysqlId(long employeeMysqlId, long suffix) {
        return EMPLOYEE_MASTER_ADDRESS_MYSQL_ID_OFFSET + employeeMysqlId * 10L + suffix;
    }

    private static void addMasterAddress(
            List<EmployeeAddressEntity> addresses,
            Employee source,
            java.util.UUID employeeId,
            String streetValue,
            AddressTypeEnum addressType,
            long suffix) {
        String street = OrgMigrationMapper.trimToNull(streetValue);
        if (street == null) {
            return;
        }
        EmployeeAddressEntity address = EmployeeAddressEntity.builder()
                .mysqlId(masterAddressMysqlId(source.getId(), suffix))
                .employeeId(employeeId)
                .addressType(addressType)
                .streetAddress(street)
                .country(CountryEnum.NP)
                .additionalInfo("Migrated from legacy employee." + (addressType == AddressTypeEnum.PERMANENT
                        ? "permanentAdd"
                        : "temperoryAdd"))
                .build();
        address.setStatus(StatusEnum.ACTIVE);
        addresses.add(address);
    }

    private static AddressTypeEnum mapAddressType(AddressType addressType) {
        if (addressType == null) {
            return AddressTypeEnum.PERMANENT;
        }
        return addressType == AddressType.Present ? AddressTypeEnum.TEMPORARY : AddressTypeEnum.PERMANENT;
    }

    private static RelationshipEnum mapRelationship(EmployeeRelation relation) {
        if (relation == null) {
            return RelationshipEnum.OTHER;
        }
        return switch (relation) {
            case Spouse -> RelationshipEnum.SPOUSE;
            case Father -> RelationshipEnum.FATHER;
            case Mother -> RelationshipEnum.MOTHER;
            case Son -> RelationshipEnum.SON;
            case Daughter -> RelationshipEnum.DAUGHTER;
            case Sister -> RelationshipEnum.SISTER;
            case Brother -> RelationshipEnum.BROTHER;
            case Uncle, Aunt -> RelationshipEnum.OTHER;
        };
    }

    private static LanguageProficiencyEnum mapLanguageProficiency(LanguageLevel level) {
        if (level == null) {
            return null;
        }
        return switch (level) {
            case Good -> LanguageProficiencyEnum.GOOD;
            case Excellent -> LanguageProficiencyEnum.EXCELLENT;
            case Average -> LanguageProficiencyEnum.AVERAGE;
        };
    }

    private static SeminarFundedByEnum mapSeminarFundedBy(SeminarFundedBy fundedBy) {
        if (fundedBy == null) {
            return null;
        }
        return switch (fundedBy) {
            case Company -> SeminarFundedByEnum.COMPANY;
            case Employee -> SeminarFundedByEnum.EMPLOYEE;
            case CompanyAndEmployee -> SeminarFundedByEnum.COMPANY_AND_EMPLOYEE;
        };
    }

    private static PublicationTypeEnum mapPublicationType(PublicationType type) {
        if (type == null) {
            return PublicationTypeEnum.OTHER;
        }
        return switch (type) {
            case Article -> PublicationTypeEnum.ARTICLE;
            case Book -> PublicationTypeEnum.BOOK;
            case Report -> PublicationTypeEnum.REPORT;
            case Research -> PublicationTypeEnum.RESEARCH;
        };
    }

    private static EmployeeHealthStatusEnum mapHealthStatus(
            com.jojolaptech.camel.model.mysql.enums.EmployeeHealthStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case EXCELLENT -> EmployeeHealthStatusEnum.EXCELLENT;
            case GOOD -> EmployeeHealthStatusEnum.GOOD;
            case POOR -> EmployeeHealthStatusEnum.POOR;
        };
    }

    private static HealthConditionEnum mapHealthCondition(HealthCondition condition) {
        if (condition == null) {
            return null;
        }
        return switch (condition) {
            case Contact -> HealthConditionEnum.CONTACT;
            case Cured -> HealthConditionEnum.CURED;
            case Good -> HealthConditionEnum.GOOD;
            case Severe -> HealthConditionEnum.SEVERE;
            case Symtptom -> HealthConditionEnum.SYMPTOM;
        };
    }

    private static JobDescriptionLevelEnum mapJobDescriptionLevel(
            com.jojolaptech.camel.model.mysql.enums.JobLevel level) {
        if (level == null) {
            return null;
        }
        return switch (level) {
            case JUNIOR -> JobDescriptionLevelEnum.JUNIOR;
            case MID -> JobDescriptionLevelEnum.MID;
            case SENIOR -> JobDescriptionLevelEnum.SENIOR;
        };
    }

    private static PremiumFrequencyEnum mapPremiumFrequency(PremiumFrequency frequency) {
        if (frequency == null) {
            return null;
        }
        return switch (frequency) {
            case Yearly -> PremiumFrequencyEnum.ANNUAL;
            case HalfYearly -> PremiumFrequencyEnum.ONE_TIME;
            case Quarterly -> PremiumFrequencyEnum.QUARTERLY;
        };
    }

    private static EmployeeSkillProficiencyEnum mapSkillProficiency(String level) {
        String normalized = OrgMigrationMapper.trimToNull(level);
        if (normalized == null) {
            return EmployeeSkillProficiencyEnum.BEGINNER;
        }
        String key = normalized.toUpperCase(Locale.ROOT).replace(' ', '_');
        return switch (key) {
            case "BEGINNER", "BASIC", "NOVICE" -> EmployeeSkillProficiencyEnum.BEGINNER;
            case "INTERMEDIATE", "MEDIUM", "AVERAGE" -> EmployeeSkillProficiencyEnum.INTERMEDIATE;
            case "ADVANCED", "HIGH" -> EmployeeSkillProficiencyEnum.ADVANCED;
            case "EXPERT", "MASTER" -> EmployeeSkillProficiencyEnum.EXPERT;
            default -> EmployeeSkillProficiencyEnum.BEGINNER;
        };
    }

    private static String skillCode(String name) {
        String slug = name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (slug.isBlank()) {
            int hash = Math.abs(name.hashCode());
            return "SK-" + Integer.toHexString(hash);
        }
        String code = "SK-" + slug;
        return code.length() <= 64 ? code : "SK-" + Integer.toHexString(
                java.util.Arrays.hashCode(name.getBytes(StandardCharsets.UTF_8)));
    }

    private static CountryEnum resolveCountry(String nation) {
        if (nation == null || nation.isBlank()) {
            return CountryEnum.NP;
        }
        String normalized = nation.trim().toUpperCase(Locale.ROOT);
        if (normalized.equals("NP") || normalized.contains("NEPAL")) {
            return CountryEnum.NP;
        }
        try {
            return CountryEnum.fromCode(normalized.length() == 2 ? normalized : normalized.substring(0, 2));
        } catch (IllegalArgumentException ignored) {
            return CountryEnum.NP;
        }
    }

    private static String buildAddressAdditionalInfo(EmployeeAddress source) {
        return Stream.of(
                        part("state", source.getState()),
                        part("zone", source.getZone()),
                        part("district", source.getDistrict()),
                        part("vdcMunicipality", source.getVdcMunicipality()),
                        part("houseNo", source.getHouseNo()))
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining("; "));
    }

    private static String buildEducationRemarks(EmployeeEducation source) {
        return Stream.of(
                        part("board", source.getBoard()),
                        part("division", source.getDivision()),
                        part("country", source.getCountry()),
                        part("courseName", source.getCourseName()),
                        part("document", source.getMyFile()))
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining("; "));
    }

    private static String buildFamilyRemarks(Family source) {
        return Stream.of(
                        source.getTitle() != null ? "title=" + source.getTitle().name() : null,
                        source.getGender() != null ? "gender=" + source.getGender().name() : null,
                        source.getBloodGroup() != null ? "bloodGroup=" + source.getBloodGroup().name() : null,
                        source.getAge() != null ? "age=" + source.getAge() : null,
                        part("nationality", source.getNationality()),
                        source.getDocumentType() != null ? "documentType=" + source.getDocumentType().name() : null,
                        part("note", source.getNote()))
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining("; "));
    }

    private static String buildExperienceRemarks(EmployeeExperience source) {
        return Stream.of(
                        part("note", source.getNote()),
                        source.getIsVerified() != null ? "verified=" + source.getIsVerified() : null,
                        part("file", source.getMyFile()))
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining("; "));
    }

    private static String buildHealthRemarks(EmployeeHealth source) {
        return Stream.of(
                        part("detail", source.getDetail()),
                        part("healthFile", source.getHealthFile()))
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining("; "));
    }

    private static String buildTrainingRemarks(EmployeeTraining source) {
        return Stream.of(
                        part("place", source.getPlace()),
                        part("country", source.getCountry()),
                        source.getTrainingFundedBy() != null
                                ? "fundedBy=" + source.getTrainingFundedBy().name()
                                : null,
                        part("file", source.getMyFile()))
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining("; "));
    }

    private static String buildInsuranceRemarks(EmployeeInsurance source) {
        return Stream.of(
                        part("financialYear", source.getFinancialYear()),
                        part("notes", source.getNotes()),
                        part("periodicAmount", source.getPeriodicAmount()),
                        source.getPremiumPaidBy() != null
                                ? "premiumPaidBy=" + source.getPremiumPaidBy().name()
                                : null,
                        source.getAmountPaidByCompany() != null
                                ? "amountPaidByCompany=" + source.getAmountPaidByCompany()
                                : null,
                        source.getAmountPaidByEmployee() != null
                                ? "amountPaidByEmployee=" + source.getAmountPaidByEmployee()
                                : null)
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining("; "));
    }

    private static String part(String key, String value) {
        String trimmed = OrgMigrationMapper.trimToNull(value);
        return trimmed == null ? null : key + "=" + trimmed;
    }

    private static String firstNonBlank(String first, String second) {
        return first != null ? first : second;
    }

    private static String firstNonBlank(String first, String second, String third) {
        if (first != null) {
            return first;
        }
        return second != null ? second : third;
    }

    private static String firstNonBlank(String first, String second, String third, String fourth) {
        if (first != null) {
            return first;
        }
        if (second != null) {
            return second;
        }
        return third != null ? third : fourth;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private static BigDecimal toBigDecimal(Integer value) {
        return value == null ? null : BigDecimal.valueOf(value.longValue());
    }

    private static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
