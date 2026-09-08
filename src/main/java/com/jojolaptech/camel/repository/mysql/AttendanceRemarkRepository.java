package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.AttendanceRemark;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRemarkRepository extends JpaRepository<AttendanceRemark, Long> {

    /** Page ids only — avoids JOIN FETCH + Pageable full-table in-memory load. */
    @Query(
            value = "SELECT r.id FROM AttendanceRemark r",
            countQuery = "SELECT count(r) FROM AttendanceRemark r")
    Page<Long> findMigratableIds(Pageable pageable);

    @Query("""
            SELECT r FROM AttendanceRemark r
            JOIN FETCH r.employee
            JOIN FETCH r.company
            WHERE r.id IN :ids
            """)
    List<AttendanceRemark> findByIdInWithGraph(@Param("ids") Collection<Long> ids);
}
