package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeSummaryRepository extends JpaRepository<EmployeeSummary, Long> {

    @Query(
            value = """
                    SELECT e FROM EmployeeSummary e
                    JOIN FETCH e.employee
                    """,
            countQuery = "SELECT count(e) FROM EmployeeSummary e")
    Page<EmployeeSummary> findMigratable(Pageable pageable);
}
