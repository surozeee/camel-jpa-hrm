package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.EmployeeDetailEntity;
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
public interface PgEmployeeDetailRepository extends JpaRepository<EmployeeDetailEntity, UUID> {

    @Query("select e.mysqlId from EmployeeDetailEntity e where e.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<EmployeeDetailEntity> findByEmployeeId(UUID employeeId);

    @Query("select e from EmployeeDetailEntity e where e.employeeId in :employeeIds")
    List<EmployeeDetailEntity> findByEmployeeIdIn(@Param("employeeIds") Collection<UUID> employeeIds);
}
