package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyAdminParams;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.master.MasterLookupEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.master.PgMasterLookupRepository;
import java.util.ArrayList;
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

/**
 * Step 18c: companyAdminParams → master_lookup (COMPANY_ADMIN_PARAM) + apply known keys to
 * source.getCompany().
 */
@Component
@RequiredArgsConstructor
public class CompanyAdminParamsProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(CompanyAdminParamsProcessor.class);

    private final PgMasterLookupRepository lookupRepository;
    private final PgCompanyRepository companyRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<CompanyAdminParams> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream()
                .map(s -> CompanyParamsLeftoversMigrationMapper.COMPANY_ADMIN_PARAM_MYSQL_ID_OFFSET + s.getId())
                .collect(Collectors.toSet());
        Set<Long> existing = lookupRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> companyMysqlIds = batch.stream()
                .filter(s -> s.getCompany() != null)
                .map(s -> s.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companies = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, Function.identity(), (a, b) -> a));

        List<MasterLookupEntity> toSave = new ArrayList<>();
        Set<CompanyEntity> companiesChanged = new HashSet<>();
        int applied = 0;

        for (CompanyAdminParams source : batch) {
            long mysqlId =
                    CompanyParamsLeftoversMigrationMapper.COMPANY_ADMIN_PARAM_MYSQL_ID_OFFSET + source.getId();
            if (!existing.contains(mysqlId)) {
                MasterLookupEntity mapped =
                        CompanyParamsLeftoversMigrationMapper.fromCompanyAdminParam(source);
                if (mapped == null) {
                    log.warn("Skipping companyAdminParams id={}, mapping failed", source.getId());
                } else if (lookupRepository.findByCategoryAndCode(mapped.getCategory(), mapped.getCode()).isPresent()) {
                    log.warn("Skipping companyAdminParams id={}, code exists", source.getId());
                } else {
                    toSave.add(mapped);
                    existing.add(mysqlId);
                }
            }

            if (source.getCompany() == null) {
                continue;
            }
            CompanyEntity company = companies.get(source.getCompany().getId());
            if (company == null) {
                log.warn(
                        "Skipping apply for companyAdminParams id={}, company mysqlId={} not migrated",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }
            if (CompanyParamsLeftoversMigrationMapper.applyCompanyEnableFlag(
                    company, source.getParamName(), source.getParamValue())) {
                companiesChanged.add(company);
                applied++;
            }
        }

        if (!toSave.isEmpty()) {
            lookupRepository.saveAll(toSave);
        }
        if (!companiesChanged.isEmpty()) {
            companyRepository.saveAll(companiesChanged);
        }
        exchange.setProperty("batchImported", toSave.size() + applied);
    }
}
