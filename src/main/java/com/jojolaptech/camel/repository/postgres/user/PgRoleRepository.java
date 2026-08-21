package com.jojolaptech.camel.repository.postgres.user;

import com.jojolaptech.camel.model.postgres.user.RoleEntity;
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
public interface PgRoleRepository extends JpaRepository<RoleEntity, UUID> {

    Optional<RoleEntity> findByMysqlId(Long mysqlId);

    Optional<RoleEntity> findByNameIgnoreCase(String name);

    @Query("select r.mysqlId from RoleEntity r where r.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select r from RoleEntity r where r.mysqlId in :mysqlIds")
    List<RoleEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select lower(r.name) from RoleEntity r where lower(r.name) in :names")
    Set<String> findExistingNamesIgnoreCase(@Param("names") Collection<String> names);
}
