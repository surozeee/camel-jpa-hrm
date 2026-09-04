package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeePayrollPaymentSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeePayrollPaymentSettingRepository
        extends JpaRepository<EmployeePayrollPaymentSetting, Long> {

    @Query(
            value = """
                    SELECT s FROM EmployeePayrollPaymentSetting s
                    JOIN FETCH s.employee
                    JOIN FETCH s.bank
                    WHERE s.bank IS NOT NULL
                      AND s.institutionIdentity IS NOT NULL
                      AND TRIM(s.institutionIdentity) <> ''
                    """,
            countQuery = """
                    SELECT count(s) FROM EmployeePayrollPaymentSetting s
                    WHERE s.bank IS NOT NULL
                      AND s.institutionIdentity IS NOT NULL
                      AND TRIM(s.institutionIdentity) <> ''
                    """)
    Page<EmployeePayrollPaymentSetting> findMigratable(Pageable pageable);
}
