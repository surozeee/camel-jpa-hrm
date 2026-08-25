package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.AttHolidayDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AttHolidayDateRepository extends JpaRepository<AttHolidayDate, Long> {

    @Query(
            value = """
                    SELECT h FROM AttHolidayDate h
                    JOIN FETCH h.company
                    JOIN FETCH h.attHolidayList
                    WHERE h.dayHoliday IS NOT NULL
                    """,
            countQuery = "SELECT count(h) FROM AttHolidayDate h WHERE h.dayHoliday IS NOT NULL")
    Page<AttHolidayDate> findMigratable(Pageable pageable);
}
