package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.ParentPayrollHeading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ParentPayrollHeadingRepository extends JpaRepository<ParentPayrollHeading, Long> {

    @Query(
            value = """
                    SELECT p FROM ParentPayrollHeading p
                    JOIN FETCH p.company
                    """,
            countQuery = "SELECT count(p) FROM ParentPayrollHeading p")
    Page<ParentPayrollHeading> findMigratable(Pageable pageable);
}
