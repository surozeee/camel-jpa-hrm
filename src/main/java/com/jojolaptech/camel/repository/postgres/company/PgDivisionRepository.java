package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.DivisionEntity;
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
public interface PgDivisionRepository extends JpaRepository<DivisionEntity, UUID> {

    @Query("select d.mysqlId from DivisionEntity d where d.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<DivisionEntity> findByMysqlId(Long mysqlId);

    @Query("select d from DivisionEntity d where d.mysqlId in :mysqlIds")
    List<DivisionEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    List<DivisionEntity> findByBranchId(UUID branchId);

    List<DivisionEntity> findByCompanyId(UUID companyId);
}
