package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.PayrollSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollSettingRepository extends JpaRepository<PayrollSetting, Long> {

    @Query(
            value = """
                    SELECT p FROM PayrollSetting p
                    JOIN FETCH p.employee
                    JOIN FETCH p.payrollheading
                    JOIN FETCH p.payrollMonth
                    """,
            countQuery = "SELECT count(p) FROM PayrollSetting p")
    Page<PayrollSetting> findMigratable(Pageable pageable);
}
