package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.FiscalYear;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.BranchFiscalYearEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyFiscalYearEntity;
import com.jojolaptech.camel.model.postgres.master.FiscalYearEntity;
import com.jojolaptech.camel.repository.postgres.company.PgBranchFiscalYearRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyFiscalYearRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.master.PgFiscalYearRepository;
import com.jojolaptech.camel.service.FiscalYearTypeCatalogService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FiscalYearProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(FiscalYearProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgBranchRepository branchRepository;
    private final PgFiscalYearRepository fiscalYearRepository;
    private final PgCompanyFiscalYearRepository companyFiscalYearRepository;
    private final PgBranchFiscalYearRepository branchFiscalYearRepository;
    private final FiscalYearTypeCatalogService fiscalYearTypeCatalogService;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<FiscalYear> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> existingCompanyFyIds = companyFiscalYearRepository.findMysqlIdsByMysqlIdIn(
                batch.stream().map(FiscalYear::getId).toList());
        Set<String> existingBranchFyKeys = branchFiscalYearRepository.findExistingKeyPairsByMysqlIdIn(
                        batch.stream().map(FiscalYear::getId).toList())
                .stream()
                .map(pair -> FiscalMigrationMapper.branchFiscalYearKey(
                        (Long) pair[0], (Long) pair[1]))
                .collect(Collectors.toCollection(HashSet::new));

        Set<Long> companyMysqlIds = batch.stream()
                .map(fy -> fy.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companiesByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, Function.identity()));

        Map<String, FiscalYearEntity> masterByName = loadOrCreateMasterYears(batch, existingCompanyFyIds);

        List<CompanyFiscalYearEntity> companyFiscalYears = new ArrayList<>();
        List<BranchFiscalYearEntity> branchFiscalYears = new ArrayList<>();

        for (FiscalYear source : batch) {
            if (existingCompanyFyIds.contains(source.getId())) {
                continue;
            }
            CompanyEntity company = companiesByMysqlId.get(source.getCompany().getId());
            if (company == null) {
                log.warn("Skipping fiscal year id={}, company mysqlId={} not migrated",
                        source.getId(), source.getCompany().getId());
                continue;
            }

            String fyName = FiscalMigrationMapper.trimToNull(source.getName());
            LocalDate startDate = FiscalMigrationMapper.toLocalDate(source.getStartDate());
            LocalDate endDate = FiscalMigrationMapper.toLocalDate(source.getEndDate());
            if (fyName == null || startDate == null || endDate == null) {
                log.warn("Skipping fiscal year id={}, missing name or dates", source.getId());
                continue;
            }

            FiscalYearEntity master = masterByName.get(fyName);
            if (master == null || master.getId() == null) {
                log.warn("Skipping fiscal year id={}, master fiscal year {} unavailable", source.getId(), fyName);
                continue;
            }

            companyFiscalYears.add(CompanyFiscalYearEntity.builder()
                    .mysqlId(source.getId())
                    .companyId(company.getId())
                    .masterFiscalYearId(master.getId())
                    .fiscalYear(fyName)
                    .startDate(startDate)
                    .endDate(endDate)
                    .build());

            List<BranchEntity> branches =
                    branchRepository.findByCompanyMysqlIdOrderByMysqlIdAsc(company.getMysqlId());
            for (BranchEntity branch : branches) {
                String key = FiscalMigrationMapper.branchFiscalYearKey(source.getId(), branch.getMysqlId());
                if (!existingBranchFyKeys.add(key)) {
                    continue;
                }
                branchFiscalYears.add(BranchFiscalYearEntity.builder()
                        .mysqlId(source.getId())
                        .mysqlBranchId(branch.getMysqlId())
                        .branchId(branch.getId())
                        .fiscalYear(FiscalMigrationMapper.branchFiscalYearLabel(
                                company.getMysqlId(), branch.getMysqlId(), fyName))
                        .startDate(startDate)
                        .endDate(endDate)
                        .fiscalYearType(FiscalMigrationMapper.branchFiscalYearType())
                        .build());
            }
        }

        int imported = 0;
        if (!companyFiscalYears.isEmpty()) {
            companyFiscalYearRepository.saveAll(companyFiscalYears);
            imported += companyFiscalYears.size();
        }
        if (!branchFiscalYears.isEmpty()) {
            branchFiscalYearRepository.saveAll(branchFiscalYears);
            imported += branchFiscalYears.size();
        }

        exchange.setProperty("batchImported", imported);
    }

    private Map<String, FiscalYearEntity> loadOrCreateMasterYears(
            List<FiscalYear> batch, Set<Long> existingCompanyFyIds) {
        Map<String, LocalDate[]> datesByName = new HashMap<>();
        for (FiscalYear source : batch) {
            if (existingCompanyFyIds.contains(source.getId())) {
                continue;
            }
            String fyName = FiscalMigrationMapper.trimToNull(source.getName());
            LocalDate startDate = FiscalMigrationMapper.toLocalDate(source.getStartDate());
            LocalDate endDate = FiscalMigrationMapper.toLocalDate(source.getEndDate());
            if (fyName != null && startDate != null && endDate != null) {
                datesByName.putIfAbsent(fyName, new LocalDate[] {startDate, endDate});
            }
        }

        Map<String, FiscalYearEntity> masterByName = fiscalYearRepository.findByFiscalYearIn(datesByName.keySet())
                .stream()
                .collect(Collectors.toMap(FiscalYearEntity::getFiscalYear, Function.identity()));

        var fiscalYearType = fiscalYearTypeCatalogService.defaultFiscalYearType();
        List<FiscalYearEntity> toCreate = new ArrayList<>();
        for (Map.Entry<String, LocalDate[]> entry : datesByName.entrySet()) {
            if (!masterByName.containsKey(entry.getKey())) {
                FiscalYearEntity master = FiscalYearEntity.builder()
                        .fiscalYear(entry.getKey())
                        .startDate(entry.getValue()[0])
                        .endDate(entry.getValue()[1])
                        .fiscalYearType(fiscalYearType)
                        .build();
                toCreate.add(master);
                masterByName.put(entry.getKey(), master);
            }
        }
        if (!toCreate.isEmpty()) {
            fiscalYearRepository.saveAll(toCreate);
            masterByName.putAll(fiscalYearRepository.findByFiscalYearIn(
                            toCreate.stream().map(FiscalYearEntity::getFiscalYear).collect(Collectors.toSet()))
                    .stream()
                    .collect(Collectors.toMap(FiscalYearEntity::getFiscalYear, Function.identity())));
        }
        return masterByName;
    }
}
