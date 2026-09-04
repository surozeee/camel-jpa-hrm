package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.LeaveBalanceEntity;
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
public interface PgLeaveBalanceRepository extends JpaRepository<LeaveBalanceEntity, UUID> {

    @Query("select b.mysqlId from LeaveBalanceEntity b where b.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select b from LeaveBalanceEntity b where b.mysqlId in :mysqlIds")
    List<LeaveBalanceEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<LeaveBalanceEntity> findByEmployeeIdAndLeaveTypeId(UUID employeeId, UUID leaveTypeId);

    @Query("""
            select b from LeaveBalanceEntity b
            where b.employeeId in :employeeIds and b.leaveTypeId in :leaveTypeIds
            """)
    List<LeaveBalanceEntity> findByEmployeeIdInAndLeaveTypeIdIn(
            @Param("employeeIds") Collection<UUID> employeeIds,
            @Param("leaveTypeIds") Collection<UUID> leaveTypeIds);
}
