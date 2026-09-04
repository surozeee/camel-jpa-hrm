package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.PayrollMonth;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollMonthRepository extends JpaRepository<PayrollMonth, Long> {

    @Query(
            value = "SELECT m FROM PayrollMonth m JOIN FETCH m.company",
            countQuery = "SELECT count(m) FROM PayrollMonth m")
    Page<PayrollMonth> findMigratable(Pageable pageable);
}
