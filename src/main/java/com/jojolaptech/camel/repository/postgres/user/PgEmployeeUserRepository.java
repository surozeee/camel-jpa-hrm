package com.jojolaptech.camel.repository.postgres.user;

import com.jojolaptech.camel.model.postgres.user.EmployeeUserEntity;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgEmployeeUserRepository extends JpaRepository<EmployeeUserEntity, UUID> {

    @Query("select eu.user.mysqlId from EmployeeUserEntity eu where eu.user.mysqlId in :userMysqlIds")
    Set<Long> findLinkedUserMysqlIds(@Param("userMysqlIds") Collection<Long> userMysqlIds);
}
