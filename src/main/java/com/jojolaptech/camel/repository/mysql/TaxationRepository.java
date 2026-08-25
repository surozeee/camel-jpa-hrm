package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Taxation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TaxationRepository extends JpaRepository<Taxation, Long> {

    @Query(
            value = "SELECT t FROM Taxation t JOIN FETCH t.company JOIN FETCH t.fiscalYear",
            countQuery = "SELECT count(t) FROM Taxation t")
    Page<Taxation> findMigratable(Pageable pageable);
}
