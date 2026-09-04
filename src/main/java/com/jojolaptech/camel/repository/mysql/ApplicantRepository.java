package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Applicant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    @Query(
            value =
                    """
                    SELECT a FROM Applicant a
                    JOIN FETCH a.vacancy
                    JOIN FETCH a.employee
                    LEFT JOIN FETCH a.stage
                    """,
            countQuery = "SELECT count(a) FROM Applicant a")
    Page<Applicant> findMigratable(Pageable pageable);
}
