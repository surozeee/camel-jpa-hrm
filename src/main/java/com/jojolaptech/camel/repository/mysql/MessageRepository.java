package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query(
            value =
                    """
                    SELECT m FROM Message m
                    JOIN FETCH m.company
                    JOIN FETCH m.receiverEmployee
                    """,
            countQuery = "SELECT count(m) FROM Message m")
    Page<Message> findMigratable(Pageable pageable);
}
