package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.JobDescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {

    @Query(
            value = "SELECT e FROM JobDescription e JOIN FETCH e.employee",
            countQuery = "SELECT count(e) FROM JobDescription e")
    Page<JobDescription> findMigratable(Pageable pageable);
}
