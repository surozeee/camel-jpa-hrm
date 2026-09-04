package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.CompanyValidity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyValidityRepository extends JpaRepository<CompanyValidity, Long> {

    @Query(
            value = """
                    SELECT v FROM CompanyValidity v
                    JOIN FETCH v.company
                    """,
            countQuery = """
                    SELECT count(v) FROM CompanyValidity v
                    """)
    Page<CompanyValidity> findMigratable(Pageable pageable);
}
