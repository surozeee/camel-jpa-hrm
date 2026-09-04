package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Happening;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HappeningRepository extends JpaRepository<Happening, Long> {

    @Query(
            value =
                    """
                    SELECT h FROM Happening h
                    JOIN FETCH h.company
                    LEFT JOIN FETCH h.senderEmployee
                    """,
            countQuery = "SELECT count(h) FROM Happening h")
    Page<Happening> findMigratable(Pageable pageable);
}
