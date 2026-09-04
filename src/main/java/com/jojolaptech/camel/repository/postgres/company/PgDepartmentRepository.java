package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.DepartmentEntity;
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
public interface PgDepartmentRepository extends JpaRepository<DepartmentEntity, UUID> {

    @Query("""
            select concat(d.mysqlId, ':', d.mysqlBranchId) from DepartmentEntity d
            where d.mysqlId in :mysqlIds and d.mysqlBranchId in :mysqlBranchIds
            """)
    Set<String> findExistingKeys(
            @Param("mysqlIds") Collection<Long> mysqlIds,
            @Param("mysqlBranchIds") Collection<Long> mysqlBranchIds);

    Optional<DepartmentEntity> findByMysqlIdAndMysqlBranchId(Long mysqlId, Long mysqlBranchId);

    @Query("""
            select d from DepartmentEntity d
            where d.mysqlId in :mysqlIds and d.mysqlBranchId in :mysqlBranchIds
            """)
    List<DepartmentEntity> findByMysqlIdInAndMysqlBranchIdIn(
            @Param("mysqlIds") Collection<Long> mysqlIds,
            @Param("mysqlBranchIds") Collection<Long> mysqlBranchIds);

    List<DepartmentEntity> findByParentDepartmentIsNull();

    List<DepartmentEntity> findByCompanyId(UUID companyId);

    List<DepartmentEntity> findByBranchId(UUID branchId);
}
