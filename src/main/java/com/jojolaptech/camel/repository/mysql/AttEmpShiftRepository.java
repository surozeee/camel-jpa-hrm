package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.AttEmpShift;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttEmpShiftRepository extends JpaRepository<AttEmpShift, Long> {

    /**
     * Latest assignment id per employee (by shift_date, then id). Avoids scanning the full
     * historical att_emp_shift table page-by-page. Native SQL must use snake_case names
     * (Spring Boot physical naming); camelCase table names do not exist in MySQL.
     */
    @Query(
            value =
                    """
                    SELECT s.id
                    FROM att_emp_shift s
                    INNER JOIN (
                        SELECT employee_id, MAX(shift_date) AS max_shift_date
                        FROM att_emp_shift
                        GROUP BY employee_id
                    ) latest
                        ON latest.employee_id = s.employee_id
                       AND latest.max_shift_date = s.shift_date
                    INNER JOIN (
                        SELECT employee_id, shift_date, MAX(id) AS max_id
                        FROM att_emp_shift
                        GROUP BY employee_id, shift_date
                    ) pick
                        ON pick.employee_id = s.employee_id
                       AND pick.shift_date = s.shift_date
                       AND pick.max_id = s.id
                    """,
            nativeQuery = true)
    List<Long> findLatestIdsPerEmployee();

    @Query("""
            SELECT s FROM AttEmpShift s
            JOIN FETCH s.employee
            JOIN FETCH s.company
            JOIN FETCH s.attShift
            WHERE s.id IN :ids
            """)
    List<AttEmpShift> findByIdInWithGraph(@Param("ids") Collection<Long> ids);
}
