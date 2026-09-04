package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.PayrollHeadingCalculation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollHeadingCalculationRepository extends JpaRepository<PayrollHeadingCalculation, Long> {

    @Query(
            value = """
                    SELECT p FROM PayrollHeadingCalculation p
                    JOIN FETCH p.companyPayrollHeading
                    JOIN FETCH p.companyBranchPayrollHeading
                    """,
            countQuery = "SELECT count(p) FROM PayrollHeadingCalculation p")
    Page<PayrollHeadingCalculation> findMigratable(Pageable pageable);
}
