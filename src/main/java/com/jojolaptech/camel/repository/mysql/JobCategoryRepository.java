package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {}
