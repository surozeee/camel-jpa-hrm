package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.TeamEntity;
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
public interface PgTeamRepository extends JpaRepository<TeamEntity, UUID> {

    @Query("select t.mysqlId from TeamEntity t where t.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<TeamEntity> findByMysqlId(Long mysqlId);

    @Query("select t from TeamEntity t where t.mysqlId in :mysqlIds")
    List<TeamEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    List<TeamEntity> findByBranchId(UUID branchId);

    List<TeamEntity> findByCompanyId(UUID companyId);

    List<TeamEntity> findByDepartmentId(UUID departmentId);

    Optional<TeamEntity> findFirstByDepartmentId(UUID departmentId);
}
