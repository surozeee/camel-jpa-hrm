package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.BranchEntity;
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
public interface PgBranchRepository extends JpaRepository<BranchEntity, UUID> {

    @Query("select b.mysqlId from BranchEntity b where b.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("""
            select b from BranchEntity b
            left join fetch b.branchAddress
            left join fetch b.company
            where b.mysqlId in :mysqlIds
            """)
    List<BranchEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<BranchEntity> findByMysqlId(Long mysqlId);

    @Query("""
            select b from BranchEntity b join fetch b.company
            where b.id in :ids
            """)
    List<BranchEntity> findByIdInWithCompany(@Param("ids") Collection<UUID> ids);

    @Query("""
            select b from BranchEntity b
            where b.company.mysqlId = :companyMysqlId
            order by b.mysqlId asc
            """)
    List<BranchEntity> findByCompanyMysqlIdOrderByMysqlIdAsc(@Param("companyMysqlId") Long companyMysqlId);

    @Query("""
            select b from BranchEntity b join fetch b.company c
            where c.mysqlId in :companyMysqlIds
            order by b.mysqlId asc
            """)
    List<BranchEntity> findByCompanyMysqlIdIn(@Param("companyMysqlIds") Collection<Long> companyMysqlIds);

    @Query("""
            select b from BranchEntity b join fetch b.company c
            where c.id in :companyIds
            order by b.mysqlId asc
            """)
    List<BranchEntity> findByCompanyIdInOrderByMysqlIdAsc(@Param("companyIds") Collection<UUID> companyIds);
}
