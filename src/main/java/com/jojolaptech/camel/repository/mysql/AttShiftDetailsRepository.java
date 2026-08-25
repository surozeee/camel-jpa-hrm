package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.AttShiftDetails;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttShiftDetailsRepository extends JpaRepository<AttShiftDetails, Long> {

    @Query("""
            SELECT d FROM AttShiftDetails d
            JOIN FETCH d.attShift
            LEFT JOIN FETCH d.attTimeTable
            JOIN FETCH d.company
            WHERE d.attShift.id IN :shiftIds AND d.isDisabled = false
            """)
    List<AttShiftDetails> findActiveByAttShiftIdIn(@Param("shiftIds") Collection<Long> shiftIds);
}
