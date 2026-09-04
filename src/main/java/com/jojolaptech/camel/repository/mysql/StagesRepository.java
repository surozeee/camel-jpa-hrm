package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Stages;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StagesRepository extends JpaRepository<Stages, Long> {

    @Query(
            value = "SELECT s FROM Stages s JOIN FETCH s.vacancy",
            countQuery = "SELECT count(s) FROM Stages s")
    Page<Stages> findMigratable(Pageable pageable);
}
