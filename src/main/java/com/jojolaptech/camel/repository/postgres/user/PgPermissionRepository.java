package com.jojolaptech.camel.repository.postgres.user;

import com.jojolaptech.camel.model.postgres.user.PermissionEntity;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgPermissionRepository extends JpaRepository<PermissionEntity, UUID> {

    @Query("select p.mysqlId from PermissionEntity p where p.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select p from PermissionEntity p where p.mysqlId in :mysqlIds")
    List<PermissionEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    boolean existsByCode(String code);

    @Query("select p.code from PermissionEntity p where p.code in :codes")
    Set<String> findExistingCodes(@Param("codes") Collection<String> codes);
}
