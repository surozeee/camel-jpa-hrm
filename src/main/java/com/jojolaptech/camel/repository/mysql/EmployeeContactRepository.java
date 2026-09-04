package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeContact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeContactRepository extends JpaRepository<EmployeeContact, Long> {

    @Query(
            value = "SELECT e FROM EmployeeContact e JOIN FETCH e.employee",
            countQuery = "SELECT count(e) FROM EmployeeContact e")
    Page<EmployeeContact> findMigratable(Pageable pageable);
}
