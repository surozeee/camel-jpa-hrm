package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.OvertimeAccLeaveParams;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OvertimeAccLeaveParamsRepository extends JpaRepository<OvertimeAccLeaveParams, Long> {

    @Query(
            value = """
                    SELECT DISTINCT p.paramDate FROM OvertimeAccLeaveParams p
                    JOIN p.company c
                    ORDER BY p.paramDate ASC
                    """,
            countQuery = "SELECT count(DISTINCT p.paramDate) FROM OvertimeAccLeaveParams p")
    Page<Date> findDistinctParamDates(Pageable pageable);

    @Query("""
            SELECT p FROM OvertimeAccLeaveParams p
            JOIN FETCH p.company
            WHERE p.paramDate IN :paramDates
            """)
    List<OvertimeAccLeaveParams> findByParamDateIn(@Param("paramDates") Collection<Date> paramDates);

    @Query("""
            SELECT p FROM OvertimeAccLeaveParams p
            JOIN FETCH p.company c
            WHERE c.id IN :companyMysqlIds
            """)
    List<OvertimeAccLeaveParams> findByCompanyMysqlIdIn(@Param("companyMysqlIds") Collection<Long> companyMysqlIds);
}
