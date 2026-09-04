package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.PayrollHeadingTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollHeadingTemplateRepository extends JpaRepository<PayrollHeadingTemplate, Long> {

    @Query(
            value = """
                    SELECT t FROM PayrollHeadingTemplate t
                    WHERE t.status = true
                    """,
            countQuery = """
                    SELECT count(t) FROM PayrollHeadingTemplate t
                    WHERE t.status = true
                    """)
    Page<PayrollHeadingTemplate> findMigratable(Pageable pageable);
}
