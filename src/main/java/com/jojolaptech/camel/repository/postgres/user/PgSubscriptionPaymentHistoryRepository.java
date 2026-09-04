package com.jojolaptech.camel.repository.postgres.user;

import com.jojolaptech.camel.model.postgres.user.SubscriptionPaymentHistoryEntity;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgSubscriptionPaymentHistoryRepository
        extends JpaRepository<SubscriptionPaymentHistoryEntity, UUID> {

    @Query("select p.mysqlId from SubscriptionPaymentHistoryEntity p where p.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select p from SubscriptionPaymentHistoryEntity p where p.mysqlId in :mysqlIds")
    List<SubscriptionPaymentHistoryEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);
}
