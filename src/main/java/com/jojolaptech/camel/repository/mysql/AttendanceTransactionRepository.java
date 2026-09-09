package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.AttendanceTransaction;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceTransactionRepository extends JpaRepository<AttendanceTransaction, Long> {

    @Query(
            value = """
                    SELECT t FROM AttendanceTransaction t
                    JOIN FETCH t.employee
                    JOIN FETCH t.company
                    WHERE t.logDate IS NOT NULL
                      AND t.logDate >= :fromDate
                      AND t.logDate <= :toDate
                    """,
            countQuery = """
                    SELECT count(t) FROM AttendanceTransaction t
                    WHERE t.logDate IS NOT NULL
                      AND t.logDate >= :fromDate
                      AND t.logDate <= :toDate
                    """)
    Page<AttendanceTransaction> findMigratable(
            @Param("fromDate") Date fromDate, @Param("toDate") Date toDate, Pageable pageable);
}
