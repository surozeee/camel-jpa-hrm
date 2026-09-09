package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.AttEmpTempShift;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttEmpTempShiftRepository extends JpaRepository<AttEmpTempShift, Long> {

    /**
     * Page ids only — JOIN FETCH with Pageable forces Hibernate to load the full result
     * set in memory and hangs/OOMs on large attEmpTempShift tables.
     */
    @Query(
            value = "SELECT t.id FROM AttEmpTempShift t",
            countQuery = "SELECT count(t) FROM AttEmpTempShift t")
    Page<Long> findMigratableIds(Pageable pageable);

    @Query("""
            SELECT t FROM AttEmpTempShift t
            JOIN FETCH t.employee
            JOIN FETCH t.company
            LEFT JOIN FETCH t.attTimeTable
            WHERE t.id IN :ids
            """)
    List<AttEmpTempShift> findByIdInWithGraph(@Param("ids") Collection<Long> ids);
}
