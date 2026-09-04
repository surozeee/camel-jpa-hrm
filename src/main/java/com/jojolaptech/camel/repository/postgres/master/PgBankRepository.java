package com.jojolaptech.camel.repository.postgres.master;

import com.jojolaptech.camel.model.postgres.master.BankEntity;
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
public interface PgBankRepository extends JpaRepository<BankEntity, UUID> {

    @Query("select b.mysqlId from BankEntity b where b.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select b from BankEntity b where b.mysqlId in :mysqlIds")
    List<BankEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<BankEntity> findByMysqlId(Long mysqlId);

    @Query("select lower(b.name) from BankEntity b where lower(b.name) in :names")
    Set<String> findExistingNamesLowerCase(@Param("names") Collection<String> names);
}
