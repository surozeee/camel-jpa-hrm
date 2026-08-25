package com.jojolaptech.camel.service;

import com.jojolaptech.camel.model.postgres.enums.FiscalYearTypeEnum;
import com.jojolaptech.camel.model.postgres.enums.MonthTypeEnum;
import com.jojolaptech.camel.model.postgres.master.FiscalYearTypeEntity;
import com.jojolaptech.camel.model.postgres.master.enums.MonthEnum;
import com.jojolaptech.camel.repository.postgres.master.PgFiscalYearTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FiscalYearTypeCatalogService {

    private final PgFiscalYearTypeRepository fiscalYearTypeRepository;

    @Transactional(transactionManager = "postgresTransactionManager")
    public FiscalYearTypeEntity defaultFiscalYearType() {
        return fiscalYearTypeRepository.findByFiscalYearType(FiscalYearTypeEnum.BIKRAM_SAMBAT)
                .orElseGet(this::seedDefaultType);
    }

    private FiscalYearTypeEntity seedDefaultType() {
        FiscalYearTypeEnum type = FiscalYearTypeEnum.BIKRAM_SAMBAT;
        return fiscalYearTypeRepository.save(FiscalYearTypeEntity.builder()
                .name(type.getDisplayName())
                .fiscalYearType(type)
                .monthType(MonthTypeEnum.NEPALI)
                .month(MonthEnum.BAISHAKH)
                .description("Default Bikram Sambat fiscal year type for migrated HRM data")
                .displayOrder(1)
                .build());
    }
}
