package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.PayrollInstitution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollInstitutionRepository extends JpaRepository<PayrollInstitution, Long> {

    @Query(
            value = "SELECT p FROM PayrollInstitution p",
            countQuery = "SELECT count(p) FROM PayrollInstitution p")
    Page<PayrollInstitution> findMigratable(Pageable pageable);
}
