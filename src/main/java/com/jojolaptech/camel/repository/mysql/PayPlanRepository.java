package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.PayPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PayPlanRepository extends JpaRepository<PayPlan, Long> {

    @Query(
            value = """
                    SELECT p FROM PayPlan p
                    LEFT JOIN FETCH p.costType
                    LEFT JOIN FETCH p.payType
                    """,
            countQuery = """
                    SELECT count(p) FROM PayPlan p
                    """)
    Page<PayPlan> findMigratable(Pageable pageable);
}
