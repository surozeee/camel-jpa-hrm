package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.BranchShiftRuleEntity;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgBranchShiftRuleRepository extends JpaRepository<BranchShiftRuleEntity, UUID> {

    @Query("select r.branchShiftId from BranchShiftRuleEntity r where r.branchShiftId in :branchShiftIds")
    Set<UUID> findExistingBranchShiftIds(@Param("branchShiftIds") Collection<UUID> branchShiftIds);
}
