package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.JobLevelPayroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JobLevelPayrollRepository extends JpaRepository<JobLevelPayroll, Long> {

    @Query(
            value = """
                    SELECT p FROM JobLevelPayroll p
                    JOIN FETCH p.jobLevel jl
                    JOIN FETCH jl.company
                    JOIN FETCH p.payrollHeading cph
                    LEFT JOIN FETCH cph.payrollHeading
                    WHERE p.status = true
                    """,
            countQuery = """
                    SELECT count(p) FROM JobLevelPayroll p
                    WHERE p.status = true
                    """)
    Page<JobLevelPayroll> findMigratable(Pageable pageable);
}
