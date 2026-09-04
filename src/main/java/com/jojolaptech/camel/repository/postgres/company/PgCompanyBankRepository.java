package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.CompanyBankEntity;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgCompanyBankRepository extends JpaRepository<CompanyBankEntity, UUID> {

    @Query("select c.mysqlId from CompanyBankEntity c where c.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select c from CompanyBankEntity c where c.companyId in :companyIds")
    List<CompanyBankEntity> findByCompanyIdIn(@Param("companyIds") Collection<UUID> companyIds);
}
