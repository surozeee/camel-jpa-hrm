package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.CompanyFiscalYearEntity;
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
public interface PgCompanyFiscalYearRepository extends JpaRepository<CompanyFiscalYearEntity, UUID> {

    @Query("select c.mysqlId from CompanyFiscalYearEntity c where c.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<CompanyFiscalYearEntity> findByMysqlId(Long mysqlId);

    @Query("select c from CompanyFiscalYearEntity c where c.mysqlId in :mysqlIds")
    List<CompanyFiscalYearEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("""
            select c from CompanyFiscalYearEntity c
            where c.companyId = :companyId
            order by c.startDate desc
            """)
    List<CompanyFiscalYearEntity> findByCompanyIdOrderByStartDateDesc(@Param("companyId") UUID companyId);

    @Query("select distinct c.masterFiscalYearId from CompanyFiscalYearEntity c")
    Set<UUID> findDistinctMasterFiscalYearIds();
}
