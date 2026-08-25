package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.BranchLeaveTypeEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgBranchLeaveTypeRepository extends JpaRepository<BranchLeaveTypeEntity, java.util.UUID> {

    @Query("""
            select b.mysqlBranchId, b.mysqlLeaveId from BranchLeaveTypeEntity b
            where b.mysqlBranchId in :branchMysqlIds and b.mysqlLeaveId in :leaveMysqlIds
            """)
    List<Object[]> findExistingKeys(
            @Param("branchMysqlIds") Collection<Long> branchMysqlIds,
            @Param("leaveMysqlIds") Collection<Long> leaveMysqlIds);
}
