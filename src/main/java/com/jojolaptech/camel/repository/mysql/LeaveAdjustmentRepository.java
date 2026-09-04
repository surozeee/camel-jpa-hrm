package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.LeaveAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveAdjustmentRepository extends JpaRepository<LeaveAdjustment, Long> {

    @Query(
            value = """
                    SELECT la FROM LeaveAdjustment la
                    JOIN FETCH la.employee
                    JOIN FETCH la.company
                    JOIN FETCH la.leaves
                    JOIN FETCH la.fiscalYear
                    """,
            countQuery = "SELECT count(la) FROM LeaveAdjustment la")
    Page<LeaveAdjustment> findMigratable(Pageable pageable);
}
