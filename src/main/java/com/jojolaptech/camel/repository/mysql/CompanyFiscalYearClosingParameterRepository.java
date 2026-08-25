package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.CompanyFiscalYearClosingParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyFiscalYearClosingParameterRepository
        extends JpaRepository<CompanyFiscalYearClosingParameter, Long> {

    @Query(
            value = """
                    SELECT p FROM CompanyFiscalYearClosingParameter p
                    JOIN FETCH p.company JOIN FETCH p.fiscalYear
                    """,
            countQuery = "SELECT count(p) FROM CompanyFiscalYearClosingParameter p")
    Page<CompanyFiscalYearClosingParameter> findMigratable(Pageable pageable);
}
