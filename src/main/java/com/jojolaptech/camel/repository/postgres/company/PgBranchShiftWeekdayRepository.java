package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.BranchShiftWeekdayEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgBranchShiftWeekdayRepository extends JpaRepository<BranchShiftWeekdayEntity, UUID> {

    @Query("""
            select w.branchShiftId, w.dayOfWeek from BranchShiftWeekdayEntity w
            where w.branchShiftId in :shiftIds
            """)
    List<Object[]> findExistingKeys(@Param("shiftIds") Collection<UUID> shiftIds);
}
