package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyPayroll;
import com.jojolaptech.camel.model.postgres.company.CompanyBankEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.master.BankEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyBankRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.master.PgBankRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
 * Step 9m: companyPayroll → hrm_company_bank (mysql_id = 45e12+id). Resolve BankEntity by code then
 * name; isPrimary=true for first bank per company when none exist.
 */
@Component
@RequiredArgsConstructor
public class CompanyPayrollBankProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(CompanyPayrollBankProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgBankRepository bankRepository;
    private final PgCompanyBankRepository companyBankRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<CompanyPayroll> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream()
                .map(s -> PayrollCatalogLeftoversMigrationMapper.COMPANY_PAYROLL_BANK_MYSQL_ID_OFFSET + s.getId())
                .collect(Collectors.toSet());
        Set<Long> existing = companyBankRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> companyMysqlIds = batch.stream()
                .filter(s -> s.getCompany() != null)
                .map(s -> s.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companies = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, Function.identity(), (a, b) -> a));

        Set<String> bankCodes = batch.stream()
                .map(CompanyPayroll::getBankCode)
                .filter(c -> c != null && !c.isBlank())
                .map(c -> c.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        Map<String, BankEntity> bankByCode = bankCodes.isEmpty()
                ? Map.of()
                : bankRepository.findByCodeIgnoreCaseIn(bankCodes).stream()
                        .filter(b -> b.getCode() != null)
                        .collect(Collectors.toMap(
                                b -> b.getCode().toLowerCase(Locale.ROOT), Function.identity(), (a, b) -> a));

        Set<String> bankNames = batch.stream()
                .map(CompanyPayroll::getBankCode)
                .filter(c -> c != null && !c.isBlank())
                .map(c -> c.trim().toLowerCase(Locale.ROOT))
                .filter(c -> !bankByCode.containsKey(c))
                .collect(Collectors.toSet());
        Map<String, BankEntity> bankByName = bankNames.isEmpty()
                ? Map.of()
                : bankRepository.findByNameIgnoreCaseIn(bankNames).stream()
                        .filter(b -> b.getName() != null)
                        .collect(Collectors.toMap(
                                b -> b.getName().toLowerCase(Locale.ROOT), Function.identity(), (a, b) -> a));

        Set<UUID> companyUuids = companies.values().stream().map(CompanyEntity::getId).collect(Collectors.toSet());
        Map<UUID, Boolean> hasPrimary = new HashMap<>();
        Set<UUID> companiesWithAnyBank = new HashSet<>();
        if (!companyUuids.isEmpty()) {
            for (CompanyBankEntity existingBank : companyBankRepository.findByCompanyIdIn(companyUuids)) {
                companiesWithAnyBank.add(existingBank.getCompanyId());
                if (Boolean.TRUE.equals(existingBank.getIsPrimary())) {
                    hasPrimary.put(existingBank.getCompanyId(), true);
                }
            }
        }

        List<CompanyBankEntity> toSave = new ArrayList<>();
        for (CompanyPayroll source : batch) {
            long mysqlId =
                    PayrollCatalogLeftoversMigrationMapper.COMPANY_PAYROLL_BANK_MYSQL_ID_OFFSET + source.getId();
            if (existing.contains(mysqlId)) {
                continue;
            }
            if (source.getCompany() == null) {
                log.warn("Skipping companyPayroll id={}, missing company", source.getId());
                continue;
            }
            CompanyEntity company = companies.get(source.getCompany().getId());
            if (company == null) {
                log.warn(
                        "Skipping companyPayroll id={}, company mysqlId={} not migrated",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }

            BankEntity bank = resolveBank(source.getBankCode(), bankByCode, bankByName);
            if (bank == null) {
                log.warn(
                        "Skipping companyPayroll id={}, bank not found for code={}",
                        source.getId(),
                        source.getBankCode());
                continue;
            }

            boolean makePrimary = !Boolean.TRUE.equals(hasPrimary.get(company.getId()))
                    && !companiesWithAnyBank.contains(company.getId());
            CompanyBankEntity mapped = PayrollCatalogLeftoversMigrationMapper.fromCompanyPayroll(
                    source, company.getId(), bank.getId(), makePrimary);
            if (mapped == null) {
                log.warn("Skipping companyPayroll id={}, mapping failed", source.getId());
                continue;
            }
            toSave.add(mapped);
            existing.add(mysqlId);
            companiesWithAnyBank.add(company.getId());
            if (makePrimary) {
                hasPrimary.put(company.getId(), true);
            }
        }

        if (!toSave.isEmpty()) {
            companyBankRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }

    private static BankEntity resolveBank(
            String bankCode, Map<String, BankEntity> byCode, Map<String, BankEntity> byName) {
        if (bankCode == null || bankCode.isBlank()) {
            return null;
        }
        String key = bankCode.trim().toLowerCase(Locale.ROOT);
        BankEntity bank = byCode.get(key);
        if (bank != null) {
            return bank;
        }
        return byName.get(key);
    }
}
