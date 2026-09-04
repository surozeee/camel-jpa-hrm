package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.AttendanceEntity;
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
public interface PgAttendanceRepository extends JpaRepository<AttendanceEntity, UUID> {

    @Query("select a.mysqlId from AttendanceEntity a where a.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select a from AttendanceEntity a where a.mysqlId in :mysqlIds")
    List<AttendanceEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<AttendanceEntity> findByMysqlId(Long mysqlId);

    @Query("""
            select a from AttendanceEntity a
            where a.employeeId in :employeeIds and a.attendanceDate in :dates
            """)
    List<AttendanceEntity> findByEmployeeIdInAndAttendanceDateIn(
            @Param("employeeIds") Collection<UUID> employeeIds, @Param("dates") Collection<LocalDate> dates);

    Optional<AttendanceEntity> findByEmployeeIdAndAttendanceDate(UUID employeeId, LocalDate attendanceDate);
}
