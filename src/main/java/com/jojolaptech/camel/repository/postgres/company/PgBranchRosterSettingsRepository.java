package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.BranchRosterSettingsEntity;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgBranchRosterSettingsRepository extends JpaRepository<BranchRosterSettingsEntity, UUID> {

    @Query("select r.branchId from BranchRosterSettingsEntity r where r.branchId in :branchIds")
    Set<UUID> findExistingBranchIds(@Param("branchIds") Collection<UUID> branchIds);
}
