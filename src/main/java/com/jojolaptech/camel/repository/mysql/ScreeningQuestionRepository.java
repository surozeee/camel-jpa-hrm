package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.ScreeningQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ScreeningQuestionRepository extends JpaRepository<ScreeningQuestion, Long> {

    @Query(
            value = "SELECT sq FROM ScreeningQuestion sq JOIN FETCH sq.vacancy",
            countQuery = "SELECT count(sq) FROM ScreeningQuestion sq")
    Page<ScreeningQuestion> findMigratable(Pageable pageable);
}
