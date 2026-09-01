package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
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
public interface PgCompanyRepository extends JpaRepository<CompanyEntity, UUID> {

    @Query("select c.mysqlId from CompanyEntity c where c.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select c from CompanyEntity c where c.mysqlId in :mysqlIds")
    List<CompanyEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<CompanyEntity> findByMysqlId(Long mysqlId);

    @Query("select lower(c.name) from CompanyEntity c where lower(c.name) in :names")
    Set<String> findExistingNamesIgnoreCase(@Param("names") Collection<String> names);

    @Query("""
            select c from CompanyEntity c
            where c.email is not null and lower(trim(c.email)) in :emails
            """)
    List<CompanyEntity> findByEmailIgnoreCaseIn(@Param("emails") Collection<String> emails);
}
