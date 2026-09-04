package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.NotificationViewed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationViewedRepository extends JpaRepository<NotificationViewed, Long> {

    @Query(
            value =
                    """
                    SELECT nv FROM NotificationViewed nv
                    JOIN FETCH nv.notification n
                    JOIN FETCH n.company
                    """,
            countQuery = "SELECT count(nv) FROM NotificationViewed nv")
    Page<NotificationViewed> findMigratable(Pageable pageable);
}
