package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.OrganizationEntity;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgOrganizationRepository extends JpaRepository<OrganizationEntity, UUID> {

    @Query("select o from OrganizationEntity o where o.code in :codes")
    List<OrganizationEntity> findByCodeIn(@Param("codes") Collection<String> codes);

    @Query("select o.code from OrganizationEntity o where o.code in :codes")
    Set<String> findExistingCodes(@Param("codes") Collection<String> codes);
}
