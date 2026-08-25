package com.jojolaptech.camel.repository.postgres.master;

import com.jojolaptech.camel.model.postgres.master.FiscalYearEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgFiscalYearRepository extends JpaRepository<FiscalYearEntity, java.util.UUID> {

    Optional<FiscalYearEntity> findByFiscalYear(String fiscalYear);

    @Query("select f.fiscalYear from FiscalYearEntity f where f.fiscalYear in :names")
    Set<String> findExistingFiscalYearNames(@Param("names") Collection<String> names);

    @Query("select f from FiscalYearEntity f where f.fiscalYear in :names")
    List<FiscalYearEntity> findByFiscalYearIn(@Param("names") Collection<String> names);
}
