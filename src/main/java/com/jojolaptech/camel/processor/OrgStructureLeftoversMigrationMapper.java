package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyEmployeeContract;
import com.jojolaptech.camel.model.mysql.EmployeeJob;
import com.jojolaptech.camel.model.mysql.EmployeeJobLevel;
import com.jojolaptech.camel.model.mysql.JobPosition;
import com.jojolaptech.camel.model.mysql.JobStatus;
import com.jojolaptech.camel.model.mysql.enums.HireMethod;
import com.jojolaptech.camel.model.mysql.enums.JobStatusStatus;
import com.jojolaptech.camel.model.postgres.company.EmployeeContractEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEmploymentHistoryEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeDesignationEntity;
import com.jojolaptech.camel.model.postgres.company.GradeEntity;
import com.jojolaptech.camel.model.postgres.company.enums.EmployeeContractTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.EmploymentHistorySourceEnum;
import com.jojolaptech.camel.model.postgres.company.enums.EmploymentMovementTypeEnum;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Mapping helpers for org-structure leftover steps 22za–22ze.
 */
final class OrgStructureLeftoversMigrationMapper {

    static final long JOB_STATUS_MYSQL_ID_OFFSET = 16_000_000_000_000L;
    static final long EMPLOYEE_JOB_MYSQL_ID_OFFSET = 17_000_000_000_000L;
    static final long JOB_POSITION_MYSQL_ID_OFFSET = 18_000_000_000_000L;

    private OrgStructureLeftoversMigrationMapper() {}

    static EmployeeEmploymentHistoryEntity fromEmployeeJobLevel(
            EmployeeJobLevel source, EmployeeEntity employee, GradeEntity grade) {
        LocalDate effectiveFrom = toLocalDate(source.getStartDate());
        if (effectiveFrom == null) {
            return null;
        }
        String gradeName = grade != null ? grade.getName() : null;
        if (gradeName == null && source.getJobLevel() != null) {
            gradeName = source.getJobLevel().getJobLevelName();
        }
        return EmployeeEmploymentHistoryEntity.builder()
                .mysqlId(source.getId())
                .employeeId(employee.getId())
                .movementType(EmploymentMovementTypeEnum.GRADE_CHANGE)
                .source(EmploymentHistorySourceEnum.MANUAL)
                .effectiveFrom(effectiveFrom)
                .effectiveTo(toLocalDate(source.getEndDate()))
                .gradeId(grade != null ? grade.getId() : null)
                .gradeName(gradeName)
                .gradeCode(grade != null ? grade.getCode() : null)
                .branchId(employee.getBranchId())
                .departmentId(employee.getDepartmentId())
                .build();
    }

    static EmployeeEmploymentHistoryEntity fromJobStatus(JobStatus source, EmployeeEntity employee) {
        LocalDate effectiveFrom = toLocalDate(source.getFromDate());
        if (effectiveFrom == null) {
            return null;
        }
        return EmployeeEmploymentHistoryEntity.builder()
                .mysqlId(JOB_STATUS_MYSQL_ID_OFFSET + source.getId())
                .employeeId(employee.getId())
                .movementType(EmploymentMovementTypeEnum.EMPLOYMENT_TYPE_CHANGE)
                .source(EmploymentHistorySourceEnum.MANUAL)
                .effectiveFrom(effectiveFrom)
                .effectiveTo(toLocalDate(source.getToDate()))
                .employmentTypeName(mapJobStatusLabel(source.getJobStatusStatus()))
                .branchId(employee.getBranchId())
                .departmentId(employee.getDepartmentId())
                .remarks(trimToNull(source.getNoteText()))
                .build();
    }

    static EmployeeEmploymentHistoryEntity fromEmployeeJob(
            EmployeeJob source,
            EmployeeEntity employee,
            EmployeeDesignationEntity designation,
            Long mysqlId) {
        LocalDate effectiveFrom = toLocalDate(source.getStartdate());
        if (effectiveFrom == null) {
            return null;
        }
        String designationName = designation != null ? designation.getName() : null;
        if (designationName == null && source.getJob() != null) {
            designationName = source.getJob().getTitleOfJob();
        }
        String remarks = null;
        if (designation == null && source.getJob() != null) {
            remarks = "Unmigrated jobTitle mysqlId=" + source.getJob().getId()
                    + (designationName != null ? "; title=" + designationName : "");
        }
        return EmployeeEmploymentHistoryEntity.builder()
                .mysqlId(mysqlId)
                .employeeId(employee.getId())
                .movementType(EmploymentMovementTypeEnum.DESIGNATION_CHANGE)
                .source(EmploymentHistorySourceEnum.MANUAL)
                .effectiveFrom(effectiveFrom)
                .effectiveTo(toLocalDate(source.getEnddate()))
                .designationId(designation != null ? designation.getId() : null)
                .designationName(designationName)
                .branchId(employee.getBranchId())
                .departmentId(employee.getDepartmentId())
                .remarks(remarks)
                .build();
    }

