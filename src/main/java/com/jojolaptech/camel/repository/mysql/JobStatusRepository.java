package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JobStatusRepository extends JpaRepository<JobStatus, Long> {

    @Query(
            value = """
                    SELECT e FROM JobStatus e
                    JOIN FETCH e.employee
                    """,
            countQuery = """
                    SELECT count(e) FROM JobStatus e
                    """)
    Page<JobStatus> findMigratable(Pageable pageable);
}
