package com.jojolaptech.camel.repository.postgres.user;

import com.jojolaptech.camel.model.postgres.user.UserEntity;
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
public interface PgUserRepository extends JpaRepository<UserEntity, UUID> {

    boolean existsByMysqlId(Long mysqlId);

    Optional<UserEntity> findByMysqlId(Long mysqlId);

    Optional<UserEntity> findByEmailAddressIgnoreCase(String emailAddress);

    @Query("select u.mysqlId from UserEntity u where u.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select lower(u.emailAddress) from UserEntity u where lower(u.emailAddress) in :emails")
    Set<String> findExistingEmailsIgnoreCase(@Param("emails") Collection<String> emails);

    @Query("select u from UserEntity u where u.mysqlId in :mysqlIds")
    List<UserEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);
}
