package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query(
            value = "SELECT e FROM Employee e",
            countQuery = "SELECT count(e) FROM Employee e")
    Page<Employee> findMigratable(Pageable pageable);
}
