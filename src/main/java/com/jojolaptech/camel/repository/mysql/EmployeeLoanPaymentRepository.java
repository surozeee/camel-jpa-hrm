package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeLoanPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeLoanPaymentRepository extends JpaRepository<EmployeeLoanPayment, Long> {

    @Query(
            value = """
                    SELECT p FROM EmployeeLoanPayment p
                    JOIN FETCH p.employeeLoan el
                    JOIN FETCH el.employee
                    LEFT JOIN FETCH p.payPeriod
                    """,
            countQuery = """
                    SELECT count(p) FROM EmployeeLoanPayment p
                    """)
    Page<EmployeeLoanPayment> findMigratable(Pageable pageable);
}
