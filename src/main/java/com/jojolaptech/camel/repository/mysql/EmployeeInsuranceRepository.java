package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeInsurance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeInsuranceRepository extends JpaRepository<EmployeeInsurance, Long> {

    @Query(
            value = "SELECT e FROM EmployeeInsurance e JOIN FETCH e.employee JOIN FETCH e.insuranceCompany",
            countQuery = "SELECT count(e) FROM EmployeeInsurance e")
    Page<EmployeeInsurance> findMigratable(Pageable pageable);
}
