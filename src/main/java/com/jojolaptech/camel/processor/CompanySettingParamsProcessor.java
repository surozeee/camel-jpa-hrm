package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanySettingParams;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyFiscalYearEntity;
import com.jojolaptech.camel.model.postgres.master.MasterLookupEntity;
import com.jojolaptech.camel.model.postgres.master.PayrollRuleEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyFiscalYearRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.master.PgMasterLookupRepository;
import com.jojolaptech.camel.repository.postgres.master.PgPayrollRuleRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Step 18b: companySettingParams → master_lookup (COMPANY_SETTING_PARAM) + apply known keys to
 * CompanyEntity enable_* flags and PayrollRuleEntity.
 */
@Component
@RequiredArgsConstructor
public class CompanySettingParamsProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(CompanySettingParamsProcessor.class);

    private final PgMasterLookupRepository lookupRepository;
    private final PgCompanyRepository companyRepository;
    private final PgCompanyFiscalYearRepository companyFiscalYearRepository;
    private final PgPayrollRuleRepository payrollRuleRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<CompanySettingParams> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream()
                .map(s -> CompanyParamsLeftoversMigrationMapper.COMPANY_SETTING_PARAM_MYSQL_ID_OFFSET + s.getId())
                .collect(Collectors.toSet());
        Set<Long> existing = lookupRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> companyMysqlIds = batch.stream()
                .filter(s -> s.getCompany() != null)
                .map(s -> s.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companies = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, Function.identity(), (a, b) -> a));

        Map<UUID, UUID> masterFyByCompanyId = new HashMap<>();
        for (CompanyEntity company : companies.values()) {
            List<CompanyFiscalYearEntity> fys =
                    companyFiscalYearRepository.findByCompanyIdOrderByStartDateDesc(company.getId());
            if (!fys.isEmpty()) {
                masterFyByCompanyId.put(company.getId(), fys.getFirst().getMasterFiscalYearId());
            }
        }
        Set<UUID> masterFyIds = new HashSet<>(masterFyByCompanyId.values());
        Map<UUID, PayrollRuleEntity> rulesByMasterFy = masterFyIds.isEmpty()
                ? Map.of()
                : payrollRuleRepository.findByFiscalYearIdIn(masterFyIds).stream()
                        .collect(Collectors.toMap(PayrollRuleEntity::getFiscalYearId, Function.identity(), (a, b) -> a));

        List<MasterLookupEntity> toSave = new ArrayList<>();
        Set<CompanyEntity> companiesChanged = new HashSet<>();
        Set<PayrollRuleEntity> rulesChanged = new HashSet<>();
        int applied = 0;

        for (CompanySettingParams source : batch) {
            long mysqlId =
                    CompanyParamsLeftoversMigrationMapper.COMPANY_SETTING_PARAM_MYSQL_ID_OFFSET + source.getId();
            if (!existing.contains(mysqlId)) {
                MasterLookupEntity mapped =
                        CompanyParamsLeftoversMigrationMapper.fromCompanySettingParam(source);
                if (mapped == null) {
                    log.warn("Skipping companySettingParams id={}, mapping failed", source.getId());
                } else if (lookupRepository.findByCategoryAndCode(mapped.getCategory(), mapped.getCode()).isPresent()) {
                    log.warn("Skipping companySettingParams id={}, code exists", source.getId());
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
                        "Skipping apply for companySettingParams id={}, company mysqlId={} not migrated",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }

            boolean companyUpdated = CompanyParamsLeftoversMigrationMapper.applyCompanyEnableFlag(
                    company, source.getCompanyParamName(), source.getCompanyParamValue());
            if (companyUpdated) {
                companiesChanged.add(company);
                applied++;
            }

            UUID masterFyId = masterFyByCompanyId.get(company.getId());
            PayrollRuleEntity rule = masterFyId != null ? rulesByMasterFy.get(masterFyId) : null;
            if (rule != null
                    && CompanyParamsLeftoversMigrationMapper.applyKnownPayrollParam(
                            rule, source.getCompanyParamName(), source.getCompanyParamValue())) {
                rulesChanged.add(rule);
                applied++;
            }
        }

        if (!toSave.isEmpty()) {
            lookupRepository.saveAll(toSave);
        }
        if (!companiesChanged.isEmpty()) {
            companyRepository.saveAll(companiesChanged);
        }
        if (!rulesChanged.isEmpty()) {
            payrollRuleRepository.saveAll(rulesChanged);
        }
        exchange.setProperty("batchImported", toSave.size() + applied);
    }
}
