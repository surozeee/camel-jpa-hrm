package com.jojolaptech.camel.service;

import com.jojolaptech.camel.model.postgres.company.OrganizationTypeEntity;
import com.jojolaptech.camel.model.postgres.company.enums.OrganizationTypeEnum;
import com.jojolaptech.camel.repository.postgres.company.PgOrganizationTypeRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizationTypeCatalogService {

    private final PgOrganizationTypeRepository organizationTypeRepository;

    @Transactional(transactionManager = "postgresTransactionManager")
    public int ensureCatalog() {
        int imported = 0;
        int displayOrder = 1;
        for (OrganizationTypeEnum type : OrganizationTypeEnum.values()) {
            imported += ensureType(type, displayOrder++);
        }
        return imported;
    }

    @Transactional(transactionManager = "postgresTransactionManager", readOnly = true)
    public OrganizationTypeEntity defaultOrganizationType() {
        return findByCode(OrganizationTypeEnum.COMPANY.name())
                .orElseThrow(() -> new IllegalStateException(
                        "Organization type catalog missing COMPANY; call ensureCatalog() before migration"));
    }

    @Transactional(transactionManager = "postgresTransactionManager", readOnly = true)
    public Optional<OrganizationTypeEntity> findByCode(String organizationTypeCode) {
        if (organizationTypeCode == null || organizationTypeCode.isBlank()) {
            return Optional.empty();
        }
        return organizationTypeRepository.findByOrganizationType(organizationTypeCode.trim().toUpperCase());
    }

    @Transactional(transactionManager = "postgresTransactionManager", readOnly = true)
    public OrganizationTypeEntity resolveOrDefault(String organizationTypeCode) {
        return findByCode(organizationTypeCode).orElseGet(this::defaultOrganizationType);
    }

    private int ensureType(OrganizationTypeEnum type, int displayOrder) {
        if (organizationTypeRepository.findByOrganizationType(type.name()).isPresent()) {
            return 0;
        }
        organizationTypeRepository.save(OrganizationTypeEntity.builder()
                .name(type.getDisplayName())
                .organizationType(type.name())
                .description(type.getDescription())
                .displayOrder(displayOrder)
                .build());
        return 1;
    }
}
