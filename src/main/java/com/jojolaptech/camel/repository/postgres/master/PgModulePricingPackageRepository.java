package com.jojolaptech.camel.repository.postgres.master;

import com.jojolaptech.camel.model.postgres.master.ModulePricingPackageEntity;
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
public interface PgModulePricingPackageRepository extends JpaRepository<ModulePricingPackageEntity, UUID> {

    @Query("select p.mysqlId from ModulePricingPackageEntity p where p.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select p from ModulePricingPackageEntity p where p.mysqlId in :mysqlIds")
    List<ModulePricingPackageEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<ModulePricingPackageEntity> findByMysqlId(Long mysqlId);

    Optional<ModulePricingPackageEntity> findByPackageCode(String packageCode);
}
