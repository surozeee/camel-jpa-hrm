package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Bank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {

    @Query(
            value = """
                    SELECT b FROM Bank b
                    """,
            countQuery = """
                    SELECT count(b) FROM Bank b
                    """)
    Page<Bank> findMigratable(Pageable pageable);
}
