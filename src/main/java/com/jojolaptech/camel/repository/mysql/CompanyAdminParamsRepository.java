package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.CompanyAdminParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyAdminParamsRepository extends JpaRepository<CompanyAdminParams, Long> {

    @Query(
            value = """
                    SELECT c FROM CompanyAdminParams c
                    JOIN FETCH c.company
                    LEFT JOIN FETCH c.admin
                    """,
            countQuery = "SELECT count(c) FROM CompanyAdminParams c")
    Page<CompanyAdminParams> findMigratable(Pageable pageable);
}
