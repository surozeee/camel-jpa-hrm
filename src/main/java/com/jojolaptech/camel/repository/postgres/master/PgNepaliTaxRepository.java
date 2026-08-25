package com.jojolaptech.camel.repository.postgres.master;

import com.jojolaptech.camel.model.postgres.master.NepaliTaxEntity;
import com.jojolaptech.camel.model.postgres.master.enums.TaxMaritalStatusEnum;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgNepaliTaxRepository extends JpaRepository<NepaliTaxEntity, UUID> {

    Optional<NepaliTaxEntity> findByMaritalStatusAndFiscalYearId(
            TaxMaritalStatusEnum maritalStatus, UUID fiscalYearId);

    @Query("""
            select t from NepaliTaxEntity t
            left join fetch t.rates
            where t.fiscalYearId in :fiscalYearIds
            """)
    List<NepaliTaxEntity> findByFiscalYearIdInWithRates(@Param("fiscalYearIds") Collection<UUID> fiscalYearIds);
}
