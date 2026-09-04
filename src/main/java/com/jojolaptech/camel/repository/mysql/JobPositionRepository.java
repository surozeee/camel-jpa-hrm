package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.JobPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {

    @Query(
            value = """
                    SELECT e FROM JobPosition e
                    JOIN FETCH e.employee
                    """,
            countQuery = """
                    SELECT count(e) FROM JobPosition e
                    """)
    Page<JobPosition> findMigratable(Pageable pageable);
}
