package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Company;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.OrganizationEntity;
import com.jojolaptech.camel.model.postgres.company.OrganizationTypeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgOrganizationRepository;
import com.jojolaptech.camel.service.OrganizationTypeCatalogService;
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

@Component
@RequiredArgsConstructor
public class OrganizationProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(OrganizationProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgOrganizationRepository organizationRepository;
    private final OrganizationTypeCatalogService organizationTypeCatalogService;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Company> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Map<Long, Company> mysqlCompanyById = batch.stream()
                .collect(Collectors.toMap(Company::getId, company -> company, (left, right) -> left));
        List<CompanyEntity> companies = companyRepository.findByMysqlIdIn(mysqlCompanyById.keySet()).stream()
                .filter(company -> company.getOrganization() == null)
                .toList();
        if (companies.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<String> orgCodes = companies.stream()
                .map(company -> OrgMigrationMapper.organizationCode(company.getMysqlId()))
                .collect(Collectors.toSet());
        Map<String, OrganizationEntity> organizationByCode = organizationRepository.findByCodeIn(orgCodes).stream()
                .collect(Collectors.toMap(OrganizationEntity::getCode, org -> org, (left, right) -> left));

        OrganizationTypeEntity organizationType = organizationTypeCatalogService.defaultOrganizationType();
        List<OrganizationEntity> newOrganizations = new ArrayList<>();
        List<CompanyEntity> companiesToUpdate = new ArrayList<>();
        int imported = 0;

        for (CompanyEntity company : companies) {
            Company source = mysqlCompanyById.get(company.getMysqlId());
            if (source == null) {
                continue;
            }
            String orgCode = OrgMigrationMapper.organizationCode(company.getMysqlId());
            OrganizationEntity organization = organizationByCode.get(orgCode);
            if (organization == null) {
                organization = OrgMigrationMapper.toOrganization(source, organizationType);
                newOrganizations.add(organization);
                organizationByCode.put(orgCode, organization);
                imported++;
            }
            company.setOrganization(organization);
            companiesToUpdate.add(company);
        }

        if (!newOrganizations.isEmpty()) {
            organizationRepository.saveAll(newOrganizations);
            organizationRepository.flush();
        }
        if (!companiesToUpdate.isEmpty()) {
            companyRepository.saveAll(companiesToUpdate);
        }

        log.info(
                "Organization batch linked {} companies ({} new organizations)",
                companiesToUpdate.size(),
                newOrganizations.size());
        exchange.setProperty("batchImported", imported);
    }
}
