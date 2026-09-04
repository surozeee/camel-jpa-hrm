package com.jojolaptech.camel.repository.postgres.recruitment;

import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentApplicationEntity;
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
public interface PgRecruitmentApplicationRepository extends JpaRepository<RecruitmentApplicationEntity, UUID> {

    @Query("select e.mysqlId from RecruitmentApplicationEntity e where e.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<RecruitmentApplicationEntity> findByMysqlId(Long mysqlId);

    @Query("select e from RecruitmentApplicationEntity e where e.mysqlId in :mysqlIds")
    List<RecruitmentApplicationEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);
}
