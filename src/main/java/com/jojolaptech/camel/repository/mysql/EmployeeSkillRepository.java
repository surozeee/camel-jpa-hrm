package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeSkill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {

    @Query(
            value = "SELECT e FROM EmployeeSkill e JOIN FETCH e.employee",
            countQuery = "SELECT count(e) FROM EmployeeSkill e")
    Page<EmployeeSkill> findMigratable(Pageable pageable);
}
