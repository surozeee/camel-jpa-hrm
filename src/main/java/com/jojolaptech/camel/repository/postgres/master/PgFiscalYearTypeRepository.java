package com.jojolaptech.camel.repository.postgres.master;

import com.jojolaptech.camel.model.postgres.master.FiscalYearTypeEntity;
import com.jojolaptech.camel.model.postgres.enums.FiscalYearTypeEnum;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PgFiscalYearTypeRepository extends JpaRepository<FiscalYearTypeEntity, java.util.UUID> {

    Optional<FiscalYearTypeEntity> findByFiscalYearType(FiscalYearTypeEnum fiscalYearType);
}
