package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.LoanPolicyEntity;
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
public interface PgLoanPolicyRepository extends JpaRepository<LoanPolicyEntity, UUID> {

    @Query("select p.mysqlId from LoanPolicyEntity p where p.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<LoanPolicyEntity> findByCompanyIdAndPolicyCode(UUID companyId, String policyCode);

    @Query("select p from LoanPolicyEntity p where p.companyId in :companyIds and p.policyCode = :policyCode")
    List<LoanPolicyEntity> findByCompanyIdInAndPolicyCode(
            @Param("companyIds") Collection<UUID> companyIds, @Param("policyCode") String policyCode);
}
