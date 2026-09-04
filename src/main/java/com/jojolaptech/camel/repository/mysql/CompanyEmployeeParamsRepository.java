package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.CompanyEmployeeParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyEmployeeParamsRepository extends JpaRepository<CompanyEmployeeParams, Long> {

    @Query(
            value = """
                    SELECT c FROM CompanyEmployeeParams c
                    JOIN FETCH c.company
                    JOIN FETCH c.employee
                    """,
            countQuery = "SELECT count(c) FROM CompanyEmployeeParams c")
    Page<CompanyEmployeeParams> findMigratable(Pageable pageable);
}
