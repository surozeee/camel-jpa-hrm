package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.LoanAccountEntity;
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
public interface PgLoanAccountRepository extends JpaRepository<LoanAccountEntity, UUID> {

    @Query("select a.mysqlId from LoanAccountEntity a where a.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select a from LoanAccountEntity a where a.mysqlId in :mysqlIds")
    List<LoanAccountEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<LoanAccountEntity> findByMysqlId(Long mysqlId);
}
