package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeLoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeLoanRepository extends JpaRepository<EmployeeLoan, Long> {

    @Query(
            value = """
                    SELECT l FROM EmployeeLoan l
                    JOIN FETCH l.employee
                    """,
            countQuery = """
                    SELECT count(l) FROM EmployeeLoan l
                    """)
    Page<EmployeeLoan> findMigratable(Pageable pageable);
}
