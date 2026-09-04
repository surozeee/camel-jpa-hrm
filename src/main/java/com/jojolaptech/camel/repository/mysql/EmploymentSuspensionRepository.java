package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmploymentSuspension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmploymentSuspensionRepository extends JpaRepository<EmploymentSuspension, Long> {

    @Query(
            value = "SELECT e FROM EmploymentSuspension e JOIN FETCH e.employee",
            countQuery = "SELECT count(e) FROM EmploymentSuspension e")
    Page<EmploymentSuspension> findMigratable(Pageable pageable);
}
