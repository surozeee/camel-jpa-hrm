package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.SubscriptionPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, Long> {

    @Query(
            value = """
                    SELECT s FROM SubscriptionPayment s
                    JOIN FETCH s.company
                    """,
            countQuery = """
                    SELECT count(s) FROM SubscriptionPayment s
                    """)
    Page<SubscriptionPayment> findMigratable(Pageable pageable);
}
