package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.CalculatedAutoLeaveAccumulation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CalculatedAutoLeaveAccumulationRepository
        extends JpaRepository<CalculatedAutoLeaveAccumulation, Long> {

    @Query(
            value = """
                    SELECT c FROM CalculatedAutoLeaveAccumulation c
                    JOIN FETCH c.employee
                    JOIN FETCH c.company
                    JOIN FETCH c.leave
                    JOIN FETCH c.fiscalYear
                    """,
            countQuery = "SELECT count(c) FROM CalculatedAutoLeaveAccumulation c")
    Page<CalculatedAutoLeaveAccumulation> findMigratable(Pageable pageable);
}
