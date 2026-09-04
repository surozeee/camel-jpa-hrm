package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.PayrollLabel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollLabelRepository extends JpaRepository<PayrollLabel, Long> {

    @Query(
            value = "SELECT p FROM PayrollLabel p",
            countQuery = "SELECT count(p) FROM PayrollLabel p")
    Page<PayrollLabel> findMigratable(Pageable pageable);
}
