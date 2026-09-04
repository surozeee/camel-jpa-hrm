package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Applicant;
import com.jojolaptech.camel.model.mysql.ApplicantsTransaction;
import com.jojolaptech.camel.model.mysql.Employee;
import com.jojolaptech.camel.model.mysql.Evaluation;
import com.jojolaptech.camel.model.mysql.ScreeningAnswer;
import com.jojolaptech.camel.model.mysql.ScreeningQuestion;
import com.jojolaptech.camel.model.mysql.Stages;
import com.jojolaptech.camel.model.mysql.Vacancy;
import com.jojolaptech.camel.model.mysql.VacancyNewspaper;
import com.jojolaptech.camel.model.mysql.enums.Status;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentApplicationEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentApplicationScreeningAnswerEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentApplicationStatusHistoryEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentCandidateEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentInterviewStageEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentScreeningEvaluationEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentVacancyEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentVacancyPublicationEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentVacancyScreeningQuestionEntity;
import com.jojolaptech.camel.model.postgres.recruitment.enums.ApplicationStatusEnum;
import com.jojolaptech.camel.model.postgres.recruitment.enums.CandidateSourceEnum;
import com.jojolaptech.camel.model.postgres.recruitment.enums.PublicationChannelEnum;
import com.jojolaptech.camel.model.postgres.recruitment.enums.PublicationPostingStatusEnum;
import com.jojolaptech.camel.model.postgres.recruitment.enums.RecruitmentEmploymentTypeEnum;
import com.jojolaptech.camel.model.postgres.recruitment.enums.ScreeningEligibilityEnum;
import com.jojolaptech.camel.model.postgres.recruitment.enums.ScreeningQuestionTypeEnum;
import com.jojolaptech.camel.model.postgres.recruitment.enums.VacancyPriorityEnum;
import com.jojolaptech.camel.model.postgres.recruitment.enums.VacancyPublishScopeEnum;
import com.jojolaptech.camel.model.postgres.recruitment.enums.VacancyStatusEnum;
import com.jojolaptech.camel.model.postgres.recruitment.enums.WorkArrangementEnum;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Maps MySQL ATS → ERP {@code hrm_recruitment_*} (not pre-employment templates). */
public final class RecruitmentAtsMigrationMapper {

    /** One candidate per employee: avoids collide with application mysql_id. */
    public static final long CANDIDATE_MYSQL_ID_OFFSET = 25_000_000_000_000L;

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private RecruitmentAtsMigrationMapper() {}

    public static long candidateMysqlId(long employeeMysqlId) {
        return CANDIDATE_MYSQL_ID_OFFSET + employeeMysqlId;
    }

    public static RecruitmentVacancyEntity fromVacancy(
            Vacancy source, UUID companyId, UUID branchId, UUID hiringManagerEmployeeId) {
        if (source == null || source.getId() == null || companyId == null || branchId == null) {
            return null;
        }
        String title = blankToNull(source.getJobTitle());
        if (title == null) {
            return null;
        }
        StringBuilder benefits = new StringBuilder();
        appendKv(benefits, "budgeted", source.getBudgeted());
        appendKv(benefits, "hireType", source.getHireType());
        appendKv(benefits, "salaryType", source.getSalaryType());
        appendKv(benefits, "salaryCurrency", source.getSalaryCurrency());
        appendKv(benefits, "salaryPaymentType", source.getSalaryPaymentType());
        appendKv(benefits, "gender", source.getGender());
        appendKv(benefits, "minAge", source.getMinAge());
        appendKv(benefits, "maxAge", source.getMaxAge());
        appendKv(benefits, "maxExperience", source.getMaxExperience());
        appendKv(benefits, "reasonForVacancy", source.getReasonForVacancy());
        if (source.getJobCategory() != null && blankToNull(source.getJobCategory().getCategoryName()) != null) {
            appendKv(benefits, "jobCategory", source.getJobCategory().getCategoryName());
        }

        return RecruitmentVacancyEntity.builder()
                .mysqlId(source.getId())
                .vacancyCode(truncate("VAC-" + source.getId(), 64))
                .title(truncate(title, 255))
                .companyId(companyId)
                .branchId(branchId)
                .employmentType(mapEmploymentType(source.getJobType(), source.getHireType()))
                .openings(source.getNumberOfOpenings() != null && source.getNumberOfOpenings() > 0
                        ? source.getNumberOfOpenings()
                        : 1)
                .filledCount(0)
                .workLocation(truncate(blankToNull(source.getJobLocation()), 255))
                .workArrangement(WorkArrangementEnum.ON_SITE)
                .hiringManagerEmployeeId(hiringManagerEmployeeId)
                .jobDescription(blankToNull(source.getJobSpecification()))
                .responsibilities(blankToNull(source.getOtherSpecification()))
                .requiredQualifications(blankToNull(source.getEducationDescription()))
                .preferredQualifications(blankToNull(source.getPreferredQualification()))
                .requiredExperienceYears(source.getMinExperience())
                .salaryMin(money(source.getMinSalary()))
                .salaryMax(money(source.getMaxSalary()))
                .benefits(benefits.isEmpty() ? null : benefits.toString())
                .applicationDeadline(toLocalDate(source.getEndDate()))
                .expectedJoiningDate(toLocalDate(source.getTargetedJobStartDate()))
                .priority(VacancyPriorityEnum.MEDIUM)
                .publishScope(VacancyPublishScopeEnum.BOTH)
                .publishedAt(toLocalDate(source.getStartDate()))
                .vacancyStatus(mapVacancyStatus(source.getStatus()))
                .build();
    }

