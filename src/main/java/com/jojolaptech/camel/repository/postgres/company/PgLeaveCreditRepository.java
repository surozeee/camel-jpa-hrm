package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.LeaveCreditEntity;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgLeaveCreditRepository extends JpaRepository<LeaveCreditEntity, UUID> {

    @Query("select c.mysqlId from LeaveCreditEntity c where c.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<LeaveCreditEntity> findByMysqlId(Long mysqlId);

    Optional<LeaveCreditEntity> findByIdempotencyKey(String idempotencyKey);
}
