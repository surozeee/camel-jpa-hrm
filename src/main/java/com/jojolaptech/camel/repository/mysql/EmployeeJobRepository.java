package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeJobRepository extends JpaRepository<EmployeeJob, Long> {

    @Query(
            value = """
                    SELECT e FROM EmployeeJob e
                    JOIN FETCH e.employee
                    JOIN FETCH e.job
                    WHERE e.isactive = true OR e.enddate IS NULL
                    """,
            countQuery = """
                    SELECT count(e) FROM EmployeeJob e
                    WHERE e.isactive = true OR e.enddate IS NULL
                    """)
    Page<EmployeeJob> findMigratable(Pageable pageable);

    /** All employeeJob rows (active and closed) for designation-change history. */
    @Query(
            value = """
                    SELECT e FROM EmployeeJob e
                    JOIN FETCH e.employee
                    JOIN FETCH e.job
                    """,
            countQuery = """
                    SELECT count(e) FROM EmployeeJob e
                    """)
    Page<EmployeeJob> findAllForHistory(Pageable pageable);
}
