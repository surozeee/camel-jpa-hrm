package com.jojolaptech.camel.repository.postgres.user;

import com.jojolaptech.camel.model.postgres.user.CompanySubscriptionEntity;
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
public interface PgCompanySubscriptionRepository extends JpaRepository<CompanySubscriptionEntity, UUID> {

    @Query("select s.mysqlId from CompanySubscriptionEntity s where s.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<CompanySubscriptionEntity> findByMysqlId(Long mysqlId);

    Optional<CompanySubscriptionEntity> findByCompanyId(UUID companyId);

    @Query("select s from CompanySubscriptionEntity s where s.companyId in :companyIds")
    List<CompanySubscriptionEntity> findByCompanyIdIn(@Param("companyIds") Collection<UUID> companyIds);
}
