package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Vacancy;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentVacancyEntity;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.recruitment.PgRecruitmentVacancyRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
 * Step 27a: vacancy → hrm_recruitment_vacancy.
 */
@Component
@RequiredArgsConstructor
public class VacancyProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(VacancyProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgBranchRepository branchRepository;
    private final PgEmployeeRepository employeeRepository;
    private final PgRecruitmentVacancyRepository vacancyRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Vacancy> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(Vacancy::getId).collect(Collectors.toSet());
        Set<Long> existingIds = vacancyRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> companyMysqlIds = batch.stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companyByMysqlId = companyMysqlIds.isEmpty()
                ? Map.of()
                : companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                        .collect(Collectors.toMap(CompanyEntity::getMysqlId, c -> c, (a, b) -> a));

        Map<Long, BranchEntity> primaryBranchByCompanyMysqlId = new HashMap<>();
        Map<UUID, BranchEntity> branchById = new HashMap<>();
        if (!companyMysqlIds.isEmpty()) {
            for (BranchEntity branch : branchRepository.findByCompanyMysqlIdIn(companyMysqlIds)) {
                if (branch.getId() != null) {
                    branchById.put(branch.getId(), branch);
                }
                if (branch.getCompany() == null || branch.getCompany().getMysqlId() == null) {
                    continue;
                }
                Long companyMysqlId = branch.getCompany().getMysqlId();
                BranchEntity existing = primaryBranchByCompanyMysqlId.get(companyMysqlId);
                if (existing == null
                        || (branch.getMysqlId() != null
                                && (existing.getMysqlId() == null
                                        || branch.getMysqlId() < existing.getMysqlId()))) {
                    primaryBranchByCompanyMysqlId.put(companyMysqlId, branch);
                }
            }
        }

        Set<Long> hiringManagerMysqlIds = batch.stream()
                .filter(row -> row.getHiringManager() != null)
                .map(row -> row.getHiringManager().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = hiringManagerMysqlIds.isEmpty()
                ? Map.of()
                : employeeRepository.findByMysqlIdIn(hiringManagerMysqlIds).stream()
                        .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<UUID> managerBranchIds = employeeByMysqlId.values().stream()
                .map(EmployeeEntity::getBranchId)
                .filter(Objects::nonNull)
                .filter(id -> !branchById.containsKey(id))
                .collect(Collectors.toSet());
        if (!managerBranchIds.isEmpty()) {
            for (BranchEntity branch : branchRepository.findByIdInWithCompany(managerBranchIds)) {
                branchById.put(branch.getId(), branch);
            }
        }

        List<RecruitmentVacancyEntity> toSave = new ArrayList<>();
        for (Vacancy source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getCompany() == null) {
                log.warn("Skipping vacancy id={}, missing company", source.getId());
                continue;
            }
            CompanyEntity company = companyByMysqlId.get(source.getCompany().getId());
            if (company == null) {
                log.warn(
                        "Skipping vacancy id={}, company mysqlId={} not migrated",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }

            BranchEntity branch = primaryBranchByCompanyMysqlId.get(source.getCompany().getId());
            UUID hiringManagerId = null;
            if (source.getHiringManager() != null) {
                EmployeeEntity manager = employeeByMysqlId.get(source.getHiringManager().getId());
                if (manager != null) {
                    hiringManagerId = manager.getId();
                    if (manager.getBranchId() != null) {
                        BranchEntity managerBranch = branchById.get(manager.getBranchId());
                        if (managerBranch != null
                                && managerBranch.getCompany() != null
                                && company.getId().equals(managerBranch.getCompany().getId())) {
                            branch = managerBranch;
                        }
                    }
                }
            }
            if (branch == null) {
                log.warn(
                        "Skipping vacancy id={}, no migrated branch for company mysqlId={}",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }

            RecruitmentVacancyEntity mapped = RecruitmentAtsMigrationMapper.fromVacancy(
                    source, company.getId(), branch.getId(), hiringManagerId);
            if (mapped == null) {
                log.warn("Skipping vacancy id={}, mapping failed", source.getId());
                continue;
            }
            toSave.add(mapped);
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            vacancyRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
