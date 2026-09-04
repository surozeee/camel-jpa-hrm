package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeAward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeAwardRepository extends JpaRepository<EmployeeAward, Long> {

    @Query(
            value = "SELECT e FROM EmployeeAward e JOIN FETCH e.employee",
            countQuery = "SELECT count(e) FROM EmployeeAward e")
    Page<EmployeeAward> findMigratable(Pageable pageable);
}
