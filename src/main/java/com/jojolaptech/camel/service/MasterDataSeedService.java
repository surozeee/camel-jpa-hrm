package com.jojolaptech.camel.service;

import com.jojolaptech.camel.model.postgres.enums.CountryEnum;
import com.jojolaptech.camel.model.postgres.enums.CurrencyEnum;
import com.jojolaptech.camel.model.postgres.master.BankTypeEntity;
import com.jojolaptech.camel.model.postgres.master.CountryEntity;
import com.jojolaptech.camel.model.postgres.master.CurrencyEntity;
import com.jojolaptech.camel.model.postgres.master.EmploymentTypeEntity;
import com.jojolaptech.camel.model.postgres.master.GenderEntity;
import com.jojolaptech.camel.model.postgres.master.SalutationEntity;
import com.jojolaptech.camel.repository.postgres.master.PgBankTypeRepository;
import com.jojolaptech.camel.repository.postgres.master.PgCountryRepository;
import com.jojolaptech.camel.repository.postgres.master.PgCurrencyRepository;
import com.jojolaptech.camel.repository.postgres.master.PgEmploymentTypeRepository;
import com.jojolaptech.camel.repository.postgres.master.PgGenderRepository;
import com.jojolaptech.camel.repository.postgres.master.PgSalutationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MasterDataSeedService {

    private static final Logger log = LoggerFactory.getLogger(MasterDataSeedService.class);

    private final PgCurrencyRepository currencyRepository;
    private final PgCountryRepository countryRepository;
    private final PgGenderRepository genderRepository;
    private final PgSalutationRepository salutationRepository;
    private final PgEmploymentTypeRepository employmentTypeRepository;
    private final PgBankTypeRepository bankTypeRepository;
    private final FiscalYearTypeCatalogService fiscalYearTypeCatalogService;
    private final CompanyTypeCatalogService companyTypeCatalogService;
    private final OrganizationTypeCatalogService organizationTypeCatalogService;

    @Transactional(transactionManager = "postgresTransactionManager")
    public int seedAll() {
        int count = 0;
        count += seedCurrency();
        count += seedCountry();
        count += seedGenders();
        count += seedSalutations();
        count += seedEmploymentTypes();
        count += seedBankTypes();
        fiscalYearTypeCatalogService.defaultFiscalYearType();
        count++;
        count += companyTypeCatalogService.ensureCatalog();
        count += organizationTypeCatalogService.ensureCatalog();
        log.info("Master platform seed completed, {} records ensured", count);
        return count;
    }

    private int seedCurrency() {
        if (currencyRepository.findByCode(CurrencyEnum.NPR).isPresent()) {
            return 0;
        }
        currencyRepository.save(CurrencyEntity.builder()
                .name("Nepalese Rupee")
                .code(CurrencyEnum.NPR)
                .build());
        return 1;
    }

    private int seedCountry() {
        if (countryRepository.findByIso2(CountryEnum.NP).isPresent()) {
            return 0;
        }
        CurrencyEntity currency = currencyRepository.findByCode(CurrencyEnum.NPR).orElse(null);
        countryRepository.save(CountryEntity.builder()
                .name("Nepal")
                .nationality("Nepali")
                .iso2(CountryEnum.NP)
                .iso3("NPL")
                .teleCode("+977")
                .baseCurrency(currency)
                .build());
        return 1;
    }

    private int seedGenders() {
        int imported = 0;
        imported += ensureGender("Male", "M");
        imported += ensureGender("Female", "F");
        imported += ensureGender("Other", "O");
        return imported;
    }

    private int seedSalutations() {
        int imported = 0;
        imported += ensureSalutation("Mr", "MR");
        imported += ensureSalutation("Mrs", "MRS");
        imported += ensureSalutation("Ms", "MS");
        return imported;
    }

    private int seedEmploymentTypes() {
        int imported = 0;
        imported += ensureEmploymentType("Full Time", "FULL_TIME");
        imported += ensureEmploymentType("Part Time", "PART_TIME");
        imported += ensureEmploymentType("Contract", "CONTRACT");
        imported += ensureEmploymentType("Intern", "INTERN");
        return imported;
    }

    private int seedBankTypes() {
        int imported = 0;
        imported += ensureBankType("Commercial Bank");
        imported += ensureBankType("Development Bank");
        imported += ensureBankType("Central Bank");
        return imported;
    }

    private int ensureGender(String name, String code) {
        if (genderRepository.findByCode(code).isPresent()) {
            return 0;
        }
        genderRepository.save(GenderEntity.builder().name(name).code(code).build());
        return 1;
    }

    private int ensureSalutation(String name, String code) {
        if (salutationRepository.findByCode(code).isPresent()) {
            return 0;
        }
        salutationRepository.save(SalutationEntity.builder().name(name).code(code).build());
        return 1;
    }

    private int ensureEmploymentType(String name, String code) {
        if (employmentTypeRepository.findByCode(code).isPresent()) {
            return 0;
        }
        employmentTypeRepository.save(EmploymentTypeEntity.builder()
                .name(name)
                .code(code)
                .build());
        return 1;
    }

    private int ensureBankType(String name) {
        if (bankTypeRepository.findAll().stream().anyMatch(row -> name.equals(row.getName()))) {
            return 0;
        }
        bankTypeRepository.save(BankTypeEntity.builder().name(name).build());
        return 1;
    }
}
