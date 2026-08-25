package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.BranchHolidayEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgBranchHolidayRepository extends JpaRepository<BranchHolidayEntity, UUID> {

    @Query("""
            select h.mysqlId, h.branchId from BranchHolidayEntity h
            where h.mysqlId in :mysqlIds and h.branchId in :branchIds
            """)
    List<Object[]> findExistingKeys(
            @Param("mysqlIds") Collection<Long> mysqlIds, @Param("branchIds") Collection<UUID> branchIds);
}
