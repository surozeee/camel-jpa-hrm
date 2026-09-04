package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.TemplatePayrollHeading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplatePayrollHeadingRepository extends JpaRepository<TemplatePayrollHeading, Long> {

    @Query(
            value = """
                    SELECT t FROM TemplatePayrollHeading t
                    JOIN FETCH t.payrollTemplate pt
                    JOIN FETCH t.payrollHeading ph
                    LEFT JOIN FETCH ph.calculatedOn
                    WHERE pt.status = true AND ph.status = true
                    """,
            countQuery = """
                    SELECT count(t) FROM TemplatePayrollHeading t
                    JOIN t.payrollTemplate pt
                    JOIN t.payrollHeading ph
                    WHERE pt.status = true AND ph.status = true
                    """)
    Page<TemplatePayrollHeading> findMigratable(Pageable pageable);
}
