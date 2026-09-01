package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Employee;
import com.jojolaptech.camel.model.mysql.EmployeeAddress;
import com.jojolaptech.camel.model.mysql.EmployeeEducation;
import com.jojolaptech.camel.model.mysql.Family;
import com.jojolaptech.camel.model.mysql.enums.AddressType;
import com.jojolaptech.camel.model.mysql.enums.EmployeeRelation;
import com.jojolaptech.camel.model.postgres.company.EmployeeAddressEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEducationEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeFamilyDetailEntity;
import com.jojolaptech.camel.model.postgres.company.enums.AddressTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.RelationshipEnum;
import com.jojolaptech.camel.model.postgres.enums.CountryEnum;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

    private static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
