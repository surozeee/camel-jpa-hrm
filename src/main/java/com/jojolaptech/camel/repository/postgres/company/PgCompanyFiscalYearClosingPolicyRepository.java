package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.CompanyFiscalYearClosingPolicyEntity;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgCompanyFiscalYearClosingPolicyRepository
        extends JpaRepository<CompanyFiscalYearClosingPolicyEntity, UUID> {

    Optional<CompanyFiscalYearClosingPolicyEntity> findByCompanyId(UUID companyId);

    @Query("select p.companyId from CompanyFiscalYearClosingPolicyEntity p where p.companyId in :companyIds")
    Set<UUID> findExistingCompanyIds(@Param("companyIds") Collection<UUID> companyIds);
}
