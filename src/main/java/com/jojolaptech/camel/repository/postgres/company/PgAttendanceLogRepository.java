package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.AttendanceLogEntity;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgAttendanceLogRepository extends JpaRepository<AttendanceLogEntity, UUID> {

    @Query("select l.mysqlId from AttendanceLogEntity l where l.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("""
            select l from AttendanceLogEntity l
            where l.enrollId in :enrollIds and l.logDateTime in :dateTimes
            """)
    List<AttendanceLogEntity> findByEnrollIdInAndLogDateTimeIn(
            @Param("enrollIds") Collection<String> enrollIds,
            @Param("dateTimes") Collection<LocalDateTime> dateTimes);

    boolean existsByEnrollIdAndLogDateTime(String enrollId, LocalDateTime logDateTime);
}
