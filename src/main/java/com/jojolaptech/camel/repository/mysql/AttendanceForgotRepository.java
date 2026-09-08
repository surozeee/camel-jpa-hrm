package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.AttendanceForgot;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceForgotRepository extends JpaRepository<AttendanceForgot, Long> {

    /**
     * Page ids only — JOIN FETCH with Pageable forces Hibernate to load the full table
     * in memory and OOMs under a 2g heap on large attendanceForgot sets.
     */
    @Query(
            value = "SELECT f.id FROM AttendanceForgot f",
            countQuery = "SELECT count(f) FROM AttendanceForgot f")
    Page<Long> findMigratableIds(Pageable pageable);

    @Query("""
            SELECT f FROM AttendanceForgot f
            JOIN FETCH f.employee
            JOIN FETCH f.company
            WHERE f.id IN :ids
            """)
    List<AttendanceForgot> findByIdInWithGraph(@Param("ids") Collection<Long> ids);
}
