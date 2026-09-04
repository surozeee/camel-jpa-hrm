package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.ScreeningAnswer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ScreeningAnswerRepository extends JpaRepository<ScreeningAnswer, Long> {

    @Query(
            value =
                    """
                    SELECT sa FROM ScreeningAnswer sa
                    JOIN FETCH sa.applicant
                    JOIN FETCH sa.screeningQuestion
                    """,
            countQuery = "SELECT count(sa) FROM ScreeningAnswer sa")
    Page<ScreeningAnswer> findMigratable(Pageable pageable);
}
