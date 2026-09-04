package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.AttendanceTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceTransactionRepository extends JpaRepository<AttendanceTransaction, Long> {

    @Query(
            value = """
                    SELECT t FROM AttendanceTransaction t
                    JOIN FETCH t.employee
                    JOIN FETCH t.company
                    """,
            countQuery = "SELECT count(t) FROM AttendanceTransaction t")
    Page<AttendanceTransaction> findMigratable(Pageable pageable);
}
