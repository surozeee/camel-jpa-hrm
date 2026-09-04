package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.LeaveTypeEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgLeaveTypeRepository extends JpaRepository<LeaveTypeEntity, java.util.UUID> {

    @Query("select l.mysqlId from LeaveTypeEntity l where l.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select l from LeaveTypeEntity l where l.mysqlId in :mysqlIds")
    List<LeaveTypeEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<LeaveTypeEntity> findByMysqlId(Long mysqlId);

    @Query("select lower(l.name) from LeaveTypeEntity l")
    Set<String> findExistingNamesLowerCase();

    @Query("select l from LeaveTypeEntity l where l.compensationType = true order by l.mysqlId asc")
    List<LeaveTypeEntity> findCompensationTypesOrderByMysqlIdAsc();

    @Query("select l from LeaveTypeEntity l where l.mysqlId is not null order by l.mysqlId asc")
    List<LeaveTypeEntity> findAllWithMysqlIdOrderByMysqlIdAsc();
}
