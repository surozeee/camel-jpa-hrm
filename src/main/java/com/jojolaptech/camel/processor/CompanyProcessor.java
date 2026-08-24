package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Company;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.service.CompanyTypeCatalogService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompanyProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(CompanyProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final CompanyTypeCatalogService companyTypeCatalogService;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Company> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> existingIds = companyRepository.findMysqlIdsByMysqlIdIn(
                batch.stream().map(Company::getId).toList());
        Set<String> names = batch.stream()
                .map(company -> OrgMigrationMapper.normalizeName(company.getName()))
                .filter(name -> !name.isBlank())
                .collect(Collectors.toSet());
        Set<String> existingNames = names.isEmpty()
                ? Set.of()
                : companyRepository.findExistingNamesIgnoreCase(names);

        var companyType = companyTypeCatalogService.defaultCompanyType();
        List<CompanyEntity> toSave = new ArrayList<>();
        Set<String> namesInBatch = new HashSet<>();
        for (Company source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            String name = OrgMigrationMapper.trimToNull(source.getName());
            if (name == null) {
                log.warn("Skipping company id={}, name is blank", source.getId());
                continue;
            }
            String nameKey = name.toLowerCase(Locale.ROOT);
            if (existingNames.contains(nameKey) || !namesInBatch.add(nameKey)) {
                log.info("Skipping company id={}, name already exists", source.getId());
                continue;
            }

            CompanyEntity company = CompanyEntity.builder()
                    .mysqlId(source.getId())
                    .name(name)
                    .contactNo(OrgMigrationMapper.trimToNull(source.getPhone()))
                    .website(OrgMigrationMapper.trimToNull(source.getUrl()))
                    .logoUrl(OrgMigrationMapper.trimToNull(source.getLogo()))
                    .description(OrgMigrationMapper.companyDescription(source))
                    .companyType(companyType)
                    .build();
            company.setStatus(OrgMigrationMapper.companyStatus(source));
            toSave.add(company);
        }

        if (!toSave.isEmpty()) {
            companyRepository.saveAll(toSave);
            companyRepository.flush();
        }

        log.info("Company batch imported {} of {} rows", toSave.size(), batch.size());
        exchange.setProperty("batchImported", toSave.size());
    }
}
