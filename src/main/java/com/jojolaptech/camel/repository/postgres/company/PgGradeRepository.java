package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.GradeEntity;
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
public interface PgGradeRepository extends JpaRepository<GradeEntity, UUID> {

    @Query("select g.mysqlId from GradeEntity g where g.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    @Query("select g from GradeEntity g where g.mysqlId in :mysqlIds")
    List<GradeEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<GradeEntity> findByMysqlId(Long mysqlId);
}
