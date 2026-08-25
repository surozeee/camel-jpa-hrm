package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
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
public interface PgEmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {

    @Query("select e.mysqlId from EmployeeEntity e where e.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select e from EmployeeEntity e where e.mysqlId in :mysqlIds")
    List<EmployeeEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<EmployeeEntity> findByMysqlId(Long mysqlId);

    @Query("select lower(e.email) from EmployeeEntity e where lower(e.email) in :emails")
    Set<String> findExistingEmailsLowerCase(@Param("emails") Collection<String> emails);
}
