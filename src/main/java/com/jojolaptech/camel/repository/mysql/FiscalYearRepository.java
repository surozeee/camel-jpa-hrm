package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.FiscalYear;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FiscalYearRepository extends JpaRepository<FiscalYear, Long> {

    @Query(
            value = "SELECT f FROM FiscalYear f JOIN FETCH f.company",
            countQuery = "SELECT count(f) FROM FiscalYear f")
    Page<FiscalYear> findMigratable(Pageable pageable);
}