    public static RecruitmentVacancyPublicationEntity fromVacancyNewspaper(
            VacancyNewspaper source, RecruitmentVacancyEntity vacancy) {
        if (source == null || source.getId() == null || vacancy == null) {
            return null;
        }
        String outlet = blankToNull(source.getNewspaperName());
        if (outlet == null) {
            outlet = "Newspaper " + source.getId();
        }
        String path = blankToNull(source.getImagePath());
        String documentUrl = path != null
                ? path
                : "migrated://mysql-vacancy-newspaper/" + source.getId();
        return RecruitmentVacancyPublicationEntity.builder()
                .mysqlId(source.getId())
                .vacancy(vacancy)
                .companyId(vacancy.getCompanyId())
                .channel(PublicationChannelEnum.NEWSPAPER)
                .outletName(truncate(outlet, 255))
                .documentUrl(truncate(documentUrl, 1000))
                .postingStatus(PublicationPostingStatusEnum.LIVE)
                .remarks("Migrated from vacancyNewspaper (clipping binary not copied)")
                .build();
    }

    public static RecruitmentInterviewStageEntity fromStage(Stages source, RecruitmentVacancyEntity vacancy) {
        if (source == null || source.getId() == null || vacancy == null) {
            return null;
        }
        String name = blankToNull(source.getName());
        if (name == null) {
            name = "Stage " + source.getId();
        }
        StringBuilder desc = new StringBuilder();
        appendKv(desc, "acceptValue", source.getAcceptValue());
        appendKv(desc, "rejectValue", source.getRejectValue());
        appendKv(desc, "holdValue", source.getHoldValue());
        return RecruitmentInterviewStageEntity.builder()
                .mysqlId(source.getId())
                .vacancy(vacancy)
                .stageName(truncate(name, 120))
                .sequenceNo(source.getId() != null ? source.getId().intValue() : 0)
                .mandatoryFeedback(Boolean.FALSE)
                .description(desc.isEmpty() ? null : desc.toString())
                .build();
    }

    public static RecruitmentVacancyScreeningQuestionEntity fromScreeningQuestion(
            ScreeningQuestion source, RecruitmentVacancyEntity vacancy, int sortOrder) {
        if (source == null || source.getId() == null || vacancy == null) {
            return null;
        }
        String text = blankToNull(source.getQuestions());
        if (text == null) {
            return null;
        }
        List<String[]> options = new ArrayList<>();
        addOption(options, "A", source.getOption1());
        addOption(options, "B", source.getOption2());
        addOption(options, "C", source.getOption3());
        addOption(options, "D", source.getOption4());
        String optionsJson = toOptionsJson(options);
        String correctKey = resolveCorrectOptionKey(source.getAnswer(), options);
        return RecruitmentVacancyScreeningQuestionEntity.builder()
                .mysqlId(source.getId())
                .vacancy(vacancy)
                .questionText(text)
                .questionType(ScreeningQuestionTypeEnum.SINGLE_CHOICE)
                .sortOrder(sortOrder)
                .required(Boolean.TRUE)
                .knockout(Boolean.FALSE)
                .points(1)
                .correctOptionKey(correctKey)
                .optionsJson(optionsJson)
                .build();
    }

