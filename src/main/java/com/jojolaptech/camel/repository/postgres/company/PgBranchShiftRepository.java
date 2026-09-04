package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.BranchShiftEntity;
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
public interface PgBranchShiftRepository extends JpaRepository<BranchShiftEntity, UUID> {

    @Query("select s.mysqlBranchId from BranchShiftEntity s where s.mysqlBranchId in :mysqlBranchIds")
    Set<Long> findMysqlBranchIdsByMysqlBranchIdIn(@Param("mysqlBranchIds") Collection<Long> mysqlBranchIds);

    Optional<BranchShiftEntity> findByMysqlBranchId(Long mysqlBranchId);

    Optional<BranchShiftEntity> findByMysqlId(Long mysqlId);

    @Query("select s from BranchShiftEntity s where s.mysqlId in :mysqlIds")
    List<BranchShiftEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select s from BranchShiftEntity s where s.mysqlBranchId in :mysqlBranchIds")
    List<BranchShiftEntity> findByMysqlBranchIdIn(@Param("mysqlBranchIds") Collection<Long> mysqlBranchIds);

    @Query("""
            select s.mysqlId, s.mysqlBranchId from BranchShiftEntity s
            where s.mysqlId in :mysqlIds and s.mysqlBranchId in :mysqlBranchIds
            """)
    List<Object[]> findExistingShiftKeys(
            @Param("mysqlIds") Collection<Long> mysqlIds,
            @Param("mysqlBranchIds") Collection<Long> mysqlBranchIds);

    @Query("""
            select s from BranchShiftEntity s
            where s.mysqlId in :mysqlIds and s.mysqlBranchId in :mysqlBranchIds
            """)
    List<BranchShiftEntity> findByMysqlIdInAndMysqlBranchIdIn(
            @Param("mysqlIds") Collection<Long> mysqlIds,
            @Param("mysqlBranchIds") Collection<Long> mysqlBranchIds);
}
