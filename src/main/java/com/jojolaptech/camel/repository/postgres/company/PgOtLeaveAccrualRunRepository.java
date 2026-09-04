package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.OtLeaveAccrualRunEntity;
import java.time.LocalDate;
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
public interface PgOtLeaveAccrualRunRepository extends JpaRepository<OtLeaveAccrualRunEntity, UUID> {

    @Query("select r.mysqlId from OtLeaveAccrualRunEntity r where r.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<OtLeaveAccrualRunEntity> findByMysqlId(Long mysqlId);

    @Query("select r from OtLeaveAccrualRunEntity r where r.mysqlId in :mysqlIds")
    List<OtLeaveAccrualRunEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<OtLeaveAccrualRunEntity> findByCompanyIdAndPeriodStartAndPeriodEnd(
            UUID companyId, LocalDate periodStart, LocalDate periodEnd);

    @Query("""
            select r from OtLeaveAccrualRunEntity r
            where r.companyId in :companyIds
              and r.periodStart in :periodStarts
              and r.periodEnd in :periodEnds
            """)
    List<OtLeaveAccrualRunEntity> findByCompanyIdInAndPeriodStartInAndPeriodEndIn(
            @Param("companyIds") Collection<UUID> companyIds,
            @Param("periodStarts") Collection<LocalDate> periodStarts,
            @Param("periodEnds") Collection<LocalDate> periodEnds);
}
