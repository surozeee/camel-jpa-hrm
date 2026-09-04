package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeePayrollHeading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeePayrollHeadingRepository extends JpaRepository<EmployeePayrollHeading, Long> {

    @Query(
            value = """
                    SELECT e FROM EmployeePayrollHeading e
                    JOIN FETCH e.employee
                    JOIN FETCH e.payrollHeading cph
                    LEFT JOIN FETCH cph.payrollHeading
                    LEFT JOIN FETCH cph.payrollParentHeading
                    WHERE e.status = true AND e.endDate IS NULL
                    """,
            countQuery = """
                    SELECT count(e) FROM EmployeePayrollHeading e
                    WHERE e.status = true AND e.endDate IS NULL
                    """)
    Page<EmployeePayrollHeading> findMigratableOpen(Pageable pageable);
}
