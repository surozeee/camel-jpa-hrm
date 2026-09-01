package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeAddress;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeAddressRepository extends JpaRepository<EmployeeAddress, Long> {

    @Query(
            value = "SELECT a FROM EmployeeAddress a JOIN FETCH a.employee",
            countQuery = "SELECT count(a) FROM EmployeeAddress a")
    Page<EmployeeAddress> findMigratable(Pageable pageable);

    @Query("""
            SELECT a FROM EmployeeAddress a JOIN FETCH a.employee
            WHERE a.employee.id IN :employeeIds
            """)
    List<EmployeeAddress> findByEmployeeIdIn(@Param("employeeIds") Collection<Long> employeeIds);
}
