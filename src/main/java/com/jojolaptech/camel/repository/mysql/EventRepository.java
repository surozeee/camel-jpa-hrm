package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query(
            value =
                    """
                    SELECT e FROM Event e
                    JOIN FETCH e.company
                    LEFT JOIN FETCH e.senderEmployee
                    """,
            countQuery = "SELECT count(e) FROM Event e")
    Page<Event> findMigratable(Pageable pageable);
}
