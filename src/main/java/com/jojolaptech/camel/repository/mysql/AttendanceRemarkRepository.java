package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.AttendanceRemark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRemarkRepository extends JpaRepository<AttendanceRemark, Long> {

    @Query(
            value = """
                    SELECT r FROM AttendanceRemark r
                    JOIN FETCH r.employee
                    JOIN FETCH r.company
                    """,
            countQuery = "SELECT count(r) FROM AttendanceRemark r")
    Page<AttendanceRemark> findMigratable(Pageable pageable);
}