    public static RecruitmentCandidateEntity fromEmployeeApplicant(
            EmployeeEntity employee, Employee mysqlEmployee, UUID companyId) {
        if (employee == null || employee.getMysqlId() == null || companyId == null) {
            return null;
        }
        String fullName = buildFullName(employee);
        String email = blankToNull(employee.getEmail());
        if (email == null) {
            email = "migrated-emp-" + employee.getMysqlId() + "@legacy.local";
        }
        return RecruitmentCandidateEntity.builder()
                .mysqlId(candidateMysqlId(employee.getMysqlId()))
                .candidateCode(truncate("CAND-" + employee.getMysqlId(), 64))
                .companyId(companyId)
                .fullName(truncate(fullName, 255))
                .email(truncate(email, 255))
                .mobile(truncate(blankToNull(employee.getPhoneNumber()), 50))
                .dateOfBirth(employee.getDateOfBirth())
                .gender(mysqlEmployee != null ? blankToNull(mysqlEmployee.getGender()) : null)
                .currentAddress(mysqlEmployee != null ? blankToNull(mysqlEmployee.getTemperoryAdd()) : null)
                .permanentAddress(mysqlEmployee != null ? blankToNull(mysqlEmployee.getPermanentAdd()) : null)
                .source(CandidateSourceEnum.INTERNAL_APPLICATION)
                .convertedEmployeeId(null)
                .notes("Migrated internal applicant from employee mysqlId=" + employee.getMysqlId())
                .build();
    }

    public static RecruitmentApplicationEntity fromApplicant(
            Applicant source,
            RecruitmentCandidateEntity candidate,
            RecruitmentVacancyEntity vacancy) {
        if (source == null || source.getId() == null || candidate == null || vacancy == null) {
            return null;
        }
        LocalDateTime appliedAt = toLocalDateTime(source.getApplyDate());
        if (appliedAt == null) {
            appliedAt = LocalDateTime.now();
        }
        String stageName = source.getStage() != null ? blankToNull(source.getStage().getName()) : null;
        return RecruitmentApplicationEntity.builder()
                .mysqlId(source.getId())
                .candidate(candidate)
                .vacancy(vacancy)
                .applicationNumber(truncate("APP-" + source.getId(), 64))
                .applicationStatus(mapApplicationStatusFromStage(stageName))
                .source(CandidateSourceEnum.INTERNAL_APPLICATION)
                .appliedAt(appliedAt)
                .remarks(blankToNull(source.getCoverLetter()))
                .matchScore(source.getMatchPercent())
                .build();
    }

    public static RecruitmentApplicationScreeningAnswerEntity fromScreeningAnswer(
            ScreeningAnswer source,
            RecruitmentApplicationEntity application,
            RecruitmentVacancyScreeningQuestionEntity question) {
        if (source == null || source.getId() == null || application == null || question == null) {
            return null;
        }
        String answer = blankToNull(source.getAnswer());
        String selectedKey = resolveSelectedOptionKey(answer, question);
        Boolean isCorrect = null;
        int points = 0;
        if (selectedKey != null && question.getCorrectOptionKey() != null) {
            isCorrect = selectedKey.equalsIgnoreCase(question.getCorrectOptionKey());
            points = Boolean.TRUE.equals(isCorrect) && question.getPoints() != null ? question.getPoints() : 0;
        }
        return RecruitmentApplicationScreeningAnswerEntity.builder()
                .mysqlId(source.getId())
                .application(application)
                .question(question)
                .selectedOptionKey(selectedKey)
                .answerText(answer)
                .isCorrect(isCorrect)
                .pointsAwarded(points)
                .build();
    }

