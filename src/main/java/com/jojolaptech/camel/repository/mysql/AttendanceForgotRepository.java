package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.AttendanceForgot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceForgotRepository extends JpaRepository<AttendanceForgot, Long> {

    @Query(
            value = """
                    SELECT f FROM AttendanceForgot f
                    JOIN FETCH f.employee
                    JOIN FETCH f.company
                    """,
            countQuery = "SELECT count(f) FROM AttendanceForgot f")
    Page<AttendanceForgot> findMigratable(Pageable pageable);
}
