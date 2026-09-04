package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeTermination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeTerminationRepository extends JpaRepository<EmployeeTermination, Long> {

    @Query(
            value = """
                    SELECT e FROM EmployeeTermination e
                    JOIN FETCH e.companyEmployee ce
                    JOIN FETCH ce.employee
                    """,
            countQuery = "SELECT count(e) FROM EmployeeTermination e")
    Page<EmployeeTermination> findMigratable(Pageable pageable);
}
