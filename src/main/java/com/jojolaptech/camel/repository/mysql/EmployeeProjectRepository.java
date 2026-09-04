package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeProjectRepository extends JpaRepository<EmployeeProject, Long> {

    @Query(
            value = "SELECT e FROM EmployeeProject e JOIN FETCH e.employee",
            countQuery = "SELECT count(e) FROM EmployeeProject e")
    Page<EmployeeProject> findMigratable(Pageable pageable);
}
