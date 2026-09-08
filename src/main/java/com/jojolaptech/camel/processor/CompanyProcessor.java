package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Company;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.service.CompanyTypeCatalogService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        // Full name set so suffix 1/2/3… collisions are detected against DB + in-batch.
        Set<String> namesInUse = new HashSet<>(companyRepository.findAllNamesLowerCase());

        var companyType = companyTypeCatalogService.defaultCompanyType();
        List<CompanyEntity> toSave = new ArrayList<>();
        for (Company source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }

            String cleaned = OrgMigrationMapper.trimToNull(source.getName());
            String name = OrgMigrationMapper.uniqueCompanyName(source.getName(), source.getId(), namesInUse);
            if (cleaned != null && !cleaned.equals(name)) {
                log.info(
                        "Company id={} name uniquified to '{}' (legacy duplicate/NUL name)",
                        source.getId(),
                        name);
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