    static EmployeeContractEntity fromJobPosition(JobPosition source, UUID employeeId) {
        LocalDate startDate = toLocalDate(source.getAppointmentHireDate());
        if (startDate == null) {
            startDate = toLocalDate(source.getAppointmentLetterDate());
        }
        if (startDate == null) {
            return null;
        }
        LocalDate endDate = toLocalDate(source.getHireContractEndDate());
        EmployeeContractTypeEnum contractType = mapHireMethodToContractType(source.getHireMethod(), endDate);
        return EmployeeContractEntity.builder()
                .mysqlId(JOB_POSITION_MYSQL_ID_OFFSET + source.getId())
                .employeeId(employeeId)
                .contractType(contractType)
                .contractTypeLabel(source.getHireMethod() != null ? source.getHireMethod().name() : null)
                .contractNumber(trimToNull(source.getAppointmentLetterNo()))
                .startDate(startDate)
                .endDate(endDate)
                .remarks(buildJobPositionRemarks(source))
                .build();
    }

    static EmployeeContractEntity fromCompanyEmployeeContract(CompanyEmployeeContract source, UUID employeeId) {
        LocalDate startDate = toLocalDate(source.getContractStartDate());
        if (startDate == null) {
            return null;
        }
        String rawType = trimToNull(source.getContractType());
        EmployeeContractTypeEnum contractType = mapContractTypeString(rawType);
        return EmployeeContractEntity.builder()
                .mysqlId(source.getId())
                .employeeId(employeeId)
                .contractType(contractType)
                .contractTypeLabel(rawType)
                .startDate(startDate)
                .endDate(toLocalDate(source.getContractEndDate()))
                .remarks(rawType != null && contractType == EmployeeContractTypeEnum.OTHER
                        ? "Source contractType=" + rawType
                        : null)
                .build();
    }

    static boolean shouldCreateJobPositionContract(JobPosition source) {
        return source.getAppointmentLetterNo() != null
                || source.getHireContractEndDate() != null
                || source.getAppointmentHireDate() != null;
    }

    static String mapJobStatusLabel(JobStatusStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case PartTime -> "Part Time";
            case FullTime -> "Full Time";
            case Contract -> "Contract";
            case Probation -> "Probation";
        };
    }

    static EmployeeContractTypeEnum mapHireMethodToContractType(HireMethod hireMethod, LocalDate endDate) {
        if (hireMethod != null) {
            String name = hireMethod.name().toLowerCase(Locale.ROOT);
            if (name.contains("contract")) {
                return EmployeeContractTypeEnum.CONTRACT;
            }
        }
        return endDate != null ? EmployeeContractTypeEnum.FIXED_TERM : EmployeeContractTypeEnum.PERMANENT;
    }

    static EmployeeContractTypeEnum mapContractTypeString(String raw) {
        if (raw == null || raw.isBlank()) {
            return EmployeeContractTypeEnum.OTHER;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        if (normalized.contains("permanent") || normalized.equals("full_time") || normalized.equals("fulltime")) {
            return EmployeeContractTypeEnum.PERMANENT;
        }
        if (normalized.contains("probation")) {
            return EmployeeContractTypeEnum.PROBATION;
        }
        if (normalized.contains("part_time") || normalized.contains("parttime") || normalized.equals("part")) {
            return EmployeeContractTypeEnum.PART_TIME;
        }
        if (normalized.contains("fixed") || normalized.contains("term")) {
            return EmployeeContractTypeEnum.FIXED_TERM;
        }
        if (normalized.contains("contract")) {
            return EmployeeContractTypeEnum.CONTRACT;
        }
        return EmployeeContractTypeEnum.OTHER;
    }

    private static String buildJobPositionRemarks(JobPosition source) {
        List<String> parts = new ArrayList<>();
        if (source.getHireMethod() != null) {
            parts.add("hireMethod=" + source.getHireMethod().name());
        }
        if (trimToNull(source.getReference()) != null) {
            parts.add("reference=" + source.getReference().trim());
        }
        LocalDate salaryCalc = toLocalDate(source.getSalaryCalculationDate());
        if (salaryCalc != null) {
            parts.add("salaryCalculationDate=" + salaryCalc);
        }
        LocalDate letterDate = toLocalDate(source.getAppointmentLetterDate());
        if (letterDate != null) {
            parts.add("appointmentLetterDate=" + letterDate);
        }
        if (parts.isEmpty()) {
            return null;
        }
        String joined = String.join("; ", parts);
        return joined.length() > 500 ? joined.substring(0, 500) : joined;
    }

    static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
