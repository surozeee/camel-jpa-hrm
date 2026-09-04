package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.PayrollOvertime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollOvertimeRepository extends JpaRepository<PayrollOvertime, Long> {

    @Query(
            value = """
                    SELECT p FROM PayrollOvertime p
                    JOIN FETCH p.employee
                    JOIN FETCH p.payPeriod
                    """,
            countQuery = "SELECT count(p) FROM PayrollOvertime p")
    Page<PayrollOvertime> findMigratable(Pageable pageable);
}
