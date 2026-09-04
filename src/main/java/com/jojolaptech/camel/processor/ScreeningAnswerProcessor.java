package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.ScreeningAnswer;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentApplicationEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentApplicationScreeningAnswerEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentVacancyScreeningQuestionEntity;
import com.jojolaptech.camel.repository.postgres.recruitment.PgRecruitmentApplicationRepository;
import com.jojolaptech.camel.repository.postgres.recruitment.PgRecruitmentApplicationScreeningAnswerRepository;
import com.jojolaptech.camel.repository.postgres.recruitment.PgRecruitmentVacancyScreeningQuestionRepository;
import java.util.ArrayList;
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
 * Step 27f: screeningAnswer → hrm_recruitment_application_screening_answer.
 */
@Component
@RequiredArgsConstructor
public class ScreeningAnswerProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(ScreeningAnswerProcessor.class);

    private final PgRecruitmentApplicationRepository applicationRepository;
    private final PgRecruitmentVacancyScreeningQuestionRepository screeningQuestionRepository;
    private final PgRecruitmentApplicationScreeningAnswerRepository screeningAnswerRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<ScreeningAnswer> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(ScreeningAnswer::getId).collect(Collectors.toSet());
        Set<Long> existingIds = screeningAnswerRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> applicationMysqlIds = batch.stream()
                .filter(row -> row.getApplicant() != null)
                .map(row -> row.getApplicant().getId())
                .collect(Collectors.toSet());
        Map<Long, RecruitmentApplicationEntity> applicationByMysqlId = applicationMysqlIds.isEmpty()
                ? Map.of()
                : applicationRepository.findByMysqlIdIn(applicationMysqlIds).stream()
                        .collect(Collectors.toMap(RecruitmentApplicationEntity::getMysqlId, a -> a, (a, b) -> a));

        Set<Long> questionMysqlIds = batch.stream()
                .filter(row -> row.getScreeningQuestion() != null)
                .map(row -> row.getScreeningQuestion().getId())
                .collect(Collectors.toSet());
        Map<Long, RecruitmentVacancyScreeningQuestionEntity> questionByMysqlId = questionMysqlIds.isEmpty()
                ? Map.of()
                : screeningQuestionRepository.findByMysqlIdIn(questionMysqlIds).stream()
                        .collect(Collectors.toMap(
                                RecruitmentVacancyScreeningQuestionEntity::getMysqlId, q -> q, (a, b) -> a));

        List<RecruitmentApplicationScreeningAnswerEntity> toSave = new ArrayList<>();
        for (ScreeningAnswer source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getApplicant() == null) {
                log.warn("Skipping screeningAnswer id={}, missing applicant", source.getId());
                continue;
            }
            if (source.getScreeningQuestion() == null) {
                log.warn("Skipping screeningAnswer id={}, missing screeningQuestion", source.getId());
                continue;
            }
            RecruitmentApplicationEntity application =
                    applicationByMysqlId.get(source.getApplicant().getId());
            if (application == null) {
                log.warn(
                        "Skipping screeningAnswer id={}, application mysqlId={} not migrated",
                        source.getId(),
                        source.getApplicant().getId());
                continue;
            }
            RecruitmentVacancyScreeningQuestionEntity question =
                    questionByMysqlId.get(source.getScreeningQuestion().getId());
            if (question == null) {
                log.warn(
                        "Skipping screeningAnswer id={}, screeningQuestion mysqlId={} not migrated",
                        source.getId(),
                        source.getScreeningQuestion().getId());
                continue;
            }
            RecruitmentApplicationScreeningAnswerEntity mapped =
                    RecruitmentAtsMigrationMapper.fromScreeningAnswer(source, application, question);
            if (mapped == null) {
                log.warn("Skipping screeningAnswer id={}, mapping failed", source.getId());
                continue;
            }
            toSave.add(mapped);
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            screeningAnswerRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
