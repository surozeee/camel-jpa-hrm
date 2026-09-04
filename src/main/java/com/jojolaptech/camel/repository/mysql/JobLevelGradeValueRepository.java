package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.JobLevelGradeValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JobLevelGradeValueRepository extends JpaRepository<JobLevelGradeValue, Long> {

    @Query(
            value = """
                    SELECT g FROM JobLevelGradeValue g
                    JOIN FETCH g.jobLevel jl
                    JOIN FETCH jl.company
                    WHERE g.status = true
                    """,
            countQuery = """
                    SELECT count(g) FROM JobLevelGradeValue g
                    WHERE g.status = true
                    """)
    Page<JobLevelGradeValue> findMigratable(Pageable pageable);
}
