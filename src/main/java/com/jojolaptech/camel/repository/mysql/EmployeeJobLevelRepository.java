package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeJobLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeJobLevelRepository extends JpaRepository<EmployeeJobLevel, Long> {

    @Query(
            value = """
                    SELECT e FROM EmployeeJobLevel e
                    JOIN FETCH e.employee
                    JOIN FETCH e.jobLevel
                    """,
            countQuery = """
                    SELECT count(e) FROM EmployeeJobLevel e
                    """)
    Page<EmployeeJobLevel> findMigratable(Pageable pageable);
}
