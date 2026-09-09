package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.LeaveApplication;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    @Query(
            value = "SELECT la.id FROM LeaveApplication la",
            countQuery = "SELECT count(la) FROM LeaveApplication la")
    Page<Long> findMigratableIds(Pageable pageable);

    @Query("""
            SELECT la FROM LeaveApplication la
            JOIN FETCH la.employee
            JOIN FETCH la.company
            JOIN FETCH la.leave
            LEFT JOIN FETCH la.approvedByEmp
            LEFT JOIN FETCH la.recommendedByEmp
            WHERE la.id IN :ids
            """)
    List<LeaveApplication> findByIdInWithGraph(@Param("ids") Collection<Long> ids);
}
