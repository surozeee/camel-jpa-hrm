package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeBranch;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeBranchRepository extends JpaRepository<EmployeeBranch, Long> {

    @Query("""
            SELECT eb FROM EmployeeBranch eb
            JOIN FETCH eb.employee
            JOIN FETCH eb.branch
            WHERE eb.employee.id IN :employeeIds
            ORDER BY eb.isActive DESC, eb.startDate DESC
            """)
    List<EmployeeBranch> findByEmployeeIdIn(@Param("employeeIds") Collection<Long> employeeIds);
}
