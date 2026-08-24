package com.jojolaptech.camel.service;

import com.jojolaptech.camel.model.postgres.company.CompanyTypeEntity;
import com.jojolaptech.camel.model.postgres.company.enums.CompanyTypeEnum;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyTypeCatalogService {

    private final PgCompanyTypeRepository companyTypeRepository;

    @Transactional(transactionManager = "postgresTransactionManager")
    public CompanyTypeEntity defaultCompanyType() {
        return companyTypeRepository.findByCompanyType(CompanyTypeEnum.SERVICE.name())
                .orElseGet(this::seedDefaultType);
    }

    private CompanyTypeEntity seedDefaultType() {
        CompanyTypeEnum type = CompanyTypeEnum.SERVICE;
        return companyTypeRepository.save(CompanyTypeEntity.builder()
                .name(type.getDisplayName())
                .companyType(type.name())
                .description(type.getDescription())
                .displayOrder(1)
                .build());
    }
}
