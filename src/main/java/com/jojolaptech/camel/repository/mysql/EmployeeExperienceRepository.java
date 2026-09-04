package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeExperience;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeExperienceRepository extends JpaRepository<EmployeeExperience, Long> {

    @Query(
            value = "SELECT e FROM EmployeeExperience e JOIN FETCH e.employee",
            countQuery = "SELECT count(e) FROM EmployeeExperience e")
    Page<EmployeeExperience> findMigratable(Pageable pageable);
}
