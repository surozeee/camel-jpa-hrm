package com.jojolaptech.camel.repository.postgres.user;

import com.jojolaptech.camel.model.postgres.user.UserDetailEntity;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgUserDetailRepository extends JpaRepository<UserDetailEntity, UUID> {

    @Query("select u.mysqlId from UserDetailEntity d join d.user u where u.mysqlId in :mysqlIds")
    Set<Long> findUserMysqlIdsWithDetail(@Param("mysqlIds") Collection<Long> mysqlIds);
}
