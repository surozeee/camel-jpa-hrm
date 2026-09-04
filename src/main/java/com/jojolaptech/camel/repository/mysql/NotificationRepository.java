package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(
            value =
                    """
                    SELECT n FROM Notification n
                    JOIN FETCH n.company
                    """,
            countQuery = "SELECT count(n) FROM Notification n")
    Page<Notification> findMigratable(Pageable pageable);
}
