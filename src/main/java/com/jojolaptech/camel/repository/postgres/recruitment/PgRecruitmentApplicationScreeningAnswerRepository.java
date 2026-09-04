package com.jojolaptech.camel.repository.postgres.recruitment;

import com.jojolaptech.camel.model.postgres.recruitment.RecruitmentApplicationScreeningAnswerEntity;
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
public interface PgRecruitmentApplicationScreeningAnswerRepository
        extends JpaRepository<RecruitmentApplicationScreeningAnswerEntity, UUID> {

    @Query("select e.mysqlId from RecruitmentApplicationScreeningAnswerEntity e where e.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<RecruitmentApplicationScreeningAnswerEntity> findByMysqlId(Long mysqlId);

    @Query("select e from RecruitmentApplicationScreeningAnswerEntity e where e.mysqlId in :mysqlIds")
    List<RecruitmentApplicationScreeningAnswerEntity> findByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);
}