    public static RecruitmentApplicationStatusHistoryEntity fromApplicantsTransaction(
            ApplicantsTransaction source, RecruitmentApplicationEntity application) {
        if (source == null || source.getId() == null || application == null) {
            return null;
        }
        LocalDateTime changedAt = toLocalDateTime(source.getTransactionDate());
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
        String stageName = source.getStage() != null ? blankToNull(source.getStage().getName()) : null;
        StringBuilder remarks = new StringBuilder();
        if (blankToNull(source.getDescription()) != null) {
            remarks.append(source.getDescription().trim());
        }
        if (blankToNull(source.getDetails()) != null) {
            if (!remarks.isEmpty()) {
                remarks.append(" | ");
            }
            remarks.append(source.getDetails().trim());
        }
        ApplicationStatusEnum newStatus = mapApplicationStatusFromStage(stageName);
        if (newStatus == ApplicationStatusEnum.APPLIED && blankToNull(source.getDescription()) != null) {
            newStatus = mapApplicationStatusFromText(source.getDescription());
        }
        return RecruitmentApplicationStatusHistoryEntity.builder()
                .mysqlId(source.getId())
                .application(application)
                .previousStatus(null)
                .newStatus(newStatus)
                .changedAt(changedAt)
                .remarks(remarks.isEmpty() ? null : remarks.toString())
                .build();
    }

    public static RecruitmentScreeningEvaluationEntity fromEvaluation(
            Evaluation source, RecruitmentApplicationEntity application, UUID reviewerEmployeeId) {
        if (source == null || source.getId() == null || application == null) {
            return null;
        }
        LocalDateTime reviewedAt = toLocalDateTime(source.getDate());
        if (reviewedAt == null) {
            reviewedAt = LocalDateTime.now();
        }
        String text = blankToNull(source.getEvaluation());
        return RecruitmentScreeningEvaluationEntity.builder()
                .mysqlId(source.getId())
                .application(application)
                .strengths(text)
                .eligibility(ScreeningEligibilityEnum.CONDITIONALLY_ELIGIBLE)
                .recommendedNextStatus(ApplicationStatusEnum.UNDER_REVIEW)
                .reviewerEmployeeId(reviewerEmployeeId)
                .reviewedAt(reviewedAt)
                .remarks("Migrated from evaluation")
                .build();
    }

    public static VacancyStatusEnum mapVacancyStatus(Status status) {
        if (status == null) {
            return VacancyStatusEnum.DRAFT;
        }
        return switch (status) {
            case Published -> VacancyStatusEnum.PUBLISHED;
            case Delete -> VacancyStatusEnum.CANCELLED;
            case New -> VacancyStatusEnum.DRAFT;
        };
    }

    public static ApplicationStatusEnum mapApplicationStatusFromStage(String stageName) {
        if (stageName == null || stageName.isBlank()) {
            return ApplicationStatusEnum.APPLIED;
        }
        return mapApplicationStatusFromText(stageName);
    }

    public static ApplicationStatusEnum mapApplicationStatusFromText(String raw) {
        String key = raw.trim().toLowerCase(Locale.ROOT);
        if (key.contains("reject") || key.contains("fail") || key.contains("declin")) {
            return ApplicationStatusEnum.REJECTED;
        }
        if (key.contains("hold") || key.contains("pause")) {
            return ApplicationStatusEnum.ON_HOLD;
        }
        if (key.contains("shortlist") || key.contains("select")) {
            return ApplicationStatusEnum.SHORTLISTED;
        }
        if (key.contains("interview")) {
            return ApplicationStatusEnum.INTERVIEW_SCHEDULED;
        }
        if (key.contains("screen")) {
            return ApplicationStatusEnum.SCREENING;
        }
        if (key.contains("offer")) {
            return ApplicationStatusEnum.OFFER_ISSUED;
        }
        if (key.contains("hire") || key.contains("join") || key.contains("convert")) {
            return ApplicationStatusEnum.CONVERTED_TO_EMPLOYEE;
        }
        if (key.contains("withdraw")) {
            return ApplicationStatusEnum.WITHDRAWN;
        }
        if (key.contains("review")) {
            return ApplicationStatusEnum.UNDER_REVIEW;
        }
        return ApplicationStatusEnum.UNDER_REVIEW;
    }

