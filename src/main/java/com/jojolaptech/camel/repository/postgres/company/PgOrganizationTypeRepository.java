package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.OrganizationTypeEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PgOrganizationTypeRepository extends JpaRepository<OrganizationTypeEntity, UUID> {

    Optional<OrganizationTypeEntity> findByOrganizationType(String organizationType);
}
