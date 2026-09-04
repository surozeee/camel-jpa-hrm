package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.HeadingWisePayrollPayment;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HeadingWisePayrollPaymentRepository extends JpaRepository<HeadingWisePayrollPayment, Long> {

    @Query("""
            SELECT h FROM HeadingWisePayrollPayment h
            JOIN FETCH h.payrollHeading
            WHERE h.employeePayrollPayment.id IN :paymentIds
            """)
    List<HeadingWisePayrollPayment> findByPaymentIds(@Param("paymentIds") Collection<Long> paymentIds);
}
