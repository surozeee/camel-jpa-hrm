package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.BranchDepartmentHead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchDepartmentHeadRepository extends JpaRepository<BranchDepartmentHead, Long> {

    @Query(
            value = """
                    SELECT e FROM BranchDepartmentHead e
                    JOIN FETCH e.employee
                    LEFT JOIN FETCH e.department
                    LEFT JOIN FETCH e.branch
                    WHERE e.endDate IS NULL
                    """,
            countQuery = """
                    SELECT count(e) FROM BranchDepartmentHead e
                    WHERE e.endDate IS NULL
                    """)
    Page<BranchDepartmentHead> findMigratable(Pageable pageable);
}
