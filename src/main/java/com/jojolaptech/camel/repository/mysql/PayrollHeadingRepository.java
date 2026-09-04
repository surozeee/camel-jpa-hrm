package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.PayrollHeading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollHeadingRepository extends JpaRepository<PayrollHeading, Long> {

    @Query(
            value = "SELECT p FROM PayrollHeading p JOIN FETCH p.company",
            countQuery = "SELECT count(p) FROM PayrollHeading p")
    Page<PayrollHeading> findMigratable(Pageable pageable);
}
