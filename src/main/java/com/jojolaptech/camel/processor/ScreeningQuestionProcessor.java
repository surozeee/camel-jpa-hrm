package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.ScreeningQuestion;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentVacancyEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentVacancyScreeningQuestionEntity;
import com.jojolaptech.camel.repository.postgres.recruitment.PgRecruitmentVacancyRepository;
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
 * Step 27d: screeningQuestion → hrm_recruitment_vacancy_screening_question.
 */
@Component
@RequiredArgsConstructor
public class ScreeningQuestionProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(ScreeningQuestionProcessor.class);

    private final PgRecruitmentVacancyRepository vacancyRepository;
    private final PgRecruitmentVacancyScreeningQuestionRepository screeningQuestionRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<ScreeningQuestion> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(ScreeningQuestion::getId).collect(Collectors.toSet());
        Set<Long> existingIds = screeningQuestionRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> vacancyMysqlIds = batch.stream()
                .filter(row -> row.getVacancy() != null)
                .map(row -> row.getVacancy().getId())
                .collect(Collectors.toSet());
        Map<Long, RecruitmentVacancyEntity> vacancyByMysqlId = vacancyMysqlIds.isEmpty()
                ? Map.of()
                : vacancyRepository.findByMysqlIdIn(vacancyMysqlIds).stream()
                        .collect(Collectors.toMap(RecruitmentVacancyEntity::getMysqlId, v -> v, (a, b) -> a));

        List<RecruitmentVacancyScreeningQuestionEntity> toSave = new ArrayList<>();
        int index = 0;
        for (ScreeningQuestion source : batch) {
            int sortOrder = source.getId() != null ? source.getId().intValue() : index;
            index++;
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getVacancy() == null) {
                log.warn("Skipping screeningQuestion id={}, missing vacancy", source.getId());
                continue;
            }
            RecruitmentVacancyEntity vacancy = vacancyByMysqlId.get(source.getVacancy().getId());
            if (vacancy == null) {
                log.warn(
                        "Skipping screeningQuestion id={}, vacancy mysqlId={} not migrated",
                        source.getId(),
                        source.getVacancy().getId());
                continue;
            }
            RecruitmentVacancyScreeningQuestionEntity mapped =
                    RecruitmentAtsMigrationMapper.fromScreeningQuestion(source, vacancy, sortOrder);
            if (mapped == null) {
                log.warn("Skipping screeningQuestion id={}, mapping failed", source.getId());
                continue;
            }
            toSave.add(mapped);
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            screeningQuestionRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
