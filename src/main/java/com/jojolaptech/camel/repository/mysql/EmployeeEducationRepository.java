package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeEducation;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeEducationRepository extends JpaRepository<EmployeeEducation, Long> {

    @Query(
            value = "SELECT e FROM EmployeeEducation e JOIN FETCH e.employee",
            countQuery = "SELECT count(e) FROM EmployeeEducation e")
    Page<EmployeeEducation> findMigratable(Pageable pageable);

    @Query("""
            SELECT e FROM EmployeeEducation e JOIN FETCH e.employee
            WHERE e.employee.id IN :employeeIds
            """)
    List<EmployeeEducation> findByEmployeeIdIn(@Param("employeeIds") Collection<Long> employeeIds);
}
