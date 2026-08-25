package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.CompanyEmployee;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyEmployeeRepository extends JpaRepository<CompanyEmployee, Long> {

    @Query("""
            SELECT ce FROM CompanyEmployee ce
            JOIN FETCH ce.company
            JOIN FETCH ce.employee
            WHERE ce.employee.id IN :employeeIds
            """)
    List<CompanyEmployee> findByEmployeeIdIn(@Param("employeeIds") Collection<Long> employeeIds);
}
