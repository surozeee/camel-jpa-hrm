package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.PayByOnlineTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PayByOnlineTransactionRepository extends JpaRepository<PayByOnlineTransaction, Long> {

    @Query(
            value = """
                    SELECT p FROM PayByOnlineTransaction p
                    JOIN FETCH p.companyValidityId cv
                    JOIN FETCH cv.company
                    """,
            countQuery = "SELECT count(p) FROM PayByOnlineTransaction p")
    Page<PayByOnlineTransaction> findMigratable(Pageable pageable);
}
