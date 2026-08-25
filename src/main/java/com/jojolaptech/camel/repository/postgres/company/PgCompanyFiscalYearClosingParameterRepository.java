package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.CompanyFiscalYearClosingParameterEntity;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgCompanyFiscalYearClosingParameterRepository
        extends JpaRepository<CompanyFiscalYearClosingParameterEntity, UUID> {

    @Query("select p.mysqlId from CompanyFiscalYearClosingParameterEntity p where p.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);
}
