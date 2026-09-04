package com.jojolaptech.camel.repository.postgres.recruitment;

import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentCandidateEntity;
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
public interface PgRecruitmentCandidateRepository extends JpaRepository<RecruitmentCandidateEntity, UUID> {

    @Query("select e.mysqlId from RecruitmentCandidateEntity e where e.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<RecruitmentCandidateEntity> findByMysqlId(Long mysqlId);

    @Query("select e from RecruitmentCandidateEntity e where e.mysqlId in :mysqlIds")
    List<RecruitmentCandidateEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);
}
