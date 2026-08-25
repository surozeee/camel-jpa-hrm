package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.LeavePolicyEntity;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgLeavePolicyRepository extends JpaRepository<LeavePolicyEntity, UUID> {

    @Query("select l.mysqlBranchId from LeavePolicyEntity l where l.mysqlBranchId in :mysqlBranchIds")
    Set<Long> findMysqlBranchIdsByMysqlBranchIdIn(@Param("mysqlBranchIds") Collection<Long> mysqlBranchIds);

    @Query("select l from LeavePolicyEntity l where l.mysqlBranchId in :mysqlBranchIds")
    java.util.List<LeavePolicyEntity> findByMysqlBranchIdIn(@Param("mysqlBranchIds") Collection<Long> mysqlBranchIds);
}