    public static RecruitmentEmploymentTypeEnum mapEmploymentType(String jobType, String hireType) {
        String raw = ((jobType != null ? jobType : "") + " " + (hireType != null ? hireType : ""))
                .toLowerCase(Locale.ROOT);
        if (raw.contains("intern")) {
            return RecruitmentEmploymentTypeEnum.INTERNSHIP;
        }
        if (raw.contains("contract") || raw.contains("consultant")) {
            return RecruitmentEmploymentTypeEnum.CONTRACT;
        }
        if (raw.contains("part")) {
            return RecruitmentEmploymentTypeEnum.PART_TIME;
        }
        if (raw.contains("temp")) {
            return RecruitmentEmploymentTypeEnum.TEMPORARY;
        }
        if (raw.contains("probation")) {
            return RecruitmentEmploymentTypeEnum.PROBATIONARY;
        }
        if (raw.contains("full") || raw.contains("permanent")) {
            return RecruitmentEmploymentTypeEnum.FULL_TIME;
        }
        return RecruitmentEmploymentTypeEnum.FULL_TIME;
    }

    private static String buildFullName(EmployeeEntity employee) {
        StringBuilder sb = new StringBuilder();
        if (blankToNull(employee.getFirstName()) != null) {
            sb.append(employee.getFirstName().trim());
        }
        if (blankToNull(employee.getMiddleName()) != null) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(employee.getMiddleName().trim());
        }
        if (blankToNull(employee.getLastName()) != null) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(employee.getLastName().trim());
        }
        return sb.isEmpty() ? "Employee " + employee.getMysqlId() : sb.toString();
    }

    private static void addOption(List<String[]> options, String key, String label) {
        String cleaned = blankToNull(label);
        if (cleaned != null) {
            options.add(new String[] {key, cleaned});
        }
    }

    private static String toOptionsJson(List<String[]> options) {
        if (options.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < options.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"key\":\"")
                    .append(escapeJson(options.get(i)[0]))
                    .append("\",\"label\":\"")
                    .append(escapeJson(options.get(i)[1]))
                    .append("\"}");
        }
        sb.append(']');
        return sb.toString();
    }

    private static String resolveCorrectOptionKey(String answer, List<String[]> options) {
        String raw = blankToNull(answer);
        if (raw == null || options.isEmpty()) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.length() == 1) {
            String key = trimmed.toUpperCase(Locale.ROOT);
            for (String[] opt : options) {
                if (opt[0].equalsIgnoreCase(key)) {
                    return opt[0];
                }
            }
        }
        for (String[] opt : options) {
            if (opt[1].equalsIgnoreCase(trimmed) || opt[0].equalsIgnoreCase(trimmed)) {
                return opt[0];
            }
        }
        return null;
    }

    private static String resolveSelectedOptionKey(
            String answer, RecruitmentVacancyScreeningQuestionEntity question) {
        if (answer == null) {
            return null;
        }
        String trimmed = answer.trim();
        if (trimmed.length() == 1 && "ABCD".contains(trimmed.toUpperCase(Locale.ROOT))) {
            return trimmed.toUpperCase(Locale.ROOT);
        }
        if (question.getCorrectOptionKey() != null
                && trimmed.equalsIgnoreCase(question.getCorrectOptionKey())) {
            return question.getCorrectOptionKey();
        }
        // Best-effort: if answer equals an option label substring in options_json, leave key null
        return trimmed.length() <= 32 && trimmed.matches("[A-Da-d]")
                ? trimmed.toUpperCase(Locale.ROOT)
                : null;
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void appendKv(StringBuilder sb, String key, Object value) {
        if (value == null) {
            return;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return;
        }
        if (!sb.isEmpty()) {
            sb.append("; ");
        }
        sb.append(key).append('=').append(text);
    }

    private static BigDecimal money(Double value) {
        if (value == null || value.isNaN() || value.isInfinite()) {
            return null;
        }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZONE).toLocalDate();
    }

    private static LocalDateTime toLocalDateTime(Date date) {
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
