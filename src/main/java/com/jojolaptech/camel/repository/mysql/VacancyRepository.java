package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Vacancy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, Long> {

    @Query(
            value =
                    """
                    SELECT v FROM Vacancy v
                    LEFT JOIN FETCH v.company
                    LEFT JOIN FETCH v.hiringManager
                    JOIN FETCH v.jobCategory
                    """,
            countQuery = "SELECT count(v) FROM Vacancy v")
    Page<Vacancy> findMigratable(Pageable pageable);
}
