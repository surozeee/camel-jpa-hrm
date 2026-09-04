package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.ChildPayrollHeading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChildPayrollHeadingRepository extends JpaRepository<ChildPayrollHeading, Long> {

    @Query(
            value = """
                    SELECT c FROM ChildPayrollHeading c
                    JOIN FETCH c.payrollSystemHeading
                    JOIN FETCH c.parentPayrollHeading
                    """,
            countQuery = "SELECT count(c) FROM ChildPayrollHeading c")
    Page<ChildPayrollHeading> findMigratable(Pageable pageable);
}
