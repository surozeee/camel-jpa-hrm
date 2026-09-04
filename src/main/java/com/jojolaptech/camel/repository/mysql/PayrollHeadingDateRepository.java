package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.PayrollHeadingDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollHeadingDateRepository extends JpaRepository<PayrollHeadingDate, Long> {

    @Query(
            value = """
                    SELECT p FROM PayrollHeadingDate p
                    LEFT JOIN FETCH p.companyPayrollHeading
                    LEFT JOIN FETCH p.companyBranchPayrollHeading
                    """,
            countQuery = "SELECT count(p) FROM PayrollHeadingDate p")
    Page<PayrollHeadingDate> findMigratable(Pageable pageable);
}
