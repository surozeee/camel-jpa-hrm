package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeGrade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeGradeRepository extends JpaRepository<EmployeeGrade, Long> {

    @Query(
            value = """
                    SELECT eg FROM EmployeeGrade eg
                    JOIN FETCH eg.employee
                    JOIN FETCH eg.jobLevelGrade jlg
                    JOIN FETCH jlg.jobLevel
                    WHERE eg.status = true AND eg.endDate IS NULL
                    """,
            countQuery = """
                    SELECT count(eg) FROM EmployeeGrade eg
                    WHERE eg.status = true AND eg.endDate IS NULL
                    """)
    Page<EmployeeGrade> findMigratable(Pageable pageable);
}
