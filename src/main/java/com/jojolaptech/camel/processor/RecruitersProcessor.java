package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Recruiters;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentVacancyEntity;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.recruitment.PgRecruitmentVacancyRepository;
import java.util.ArrayList;
import java.util.HashSet;
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
 * Step 27h part A: recruiters → patch hrm_recruitment_vacancy.recruiter_employee_id when unset.
 */
@Component
@RequiredArgsConstructor
public class RecruitersProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(RecruitersProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgRecruitmentVacancyRepository vacancyRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Recruiters> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeMysqlIds.isEmpty()
                ? Map.of()
                : employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                        .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<Long> vacancyMysqlIds = batch.stream()
                .filter(row -> row.getVacancy() != null)
                .map(row -> row.getVacancy().getId())
                .collect(Collectors.toSet());
        Map<Long, RecruitmentVacancyEntity> vacancyByMysqlId = vacancyMysqlIds.isEmpty()
                ? Map.of()
                : vacancyRepository.findByMysqlIdIn(vacancyMysqlIds).stream()
                        .collect(Collectors.toMap(RecruitmentVacancyEntity::getMysqlId, v -> v, (a, b) -> a));

        List<RecruitmentVacancyEntity> toSave = new ArrayList<>();
        Set<Long> patchedVacancyMysqlIds = new HashSet<>();
        for (Recruiters source : batch) {
            if (source.getVacancy() == null) {
                log.warn("Skipping recruiters id={}, missing vacancy", source.getId());
                continue;
            }
            if (source.getEmployee() == null) {
                log.warn("Skipping recruiters id={}, missing employee", source.getId());
                continue;
            }
            RecruitmentVacancyEntity vacancy = vacancyByMysqlId.get(source.getVacancy().getId());
            if (vacancy == null) {
                log.warn(
                        "Skipping recruiters id={}, vacancy mysqlId={} not migrated",
                        source.getId(),
                        source.getVacancy().getId());
                continue;
            }
            if (vacancy.getRecruiterEmployeeId() != null || patchedVacancyMysqlIds.contains(vacancy.getMysqlId())) {
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping recruiters id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }
            vacancy.setRecruiterEmployeeId(employee.getId());
            toSave.add(vacancy);
            patchedVacancyMysqlIds.add(vacancy.getMysqlId());
        }

        if (!toSave.isEmpty()) {
            vacancyRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
