package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.PayrollOpeningBalanceEntity;
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
public interface PgPayrollOpeningBalanceRepository extends JpaRepository<PayrollOpeningBalanceEntity, UUID> {

    @Query("select p.mysqlId from PayrollOpeningBalanceEntity p where p.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select p from PayrollOpeningBalanceEntity p where p.mysqlId in :mysqlIds")
    List<PayrollOpeningBalanceEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<PayrollOpeningBalanceEntity> findByMysqlId(Long mysqlId);

    Optional<PayrollOpeningBalanceEntity> findByCompanyIdAndCompanyFiscalYearId(
            UUID companyId, UUID companyFiscalYearId);
}
