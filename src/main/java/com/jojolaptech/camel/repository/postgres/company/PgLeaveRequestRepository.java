package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.LeaveRequestEntity;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgLeaveRequestRepository extends JpaRepository<LeaveRequestEntity, UUID> {

    @Query("select r.mysqlId from LeaveRequestEntity r where r.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select r from LeaveRequestEntity r where r.mysqlId in :mysqlIds")
    List<LeaveRequestEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select r.mysqlCancellationId from LeaveRequestEntity r where r.mysqlCancellationId in :mysqlIds")
    Set<Long> findMysqlCancellationIdsByMysqlCancellationIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);
}
