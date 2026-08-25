package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.AutoLeaveAccParams;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AutoLeaveAccParamsRepository extends JpaRepository<AutoLeaveAccParams, Long> {

    @Query(
            value = """
                    SELECT DISTINCT p.paramDate FROM AutoLeaveAccParams p
                    JOIN p.company c
                    WHERE p.isDeleted = false
                      AND (p.isActive = true OR p.isActive IS NULL)
                    ORDER BY p.paramDate ASC
                    """,
            countQuery = """
                    SELECT count(DISTINCT p.paramDate) FROM AutoLeaveAccParams p
                    WHERE p.isDeleted = false
                      AND (p.isActive = true OR p.isActive IS NULL)
                    """)
    Page<java.util.Date> findDistinctParamDates(Pageable pageable);

    @Query("""
            SELECT p FROM AutoLeaveAccParams p
            JOIN FETCH p.company
            JOIN FETCH p.leave
            WHERE p.paramDate IN :paramDates
              AND p.isDeleted = false
              AND (p.isActive = true OR p.isActive IS NULL)
            """)
    List<AutoLeaveAccParams> findActiveByParamDateIn(@Param("paramDates") Collection<java.util.Date> paramDates);

    @Query("""
            SELECT p FROM AutoLeaveAccParams p
            JOIN FETCH p.company
            JOIN FETCH p.leave
            WHERE p.company.id IN :companyIds
              AND p.paramDate IN :paramDates
              AND p.isDeleted = false
              AND (p.isActive = true OR p.isActive IS NULL)
            """)
    List<AutoLeaveAccParams> findActiveByCompanyIdInAndParamDateIn(
            @Param("companyIds") Collection<Long> companyIds,
            @Param("paramDates") Collection<java.util.Date> paramDates);
}
