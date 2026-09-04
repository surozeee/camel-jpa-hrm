package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.CompanyNoticeEntity;
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
public interface PgCompanyNoticeRepository extends JpaRepository<CompanyNoticeEntity, UUID> {

    @Query("select e.mysqlId from CompanyNoticeEntity e where e.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<CompanyNoticeEntity> findByMysqlId(Long mysqlId);

    @Query("select e from CompanyNoticeEntity e where e.mysqlId in :mysqlIds")
    List<CompanyNoticeEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);
}
