package com.jojolaptech.camel.repository.postgres.master;

import com.jojolaptech.camel.model.postgres.master.PayrollRuleEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgPayrollRuleRepository extends JpaRepository<PayrollRuleEntity, UUID> {

    Optional<PayrollRuleEntity> findByFiscalYearId(UUID fiscalYearId);

    @Query("select p.fiscalYearId from PayrollRuleEntity p where p.fiscalYearId in :fiscalYearIds")
    Set<UUID> findExistingFiscalYearIds(@Param("fiscalYearIds") Collection<UUID> fiscalYearIds);

    @Query("select p from PayrollRuleEntity p where p.fiscalYearId in :fiscalYearIds")
    List<PayrollRuleEntity> findByFiscalYearIdIn(@Param("fiscalYearIds") Collection<UUID> fiscalYearIds);
}
