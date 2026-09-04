package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeePayrollPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeePayrollPaymentRepository extends JpaRepository<EmployeePayrollPayment, Long> {

    @Query(
            value = """
                    SELECT p FROM EmployeePayrollPayment p
                    JOIN FETCH p.employee
                    JOIN FETCH p.paymentPeriod pp
                    LEFT JOIN FETCH pp.company
                    LEFT JOIN FETCH p.company
                    WHERE p.status = true
                    """,
            countQuery = "SELECT count(p) FROM EmployeePayrollPayment p WHERE p.status = true")
    Page<EmployeePayrollPayment> findMigratable(Pageable pageable);
}
