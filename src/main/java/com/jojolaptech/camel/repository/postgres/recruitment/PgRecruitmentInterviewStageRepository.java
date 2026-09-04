package com.jojolaptech.camel.repository.postgres.recruitment;

import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentInterviewStageEntity;
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
public interface PgRecruitmentInterviewStageRepository
        extends JpaRepository<RecruitmentInterviewStageEntity, UUID> {

    @Query("select e.mysqlId from RecruitmentInterviewStageEntity e where e.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<RecruitmentInterviewStageEntity> findByMysqlId(Long mysqlId);

    @Query("select e from RecruitmentInterviewStageEntity e where e.mysqlId in :mysqlIds")
    List<RecruitmentInterviewStageEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);
}
