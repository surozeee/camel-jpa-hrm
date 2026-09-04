package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.RosterShiftSlotEntity;
import com.jojolaptech.camel.model.postgres.company.enums.RosterShiftSlotEnum;
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
public interface PgRosterShiftSlotRepository extends JpaRepository<RosterShiftSlotEntity, UUID> {

    @Query("select s.mysqlId from RosterShiftSlotEntity s where s.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<RosterShiftSlotEntity> findByBranchIdAndShiftSlot(UUID branchId, RosterShiftSlotEnum shiftSlot);

    @Query("""
            select s from RosterShiftSlotEntity s
            where s.branchId in :branchIds
            """)
    List<RosterShiftSlotEntity> findByBranchIdIn(@Param("branchIds") Collection<UUID> branchIds);
}
