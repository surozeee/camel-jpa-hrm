package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.CompanyBranchPayrollHeading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyBranchPayrollHeadingRepository extends JpaRepository<CompanyBranchPayrollHeading, Long> {

    @Query(
            value = """
                    SELECT c FROM CompanyBranchPayrollHeading c
                    JOIN FETCH c.branchDepartment
                    JOIN FETCH c.companyPayrollHeading
                    WHERE c.status = true
                    """,
            countQuery = "SELECT count(c) FROM CompanyBranchPayrollHeading c WHERE c.status = true")
    Page<CompanyBranchPayrollHeading> findMigratable(Pageable pageable);
}
