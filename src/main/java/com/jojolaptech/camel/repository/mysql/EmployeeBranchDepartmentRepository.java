package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeBranchDepartment;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeBranchDepartmentRepository extends JpaRepository<EmployeeBranchDepartment, Long> {

    @Query("""
            SELECT ebd FROM EmployeeBranchDepartment ebd
            JOIN FETCH ebd.employee
            LEFT JOIN FETCH ebd.branch
            LEFT JOIN FETCH ebd.department
            WHERE ebd.employee.id IN :employeeIds
            ORDER BY ebd.isActive DESC, ebd.startDate DESC
            """)
    List<EmployeeBranchDepartment> findByEmployeeIdIn(@Param("employeeIds") Collection<Long> employeeIds);
}
