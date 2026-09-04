package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.AttEmpTempShift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AttEmpTempShiftRepository extends JpaRepository<AttEmpTempShift, Long> {

    @Query(
            value = """
                    SELECT t FROM AttEmpTempShift t
                    JOIN FETCH t.employee
                    JOIN FETCH t.company
                    LEFT JOIN FETCH t.attTimeTable
                    """,
            countQuery = "SELECT count(t) FROM AttEmpTempShift t")
    Page<AttEmpTempShift> findMigratable(Pageable pageable);
}
