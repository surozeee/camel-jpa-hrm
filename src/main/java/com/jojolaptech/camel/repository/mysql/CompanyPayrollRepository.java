package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.CompanyPayroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyPayrollRepository extends JpaRepository<CompanyPayroll, Long> {

    @Query(
            value = """
                    SELECT c FROM CompanyPayroll c
                    JOIN FETCH c.company
                    """,
            countQuery = "SELECT count(c) FROM CompanyPayroll c")
    Page<CompanyPayroll> findMigratable(Pageable pageable);
}
