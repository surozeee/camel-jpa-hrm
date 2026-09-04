package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeSeminar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeSeminarRepository extends JpaRepository<EmployeeSeminar, Long> {

    @Query(
            value = "SELECT e FROM EmployeeSeminar e JOIN FETCH e.employee",
            countQuery = "SELECT count(e) FROM EmployeeSeminar e")
    Page<EmployeeSeminar> findMigratable(Pageable pageable);
}
