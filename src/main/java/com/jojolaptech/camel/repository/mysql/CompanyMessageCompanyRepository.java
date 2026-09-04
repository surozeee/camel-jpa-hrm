package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.CompanyMessageCompany;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyMessageCompanyRepository extends JpaRepository<CompanyMessageCompany, Long> {

    @Query(
            value =
                    """
                    SELECT c FROM CompanyMessageCompany c
                    JOIN FETCH c.company
                    JOIN FETCH c.companyMessage
                    """,
            countQuery = "SELECT count(c) FROM CompanyMessageCompany c")
    Page<CompanyMessageCompany> findMigratable(Pageable pageable);
}
