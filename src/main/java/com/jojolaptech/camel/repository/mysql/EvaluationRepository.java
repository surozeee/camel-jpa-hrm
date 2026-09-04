package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Evaluation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    @Query(
            value =
                    """
                    SELECT e FROM Evaluation e
                    JOIN FETCH e.applicant
                    JOIN FETCH e.recruiter r
                    JOIN FETCH r.employee
                    """,
            countQuery = "SELECT count(e) FROM Evaluation e")
    Page<Evaluation> findMigratable(Pageable pageable);
}
