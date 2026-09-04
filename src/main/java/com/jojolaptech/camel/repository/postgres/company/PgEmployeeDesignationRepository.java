package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.EmployeeDesignationEntity;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgEmployeeDesignationRepository extends JpaRepository<EmployeeDesignationEntity, UUID> {

    @Query("select e.mysqlId from EmployeeDesignationEntity e where e.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select e from EmployeeDesignationEntity e where e.mysqlId in :mysqlIds")
    List<EmployeeDesignationEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);
}
