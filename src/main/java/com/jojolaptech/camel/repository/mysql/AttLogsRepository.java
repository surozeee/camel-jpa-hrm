package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.AttLogs;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttLogsRepository extends JpaRepository<AttLogs, Long> {

    @Query(
            value = """
                    SELECT l FROM AttLogs l JOIN FETCH l.company
                    WHERE (l.isDeleted IS NULL OR l.isDeleted = false)
                      AND l.checkTime IS NOT NULL
                      AND l.checkTime >= :fromDate
                      AND l.checkTime <= :toDate
                    """,
            countQuery = """
                    SELECT count(l) FROM AttLogs l
                    WHERE (l.isDeleted IS NULL OR l.isDeleted = false)
                      AND l.checkTime IS NOT NULL
                      AND l.checkTime >= :fromDate
                      AND l.checkTime <= :toDate
                    """)
    Page<AttLogs> findMigratable(
            @Param("fromDate") Date fromDate, @Param("toDate") Date toDate, Pageable pageable);
}
