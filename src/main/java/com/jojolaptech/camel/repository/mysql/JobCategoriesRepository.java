package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.JobCategories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JobCategoriesRepository extends JpaRepository<JobCategories, Long> {

    @Query(
            value = "SELECT j FROM JobCategories j JOIN FETCH j.company",
            countQuery = "SELECT count(j) FROM JobCategories j")
    Page<JobCategories> findMigratable(Pageable pageable);
}
