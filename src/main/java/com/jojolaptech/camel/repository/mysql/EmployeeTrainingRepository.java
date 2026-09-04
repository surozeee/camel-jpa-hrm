package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeTraining;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeTrainingRepository extends JpaRepository<EmployeeTraining, Long> {

    @Query(
            value = "SELECT e FROM EmployeeTraining e JOIN FETCH e.employee",
            countQuery = "SELECT count(e) FROM EmployeeTraining e")
    Page<EmployeeTraining> findMigratable(Pageable pageable);
}
