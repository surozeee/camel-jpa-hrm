package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.CompanyPayrollInstitution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyPayrollInstitutionRepository extends JpaRepository<CompanyPayrollInstitution, Long> {

    @Query(
            value = """
                    SELECT c FROM CompanyPayrollInstitution c
                    JOIN FETCH c.company
                    JOIN FETCH c.payrollInstitution
                    """,
            countQuery = "SELECT count(c) FROM CompanyPayrollInstitution c")
    Page<CompanyPayrollInstitution> findMigratable(Pageable pageable);
}
