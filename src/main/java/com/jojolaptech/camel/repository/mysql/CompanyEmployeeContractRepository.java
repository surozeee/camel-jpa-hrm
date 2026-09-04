package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.CompanyEmployeeContract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyEmployeeContractRepository extends JpaRepository<CompanyEmployeeContract, Long> {

    @Query(
            value = """
                    SELECT e FROM CompanyEmployeeContract e
                    JOIN FETCH e.employee
                    """,
            countQuery = """
                    SELECT count(e) FROM CompanyEmployeeContract e
                    """)
    Page<CompanyEmployeeContract> findMigratable(Pageable pageable);
}
