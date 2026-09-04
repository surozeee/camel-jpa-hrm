package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.SkillEntity;
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
public interface PgSkillRepository extends JpaRepository<SkillEntity, UUID> {

    @Query("select e.mysqlId from SkillEntity e where e.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<SkillEntity> findByCompanyIdAndNameIgnoreCase(UUID companyId, String name);

    @Query("""
            select s from SkillEntity s
            where s.companyId in :companyIds
            """)
    List<SkillEntity> findByCompanyIdIn(@Param("companyIds") Collection<UUID> companyIds);
}
