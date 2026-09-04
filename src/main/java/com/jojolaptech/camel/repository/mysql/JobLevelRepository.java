package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.JobLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JobLevelRepository extends JpaRepository<JobLevel, Long> {

    @Query(
            value = """
                    SELECT j FROM JobLevel j
                    JOIN FETCH j.company
                    WHERE j.status = true
                    """,
            countQuery = """
                    SELECT count(j) FROM JobLevel j
                    WHERE j.status = true
                    """)
    Page<JobLevel> findMigratable(Pageable pageable);
}
