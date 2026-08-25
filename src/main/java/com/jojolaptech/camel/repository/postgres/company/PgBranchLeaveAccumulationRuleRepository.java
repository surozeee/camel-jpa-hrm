package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.BranchLeaveAccumulationRuleEntity;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgBranchLeaveAccumulationRuleRepository
        extends JpaRepository<BranchLeaveAccumulationRuleEntity, UUID> {

    @Query("select r.mysqlId from BranchLeaveAccumulationRuleEntity r where r.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select r.branchLeaveType.id from BranchLeaveAccumulationRuleEntity r where r.branchLeaveType.id in :ids")
    Set<UUID> findExistingBranchLeaveTypeIds(@Param("ids") Collection<UUID> ids);
}
