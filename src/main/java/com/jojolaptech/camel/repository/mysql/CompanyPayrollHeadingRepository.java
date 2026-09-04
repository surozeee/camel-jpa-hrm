package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.CompanyPayrollHeading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyPayrollHeadingRepository extends JpaRepository<CompanyPayrollHeading, Long> {

    @Query(
            value = """
                    SELECT c FROM CompanyPayrollHeading c
                    JOIN FETCH c.company
                    LEFT JOIN FETCH c.payrollHeading ph
                    LEFT JOIN FETCH ph.calculatedOn
                    LEFT JOIN FETCH c.payrollParentHeading
                    WHERE c.status = true
                    """,
            countQuery = "SELECT count(c) FROM CompanyPayrollHeading c WHERE c.status = true")
    Page<CompanyPayrollHeading> findMigratable(Pageable pageable);
}
