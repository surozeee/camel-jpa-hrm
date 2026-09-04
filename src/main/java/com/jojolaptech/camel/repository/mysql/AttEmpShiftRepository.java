package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.AttEmpShift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AttEmpShiftRepository extends JpaRepository<AttEmpShift, Long> {

    @Query(
            value = """
                    SELECT s FROM AttEmpShift s
                    JOIN FETCH s.employee
                    JOIN FETCH s.company
                    JOIN FETCH s.attShift
                    """,
            countQuery = "SELECT count(s) FROM AttEmpShift s")
    Page<AttEmpShift> findMigratable(Pageable pageable);
}
