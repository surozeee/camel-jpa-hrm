package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.CalculatedTypeValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CalculatedTypeValueRepository extends JpaRepository<CalculatedTypeValue, Long> {

    @Query(
            value = """
                    SELECT c FROM CalculatedTypeValue c
                    JOIN FETCH c.employee
                    JOIN FETCH c.payPeriod
                    """,
            countQuery = "SELECT count(c) FROM CalculatedTypeValue c")
    Page<CalculatedTypeValue> findMigratable(Pageable pageable);
}
