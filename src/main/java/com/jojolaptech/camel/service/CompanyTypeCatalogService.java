package com.jojolaptech.camel.service;

import com.jojolaptech.camel.model.postgres.company.CompanyTypeEntity;
import com.jojolaptech.camel.model.postgres.company.enums.CompanyTypeEnum;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyTypeRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyTypeCatalogService {

    private final PgCompanyTypeRepository companyTypeRepository;

    @Transactional(transactionManager = "postgresTransactionManager")
    public int ensureCatalog() {
        int imported = 0;
        int displayOrder = 1;
        for (CompanyTypeEnum type : CompanyTypeEnum.values()) {
            imported += ensureType(type, displayOrder++);
        }
        return imported;
    }

    @Transactional(transactionManager = "postgresTransactionManager", readOnly = true)
    public CompanyTypeEntity defaultCompanyType() {
        return findByCode(CompanyTypeEnum.SERVICE.name())
                .orElseThrow(() -> new IllegalStateException(
                        "Company type catalog missing SERVICE; call ensureCatalog() before migration"));
    }

    @Transactional(transactionManager = "postgresTransactionManager", readOnly = true)
    public Optional<CompanyTypeEntity> findByCode(String companyTypeCode) {
        if (companyTypeCode == null || companyTypeCode.isBlank()) {
            return Optional.empty();
        }
        return companyTypeRepository.findByCompanyType(companyTypeCode.trim().toUpperCase());
    }

    @Transactional(transactionManager = "postgresTransactionManager", readOnly = true)
    public CompanyTypeEntity resolveOrDefault(String companyTypeCode) {
        return findByCode(companyTypeCode).orElseGet(this::defaultCompanyType);
    }

    private int ensureType(CompanyTypeEnum type, int displayOrder) {
        if (companyTypeRepository.findByCompanyType(type.name()).isPresent()) {
            return 0;
        }
        companyTypeRepository.save(CompanyTypeEntity.builder()
                .name(type.getDisplayName())
                .companyType(type.name())
                .description(type.getDescription())
                .displayOrder(displayOrder)
                .build());
        return 1;
    }
}
