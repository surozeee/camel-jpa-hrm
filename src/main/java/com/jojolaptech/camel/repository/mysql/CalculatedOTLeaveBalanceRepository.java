package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.CalculatedOTLeaveBalance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CalculatedOTLeaveBalanceRepository extends JpaRepository<CalculatedOTLeaveBalance, Long> {

    @Query(
            value = """
                    SELECT c FROM CalculatedOTLeaveBalance c
                    JOIN FETCH c.employee
                    JOIN FETCH c.company
                    """,
            countQuery = "SELECT count(c) FROM CalculatedOTLeaveBalance c")
    Page<CalculatedOTLeaveBalance> findMigratable(Pageable pageable);
}
