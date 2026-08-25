package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Leaves;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LeavesRepository extends JpaRepository<Leaves, Long> {

    @Query(
            value = "SELECT l FROM Leaves l JOIN FETCH l.company",
            countQuery = "SELECT count(l) FROM Leaves l")
    Page<Leaves> findMigratable(Pageable pageable);
}
