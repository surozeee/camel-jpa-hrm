package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.OldAttendanceTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OldAttendanceTransactionRepository extends JpaRepository<OldAttendanceTransaction, Long> {

    @Query(
            value = """
                    SELECT t FROM OldAttendanceTransaction t
                    JOIN FETCH t.employee
                    JOIN FETCH t.company
                    """,
            countQuery = "SELECT count(t) FROM OldAttendanceTransaction t")
    Page<OldAttendanceTransaction> findMigratable(Pageable pageable);
}
