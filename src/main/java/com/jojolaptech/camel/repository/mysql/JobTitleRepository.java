package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.JobTitle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JobTitleRepository extends JpaRepository<JobTitle, Long> {

    @Query(
            value = "SELECT e FROM JobTitle e JOIN FETCH e.company",
            countQuery = "SELECT count(e) FROM JobTitle e")
    Page<JobTitle> findMigratable(Pageable pageable);
}
