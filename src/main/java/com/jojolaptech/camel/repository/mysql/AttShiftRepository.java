package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.AttShift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AttShiftRepository extends JpaRepository<AttShift, Long> {

    @Query(
            value = "SELECT s FROM AttShift s JOIN FETCH s.company WHERE s.isDisabled = false",
            countQuery = "SELECT count(s) FROM AttShift s WHERE s.isDisabled = false")
    Page<AttShift> findMigratable(Pageable pageable);
}
