package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.CompanyTypeEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PgCompanyTypeRepository extends JpaRepository<CompanyTypeEntity, UUID> {

    Optional<CompanyTypeEntity> findByCompanyType(String companyType);
}
