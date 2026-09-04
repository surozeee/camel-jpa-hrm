package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.LeaveApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    @Query(
            value = """
                    SELECT la FROM LeaveApplication la
                    JOIN FETCH la.employee
                    JOIN FETCH la.company
                    JOIN FETCH la.leave
                    LEFT JOIN FETCH la.approvedByEmp
                    LEFT JOIN FETCH la.recommendedByEmp
                    """,
            countQuery = "SELECT count(la) FROM LeaveApplication la")
    Page<LeaveApplication> findMigratable(Pageable pageable);
}
