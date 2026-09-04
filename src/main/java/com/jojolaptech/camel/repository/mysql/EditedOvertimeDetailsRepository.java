package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EditedOvertimeDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EditedOvertimeDetailsRepository extends JpaRepository<EditedOvertimeDetails, Long> {

    @Query(
            value = """
                    SELECT e FROM EditedOvertimeDetails e
                    JOIN FETCH e.attendanceTransaction
                    """,
            countQuery = "SELECT count(e) FROM EditedOvertimeDetails e")
    Page<EditedOvertimeDetails> findMigratable(Pageable pageable);
}
