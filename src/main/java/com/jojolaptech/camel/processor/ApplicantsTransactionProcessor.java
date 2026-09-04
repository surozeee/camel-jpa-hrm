package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.ApplicantsTransaction;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentApplicationEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentApplicationStatusHistoryEntity;
import com.jojolaptech.camel.repository.postgres.recruitment.PgRecruitmentApplicationRepository;
import com.jojolaptech.camel.repository.postgres.recruitment.PgRecruitmentApplicationStatusHistoryRepository;
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
 * Step 27g: applicantsTransaction → hrm_recruitment_application_status_history.
 */
@Component
@RequiredArgsConstructor
public class ApplicantsTransactionProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(ApplicantsTransactionProcessor.class);

    private final PgRecruitmentApplicationRepository applicationRepository;
    private final PgRecruitmentApplicationStatusHistoryRepository statusHistoryRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<ApplicantsTransaction> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(ApplicantsTransaction::getId).collect(Collectors.toSet());
        Set<Long> existingIds = statusHistoryRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> applicationMysqlIds = batch.stream()
                .filter(row -> row.getApplicant() != null)
                .map(row -> row.getApplicant().getId())
                .collect(Collectors.toSet());
        Map<Long, RecruitmentApplicationEntity> applicationByMysqlId = applicationMysqlIds.isEmpty()
                ? Map.of()
                : applicationRepository.findByMysqlIdIn(applicationMysqlIds).stream()
                        .collect(Collectors.toMap(RecruitmentApplicationEntity::getMysqlId, a -> a, (a, b) -> a));

        List<RecruitmentApplicationStatusHistoryEntity> toSave = new ArrayList<>();
        for (ApplicantsTransaction source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getApplicant() == null) {
                log.warn("Skipping applicantsTransaction id={}, missing applicant", source.getId());
                continue;
            }
            RecruitmentApplicationEntity application =
                    applicationByMysqlId.get(source.getApplicant().getId());
            if (application == null) {
                log.warn(
                        "Skipping applicantsTransaction id={}, application mysqlId={} not migrated",
                        source.getId(),
                        source.getApplicant().getId());
                continue;
            }
            RecruitmentApplicationStatusHistoryEntity mapped =
                    RecruitmentAtsMigrationMapper.fromApplicantsTransaction(source, application);
            if (mapped == null) {
                log.warn("Skipping applicantsTransaction id={}, mapping failed", source.getId());
                continue;
            }
            toSave.add(mapped);
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            statusHistoryRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
