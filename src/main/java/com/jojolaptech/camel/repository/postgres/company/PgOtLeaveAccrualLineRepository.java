package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.OtLeaveAccrualLineEntity;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgOtLeaveAccrualLineRepository extends JpaRepository<OtLeaveAccrualLineEntity, UUID> {

    @Query("select l.mysqlId from OtLeaveAccrualLineEntity l where l.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<OtLeaveAccrualLineEntity> findByMysqlId(Long mysqlId);
}
