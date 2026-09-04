package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Evaluation;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentApplicationEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentScreeningEvaluationEntity;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.recruitment.PgRecruitmentApplicationRepository;
import com.jojolaptech.camel.repository.postgres.recruitment.PgRecruitmentScreeningEvaluationRepository;
import java.util.ArrayList;
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

/**
 * Step 27h: evaluation → hrm_recruitment_screening.
 */
@Component
@RequiredArgsConstructor
public class EvaluationProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EvaluationProcessor.class);

    private final PgRecruitmentApplicationRepository applicationRepository;
    private final PgEmployeeRepository employeeRepository;
    private final PgRecruitmentScreeningEvaluationRepository evaluationRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Evaluation> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(Evaluation::getId).collect(Collectors.toSet());
        Set<Long> existingIds = evaluationRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> applicationMysqlIds = batch.stream()
                .filter(row -> row.getApplicant() != null)
                .map(row -> row.getApplicant().getId())
                .collect(Collectors.toSet());
        Map<Long, RecruitmentApplicationEntity> applicationByMysqlId = applicationMysqlIds.isEmpty()
                ? Map.of()
                : applicationRepository.findByMysqlIdIn(applicationMysqlIds).stream()
                        .collect(Collectors.toMap(RecruitmentApplicationEntity::getMysqlId, a -> a, (a, b) -> a));

        Set<Long> reviewerMysqlIds = batch.stream()
                .filter(row -> row.getRecruiter() != null && row.getRecruiter().getEmployee() != null)
                .map(row -> row.getRecruiter().getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = reviewerMysqlIds.isEmpty()
                ? Map.of()
                : employeeRepository.findByMysqlIdIn(reviewerMysqlIds).stream()
                        .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        List<RecruitmentScreeningEvaluationEntity> toSave = new ArrayList<>();
        for (Evaluation source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getApplicant() == null) {
                log.warn("Skipping evaluation id={}, missing applicant", source.getId());
                continue;
            }
            RecruitmentApplicationEntity application =
                    applicationByMysqlId.get(source.getApplicant().getId());
            if (application == null) {
                log.warn(
                        "Skipping evaluation id={}, application mysqlId={} not migrated",
                        source.getId(),
                        source.getApplicant().getId());
                continue;
            }

            UUID reviewerEmployeeId = null;
            if (source.getRecruiter() != null && source.getRecruiter().getEmployee() != null) {
                EmployeeEntity reviewer =
                        employeeByMysqlId.get(source.getRecruiter().getEmployee().getId());
                if (reviewer != null) {
                    reviewerEmployeeId = reviewer.getId();
                } else {
                    log.warn(
                            "evaluation id={}, reviewer employee mysqlId={} not migrated; importing without reviewer",
                            source.getId(),
                            source.getRecruiter().getEmployee().getId());
                }
            }

            RecruitmentScreeningEvaluationEntity mapped =
                    RecruitmentAtsMigrationMapper.fromEvaluation(source, application, reviewerEmployeeId);
            if (mapped == null) {
                log.warn("Skipping evaluation id={}, mapping failed", source.getId());
                continue;
            }
            toSave.add(mapped);
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            evaluationRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
