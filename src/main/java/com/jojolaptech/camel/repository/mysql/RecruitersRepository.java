package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Recruiters;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruitersRepository extends JpaRepository<Recruiters, Long> {

    @Query(
            value =
                    """
                    SELECT r FROM Recruiters r
                    JOIN FETCH r.employee
                    JOIN FETCH r.vacancy
                    """,
            countQuery = "SELECT count(r) FROM Recruiters r")
    Page<Recruiters> findMigratable(Pageable pageable);
}
