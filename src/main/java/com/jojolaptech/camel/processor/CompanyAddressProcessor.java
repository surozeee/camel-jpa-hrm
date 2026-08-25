package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Company;
import com.jojolaptech.camel.model.postgres.company.CompanyAddressEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyAddressRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
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
public class CompanyAddressProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(CompanyAddressProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgCompanyAddressRepository companyAddressRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Company> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        List<Long> companyIds = batch.stream().map(Company::getId).toList();
        Set<Long> existingAddressIds = companyAddressRepository.findMysqlIdsByMysqlIdIn(companyIds);
        Map<Long, CompanyEntity> companiesByMysqlId = companyRepository.findByMysqlIdIn(companyIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, company -> company));

        List<CompanyAddressEntity> toSave = new ArrayList<>();
        for (Company source : batch) {
            if (existingAddressIds.contains(source.getId())) {
                continue;
            }
            if (!OrgMigrationMapper.hasCompanyAddress(source)) {
                continue;
            }
            CompanyEntity company = companiesByMysqlId.get(source.getId());
            if (company == null) {
                log.warn("Skipping company address id={}, company not migrated yet", source.getId());
                continue;
            }
            CompanyAddressEntity address = AddressMigrationMapper.companyAddress(source, company.getId());
            if (address != null) {
                toSave.add(address);
            }
        }

        if (!toSave.isEmpty()) {
            companyAddressRepository.saveAll(toSave);
            companyAddressRepository.flush();
        }

        log.info("Company address batch imported {} of {} rows", toSave.size(), batch.size());
        exchange.setProperty("batchImported", toSave.size());
    }
}
