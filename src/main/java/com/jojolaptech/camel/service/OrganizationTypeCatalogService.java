package com.jojolaptech.camel.service;

import com.jojolaptech.camel.model.postgres.company.OrganizationTypeEntity;
import com.jojolaptech.camel.model.postgres.company.enums.OrganizationTypeEnum;
import com.jojolaptech.camel.repository.postgres.company.PgOrganizationTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizationTypeCatalogService {

    private final PgOrganizationTypeRepository organizationTypeRepository;

    @Transactional(transactionManager = "postgresTransactionManager")
    public OrganizationTypeEntity defaultOrganizationType() {
        return organizationTypeRepository
                .findByOrganizationType(OrganizationTypeEnum.COMPANY.name())
                .orElseGet(this::seedDefaultType);
    }

    private OrganizationTypeEntity seedDefaultType() {
        OrganizationTypeEnum type = OrganizationTypeEnum.COMPANY;
        return organizationTypeRepository.save(OrganizationTypeEntity.builder()
                .name(type.getDisplayName())
                .organizationType(type.name())
                .description(type.getDescription())
                .displayOrder(1)
                .build());
    }
}
