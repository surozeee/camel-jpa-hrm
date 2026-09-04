package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.OpeningPayrollBalance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OpeningPayrollBalanceRepository extends JpaRepository<OpeningPayrollBalance, Long> {

    @Query(
            value = """
                    SELECT o FROM OpeningPayrollBalance o
                    JOIN FETCH o.employee
                    JOIN FETCH o.company
                    JOIN FETCH o.fiscalYear
                    JOIN FETCH o.payrollheading
                    """,
            countQuery = "SELECT count(o) FROM OpeningPayrollBalance o")
    Page<OpeningPayrollBalance> findMigratable(Pageable pageable);
}
