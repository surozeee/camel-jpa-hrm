package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.WorkShift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkShiftRepository extends JpaRepository<WorkShift, Long> {

    @Query(
            value = """
                    SELECT w FROM WorkShift w JOIN FETCH w.company
                    """,
            countQuery = "SELECT count(w) FROM WorkShift w")
    Page<WorkShift> findMigratable(Pageable pageable);
}
