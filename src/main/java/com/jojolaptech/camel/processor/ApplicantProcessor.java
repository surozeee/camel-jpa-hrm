package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Applicant;
import com.jojolaptech.camel.model.mysql.Employee;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentApplicationEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentCandidateEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentVacancyEntity;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.recruitment.PgRecruitmentApplicationRepository;
import com.jojolaptech.camel.repository.postgres.recruitment.PgRecruitmentCandidateRepository;
import com.jojolaptech.camel.repository.postgres.recruitment.PgRecruitmentVacancyRepository;
import java.util.ArrayList;
import java.util.HashMap;
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

/**
 * Step 27e: applicant → hrm_recruitment_candidate (if needed) + hrm_recruitment_application.
 */
@Component
@RequiredArgsConstructor
public class ApplicantProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(ApplicantProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgRecruitmentVacancyRepository vacancyRepository;
    private final PgRecruitmentCandidateRepository candidateRepository;
    private final PgRecruitmentApplicationRepository applicationRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Applicant> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> applicationMysqlIds = batch.stream().map(Applicant::getId).collect(Collectors.toSet());
        Set<Long> existingApplicationIds = applicationRepository.findMysqlIdsByMysqlIdIn(applicationMysqlIds);

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeMysqlIds.isEmpty()
                ? Map.of()
                : employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                        .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<Long> candidateMysqlIds = employeeMysqlIds.stream()
                .map(RecruitmentAtsMigrationMapper::candidateMysqlId)
                .collect(Collectors.toSet());
        Map<Long, RecruitmentCandidateEntity> candidateByMysqlId = new HashMap<>();
        if (!candidateMysqlIds.isEmpty()) {
            for (RecruitmentCandidateEntity candidate : candidateRepository.findByMysqlIdIn(candidateMysqlIds)) {
                candidateByMysqlId.put(candidate.getMysqlId(), candidate);
            }
        }

        Set<Long> vacancyMysqlIds = batch.stream()
                .filter(row -> row.getVacancy() != null)
                .map(row -> row.getVacancy().getId())
                .collect(Collectors.toSet());
        Map<Long, RecruitmentVacancyEntity> vacancyByMysqlId = vacancyMysqlIds.isEmpty()
                ? Map.of()
                : vacancyRepository.findByMysqlIdIn(vacancyMysqlIds).stream()
                        .collect(Collectors.toMap(RecruitmentVacancyEntity::getMysqlId, v -> v, (a, b) -> a));

        List<RecruitmentCandidateEntity> candidatesToSave = new ArrayList<>();
        List<RecruitmentApplicationEntity> applicationsToSave = new ArrayList<>();

        for (Applicant source : batch) {
            if (existingApplicationIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployee() == null) {
                log.warn("Skipping applicant id={}, missing employee", source.getId());
                continue;
            }
            if (source.getVacancy() == null) {
                log.warn("Skipping applicant id={}, missing vacancy", source.getId());
                continue;
            }

            Employee mysqlEmployee = source.getEmployee();
            EmployeeEntity pgEmployee = employeeByMysqlId.get(mysqlEmployee.getId());
            if (pgEmployee == null) {
                log.warn(
                        "Skipping applicant id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        mysqlEmployee.getId());
                continue;
            }

            RecruitmentVacancyEntity vacancy = vacancyByMysqlId.get(source.getVacancy().getId());
            if (vacancy == null) {
                log.warn(
                        "Skipping applicant id={}, vacancy mysqlId={} not migrated",
                        source.getId(),
                        source.getVacancy().getId());
                continue;
            }

            long candidateMysqlId = RecruitmentAtsMigrationMapper.candidateMysqlId(pgEmployee.getMysqlId());
            RecruitmentCandidateEntity candidate = candidateByMysqlId.get(candidateMysqlId);
            if (candidate == null) {
                candidate = RecruitmentAtsMigrationMapper.fromEmployeeApplicant(
                        pgEmployee, mysqlEmployee, vacancy.getCompanyId());
                if (candidate == null) {
                    log.warn("Skipping applicant id={}, candidate mapping failed", source.getId());
                    continue;
                }
                candidatesToSave.add(candidate);
                candidateByMysqlId.put(candidateMysqlId, candidate);
            }

            RecruitmentApplicationEntity application =
                    RecruitmentAtsMigrationMapper.fromApplicant(source, candidate, vacancy);
            if (application == null) {
                log.warn("Skipping applicant id={}, application mapping failed", source.getId());
                continue;
            }
            applicationsToSave.add(application);
            existingApplicationIds.add(source.getId());
        }

        if (!candidatesToSave.isEmpty()) {
            List<RecruitmentCandidateEntity> savedCandidates = candidateRepository.saveAll(candidatesToSave);
            for (RecruitmentCandidateEntity saved : savedCandidates) {
                candidateByMysqlId.put(saved.getMysqlId(), saved);
            }
            // Re-attach managed candidates onto applications that referenced the transient ones
            for (RecruitmentApplicationEntity application : applicationsToSave) {
                Long mysqlId = application.getCandidate() != null ? application.getCandidate().getMysqlId() : null;
                if (mysqlId != null) {
                    RecruitmentCandidateEntity managed = candidateByMysqlId.get(mysqlId);
                    if (managed != null) {
                        application.setCandidate(managed);
                    }
                }
            }
        }

        if (!applicationsToSave.isEmpty()) {
            applicationRepository.saveAll(applicationsToSave);
        }
        exchange.setProperty("batchImported", applicationsToSave.size());
    }
}
