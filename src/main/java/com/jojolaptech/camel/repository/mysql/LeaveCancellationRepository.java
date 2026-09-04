package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.LeaveCancellation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveCancellationRepository extends JpaRepository<LeaveCancellation, Long> {

    @Query(
            value = """
                    SELECT lc FROM LeaveCancellation lc
                    JOIN FETCH lc.leaveApplication
                    LEFT JOIN FETCH lc.respondByEmp
                    """,
            countQuery = "SELECT count(lc) FROM LeaveCancellation lc")
    Page<LeaveCancellation> findMigratable(Pageable pageable);
}
