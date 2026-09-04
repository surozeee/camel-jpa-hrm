package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeePayrollHeadingPayment;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeePayrollHeadingPaymentRepository
        extends JpaRepository<EmployeePayrollHeadingPayment, Long> {

    @Query("""
            SELECT h FROM EmployeePayrollHeadingPayment h
            JOIN FETCH h.payrollHeading
            WHERE h.employeePayrollPayment.id IN :paymentIds
            """)
    List<EmployeePayrollHeadingPayment> findByPaymentIds(@Param("paymentIds") Collection<Long> paymentIds);
}
