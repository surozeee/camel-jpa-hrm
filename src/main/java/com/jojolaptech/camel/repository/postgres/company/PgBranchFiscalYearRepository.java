package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.BranchFiscalYearEntity;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgBranchFiscalYearRepository extends JpaRepository<BranchFiscalYearEntity, java.util.UUID> {

    @Query("""
            select b.mysqlId, b.mysqlBranchId from BranchFiscalYearEntity b
            where b.mysqlId in :mysqlIds
            """)
    List<Object[]> findExistingKeyPairsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);
}
