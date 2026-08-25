package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.LeaveAccumulation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveAccumulationRepository extends JpaRepository<LeaveAccumulation, Long> {

    @Query(
            value = """
                    SELECT la FROM LeaveAccumulation la
                    JOIN FETCH la.employee
                    JOIN FETCH la.company
                    JOIN FETCH la.leaves
                    JOIN FETCH la.fiscalYear
                    """,
            countQuery = "SELECT count(la) FROM LeaveAccumulation la")
    Page<LeaveAccumulation> findMigratable(Pageable pageable);
}
