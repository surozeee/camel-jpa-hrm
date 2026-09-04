package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.MassIncrement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MassIncrementRepository extends JpaRepository<MassIncrement, Long> {

    @Query(
            value = """
                    SELECT m FROM MassIncrement m
                    LEFT JOIN FETCH m.company
                    LEFT JOIN FETCH m.branch
                    LEFT JOIN FETCH m.jobLevel jl
                    LEFT JOIN FETCH jl.company
                    JOIN FETCH m.payrollHeading
                    """,
            countQuery = """
                    SELECT count(m) FROM MassIncrement m
                    """)
    Page<MassIncrement> findMigratable(Pageable pageable);
}
