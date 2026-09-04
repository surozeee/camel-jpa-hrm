package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.BranchPayPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchPayPeriodRepository extends JpaRepository<BranchPayPeriod, Long> {

    @Query(
            value = """
                    SELECT b FROM BranchPayPeriod b
                    JOIN FETCH b.payPeriod
                    JOIN FETCH b.branch
                    """,
            countQuery = "SELECT count(b) FROM BranchPayPeriod b")
    Page<BranchPayPeriod> findMigratable(Pageable pageable);
}
