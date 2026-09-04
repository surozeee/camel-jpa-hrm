package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.LeaveBalance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    @Query(
            value = """
                    SELECT lb FROM LeaveBalance lb
                    JOIN FETCH lb.employee
                    JOIN FETCH lb.company
                    JOIN FETCH lb.leave
                    JOIN FETCH lb.fiscal
                    """,
            countQuery = "SELECT count(lb) FROM LeaveBalance lb")
    Page<LeaveBalance> findMigratable(Pageable pageable);
}
