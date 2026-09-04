package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.EmployeeSalaryEntity;
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
public interface PgEmployeeSalaryRepository extends JpaRepository<EmployeeSalaryEntity, UUID> {

    @Query("select e.mysqlId from EmployeeSalaryEntity e where e.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select e from EmployeeSalaryEntity e where e.mysqlId in :mysqlIds")
    List<EmployeeSalaryEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<EmployeeSalaryEntity> findByMysqlId(Long mysqlId);

    Optional<EmployeeSalaryEntity> findByEmployeeIdAndEndDateIsNull(UUID employeeId);
}
