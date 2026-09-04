package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.CostCenterEntity;
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
public interface PgCostCenterRepository extends JpaRepository<CostCenterEntity, UUID> {

    @Query("select c.mysqlId from CostCenterEntity c where c.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<CostCenterEntity> findByMysqlId(Long mysqlId);

    @Query("select c from CostCenterEntity c where c.mysqlId in :mysqlIds")
    List<CostCenterEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    List<CostCenterEntity> findByBranchId(UUID branchId);

    Optional<CostCenterEntity> findFirstByBranchId(UUID branchId);

    List<CostCenterEntity> findByCompanyIdAndBranchIdIsNull(UUID companyId);

    Optional<CostCenterEntity> findFirstByCompanyIdAndBranchIdIsNull(UUID companyId);

    List<CostCenterEntity> findByCompanyId(UUID companyId);
}
