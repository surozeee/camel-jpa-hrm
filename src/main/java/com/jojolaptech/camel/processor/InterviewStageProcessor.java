package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Stages;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentInterviewStageEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentVacancyEntity;
import com.jojolaptech.camel.repository.postgres.recruitment.PgRecruitmentInterviewStageRepository;
import com.jojolaptech.camel.repository.postgres.recruitment.PgRecruitmentVacancyRepository;
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
 * Step 27c: stages → hrm_recruitment_interview_stage.
 */
@Component
@RequiredArgsConstructor
public class InterviewStageProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(InterviewStageProcessor.class);

    private final PgRecruitmentVacancyRepository vacancyRepository;
    private final PgRecruitmentInterviewStageRepository interviewStageRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Stages> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(Stages::getId).collect(Collectors.toSet());
        Set<Long> existingIds = interviewStageRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> vacancyMysqlIds = batch.stream()
                .filter(row -> row.getVacancy() != null)
                .map(row -> row.getVacancy().getId())
                .collect(Collectors.toSet());
        Map<Long, RecruitmentVacancyEntity> vacancyByMysqlId = vacancyMysqlIds.isEmpty()
                ? Map.of()
                : vacancyRepository.findByMysqlIdIn(vacancyMysqlIds).stream()
                        .collect(Collectors.toMap(RecruitmentVacancyEntity::getMysqlId, v -> v, (a, b) -> a));

        List<RecruitmentInterviewStageEntity> toSave = new ArrayList<>();
        for (Stages source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getVacancy() == null) {
                log.warn("Skipping stages id={}, missing vacancy", source.getId());
                continue;
            }
            RecruitmentVacancyEntity vacancy = vacancyByMysqlId.get(source.getVacancy().getId());
            if (vacancy == null) {
                log.warn(
                        "Skipping stages id={}, vacancy mysqlId={} not migrated",
                        source.getId(),
                        source.getVacancy().getId());
                continue;
            }
            RecruitmentInterviewStageEntity mapped = RecruitmentAtsMigrationMapper.fromStage(source, vacancy);
            if (mapped == null) {
                log.warn("Skipping stages id={}, mapping failed", source.getId());
                continue;
            }
            toSave.add(mapped);
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            interviewStageRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
