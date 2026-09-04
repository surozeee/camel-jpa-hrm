package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeHealth;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeHealthRepository extends JpaRepository<EmployeeHealth, Long> {

    @Query(
            value = "SELECT e FROM EmployeeHealth e JOIN FETCH e.employee",
            countQuery = "SELECT count(e) FROM EmployeeHealth e")
    Page<EmployeeHealth> findMigratable(Pageable pageable);
}
