package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.OtLeaveAccrualRuleEntity;
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
public interface PgOtLeaveAccrualRuleRepository extends JpaRepository<OtLeaveAccrualRuleEntity, UUID> {

    @Query("select r.mysqlId from OtLeaveAccrualRuleEntity r where r.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<OtLeaveAccrualRuleEntity> findByMysqlId(Long mysqlId);

    Optional<OtLeaveAccrualRuleEntity> findByBranchId(UUID branchId);

    @Query("select r from OtLeaveAccrualRuleEntity r where r.branchId in :branchIds")
    List<OtLeaveAccrualRuleEntity> findByBranchIdIn(@Param("branchIds") Collection<UUID> branchIds);
}
