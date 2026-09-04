package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query(
            value = "SELECT d FROM Document d JOIN FETCH d.employee",
            countQuery = "SELECT count(d) FROM Document d")
    Page<Document> findMigratable(Pageable pageable);
}
