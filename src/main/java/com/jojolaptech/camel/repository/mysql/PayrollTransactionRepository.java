package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.PayrollTransaction;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollTransactionRepository extends JpaRepository<PayrollTransaction, Long> {

    @Query(
            value = """
                    SELECT t FROM PayrollTransaction t
                    JOIN FETCH t.employee
                    JOIN FETCH t.company
                    JOIN FETCH t.payrollMonth
                    JOIN FETCH t.payrollheading
                    JOIN FETCH t.fiscalYear
                    """,
            countQuery = "SELECT count(t) FROM PayrollTransaction t")
    Page<PayrollTransaction> findMigratable(Pageable pageable);

    @Query("""
            SELECT t FROM PayrollTransaction t
            JOIN FETCH t.employee
            JOIN FETCH t.company
            JOIN FETCH t.payrollMonth
            JOIN FETCH t.payrollheading
            WHERE t.payrollMonth.id = :monthId
            """)
    List<PayrollTransaction> findByPayrollMonthId(@Param("monthId") Long monthId);
}
