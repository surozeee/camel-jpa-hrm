package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.LeaveBranchDepartment;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveBranchDepartmentRepository extends JpaRepository<LeaveBranchDepartment, Long> {

    @Query("""
            SELECT lbd FROM LeaveBranchDepartment lbd
            JOIN FETCH lbd.leaves
            JOIN FETCH lbd.branch b
            JOIN FETCH b.company
            WHERE lbd.leaves.id IN :leaveIds
            """)
    List<LeaveBranchDepartment> findByLeaveIdIn(@Param("leaveIds") Collection<Long> leaveIds);
}
