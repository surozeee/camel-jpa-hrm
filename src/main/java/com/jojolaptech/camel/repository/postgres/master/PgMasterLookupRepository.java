package com.jojolaptech.camel.repository.postgres.master;

import com.jojolaptech.camel.model.postgres.master.MasterLookupEntity;
import com.jojolaptech.camel.model.postgres.master.enums.MasterLookupCategoryEnum;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgMasterLookupRepository extends JpaRepository<MasterLookupEntity, UUID> {

    @Query("select m.mysqlId from MasterLookupEntity m where m.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<MasterLookupEntity> findByCategoryAndCode(MasterLookupCategoryEnum category, String code);
}
