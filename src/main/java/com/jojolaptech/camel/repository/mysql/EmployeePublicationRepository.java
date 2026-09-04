package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeePublication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeePublicationRepository extends JpaRepository<EmployeePublication, Long> {

    @Query(
            value = "SELECT e FROM EmployeePublication e JOIN FETCH e.employee",
            countQuery = "SELECT count(e) FROM EmployeePublication e")
    Page<EmployeePublication> findMigratable(Pageable pageable);
}
