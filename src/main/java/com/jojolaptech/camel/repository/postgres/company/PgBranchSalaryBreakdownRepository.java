package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.BranchSalaryBreakdownEntity;
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
public interface PgBranchSalaryBreakdownRepository extends JpaRepository<BranchSalaryBreakdownEntity, UUID> {

    @Query("select b.mysqlId from BranchSalaryBreakdownEntity b where b.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select b from BranchSalaryBreakdownEntity b where b.mysqlId in :mysqlIds")
    List<BranchSalaryBreakdownEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<BranchSalaryBreakdownEntity> findByMysqlId(Long mysqlId);

    @Query("select b from BranchSalaryBreakdownEntity b where b.companyId in :companyIds")
    List<BranchSalaryBreakdownEntity> findByCompanyIdIn(@Param("companyIds") Collection<UUID> companyIds);

    @Query("""
            select b from BranchSalaryBreakdownEntity b
            where b.companyId = :companyId and lower(b.lineName) = lower(:lineName)
            """)
    List<BranchSalaryBreakdownEntity> findByCompanyIdAndLineNameIgnoreCase(
            @Param("companyId") UUID companyId, @Param("lineName") String lineName);
}
