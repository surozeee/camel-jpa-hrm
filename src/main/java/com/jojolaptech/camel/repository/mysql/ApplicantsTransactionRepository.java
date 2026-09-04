package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.ApplicantsTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicantsTransactionRepository extends JpaRepository<ApplicantsTransaction, Long> {

    @Query(
            value =
                    """
                    SELECT tx FROM ApplicantsTransaction tx
                    JOIN FETCH tx.applicant
                    LEFT JOIN FETCH tx.stage
                    """,
            countQuery = "SELECT count(tx) FROM ApplicantsTransaction tx")
    Page<ApplicantsTransaction> findMigratable(Pageable pageable);
}
