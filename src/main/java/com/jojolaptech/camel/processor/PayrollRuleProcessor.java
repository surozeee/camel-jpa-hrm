package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.PayrollCalculationSetting;
import com.jojolaptech.camel.model.postgres.company.CompanyFiscalYearEntity;
import com.jojolaptech.camel.model.postgres.master.PayrollRuleEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyFiscalYearRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.master.PgPayrollRuleRepository;
import java.util.ArrayList;
import java.util.HashMap;
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

@Component
@RequiredArgsConstructor
public class PayrollRuleProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(PayrollRuleProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgCompanyFiscalYearRepository companyFiscalYearRepository;
    private final PgPayrollRuleRepository payrollRuleRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<PayrollCalculationSetting> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            seedMissingPayrollRules();
            exchange.setProperty("batchImported", 0);
            return;
        }

        seedMissingPayrollRules();

        Set<Long> companyMysqlIds = batch.stream()
                .map(setting -> setting.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, UUID> companyIdByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(
                        company -> company.getMysqlId(), company -> company.getId()));

        Map<UUID, UUID> masterFyByCompanyId = new HashMap<>();
        for (Long companyMysqlId : companyMysqlIds) {
            UUID companyId = companyIdByMysqlId.get(companyMysqlId);
            if (companyId == null) {
                continue;
            }
            List<CompanyFiscalYearEntity> fiscalYears =
                    companyFiscalYearRepository.findByCompanyIdOrderByStartDateDesc(companyId);
            if (!fiscalYears.isEmpty()) {
                masterFyByCompanyId.put(companyId, fiscalYears.getFirst().getMasterFiscalYearId());
            }
        }

        Set<UUID> masterFyIds = masterFyByCompanyId.values().stream().collect(Collectors.toSet());
        Map<UUID, PayrollRuleEntity> rulesByMasterFy = payrollRuleRepository.findByFiscalYearIdIn(masterFyIds)
                .stream()
                .collect(Collectors.toMap(PayrollRuleEntity::getFiscalYearId, Function.identity()));

        Set<UUID> missingMasterFyIds = masterFyIds.stream()
                .filter(id -> !rulesByMasterFy.containsKey(id))
                .collect(Collectors.toSet());
        List<PayrollRuleEntity> newRules = new ArrayList<>();
        for (UUID masterFyId : missingMasterFyIds) {
            PayrollRuleEntity rule = FiscalMigrationMapper.defaultPayrollRule(masterFyId);
            newRules.add(rule);
            rulesByMasterFy.put(masterFyId, rule);
        }
        if (!newRules.isEmpty()) {
            payrollRuleRepository.saveAll(newRules);
        }

        int updated = 0;
        for (PayrollCalculationSetting setting : batch) {
            UUID companyId = companyIdByMysqlId.get(setting.getCompany().getId());
            if (companyId == null) {
                continue;
            }
            UUID masterFyId = masterFyByCompanyId.get(companyId);
            if (masterFyId == null) {
                log.warn("Skipping payroll setting id={}, no fiscal year for company mysqlId={}",
                        setting.getId(), setting.getCompany().getId());
                continue;
            }
            PayrollRuleEntity rule = rulesByMasterFy.get(masterFyId);
            if (rule == null) {
                continue;
            }
            FiscalMigrationMapper.applyPayrollParam(
                    rule, setting.getPayrollParamName(), setting.getPayrollParamValue());
            updated++;
        }

        if (updated > 0) {
            payrollRuleRepository.saveAll(rulesByMasterFy.values());
        }

        exchange.setProperty("batchImported", newRules.size() + (updated > 0 ? 1 : 0));
    }

    private void seedMissingPayrollRules() {
        Set<UUID> masterFyIds = companyFiscalYearRepository.findDistinctMasterFiscalYearIds();
        if (masterFyIds.isEmpty()) {
            return;
        }
        Set<UUID> existing = payrollRuleRepository.findExistingFiscalYearIds(masterFyIds);
        List<PayrollRuleEntity> toCreate = masterFyIds.stream()
                .filter(id -> !existing.contains(id))
                .map(FiscalMigrationMapper::defaultPayrollRule)
                .toList();
        if (!toCreate.isEmpty()) {
            payrollRuleRepository.saveAll(toCreate);
        }
    }
}
