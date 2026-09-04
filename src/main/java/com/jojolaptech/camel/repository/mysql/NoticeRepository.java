package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query(
            value =
                    """
                    SELECT n FROM Notice n
                    JOIN FETCH n.company
                    LEFT JOIN FETCH n.senderEmployee
                    """,
            countQuery = "SELECT count(n) FROM Notice n")
    Page<Notice> findMigratable(Pageable pageable);
}
