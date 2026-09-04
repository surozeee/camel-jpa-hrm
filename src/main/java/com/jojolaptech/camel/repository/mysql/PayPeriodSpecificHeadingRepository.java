package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.PayPeriodSpecificHeading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PayPeriodSpecificHeadingRepository extends JpaRepository<PayPeriodSpecificHeading, Long> {

    @Query(
            value = """
                    SELECT p FROM PayPeriodSpecificHeading p
                    JOIN FETCH p.payPeriod
                    JOIN FETCH p.companyPayrollHeading
                    JOIN FETCH p.fiscalYear
                    """,
            countQuery = "SELECT count(p) FROM PayPeriodSpecificHeading p")
    Page<PayPeriodSpecificHeading> findMigratable(Pageable pageable);
}
