package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeePayrollDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeePayrollDateRepository extends JpaRepository<EmployeePayrollDate, Long> {

    @Query("""
            SELECT d FROM EmployeePayrollDate d
            JOIN FETCH d.employeePayrollHeading
            WHERE d.employeePayrollHeading.id IN :headingIds
              AND d.endDate IS NULL
            """)
    List<EmployeePayrollDate> findOpenByHeadingIds(@Param("headingIds") Collection<Long> headingIds);
}
