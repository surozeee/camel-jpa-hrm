package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.PayrollCalculationSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollCalculationSettingRepository extends JpaRepository<PayrollCalculationSetting, Long> {

    @Query(
            value = "SELECT p FROM PayrollCalculationSetting p JOIN FETCH p.company",
            countQuery = "SELECT count(p) FROM PayrollCalculationSetting p")
    Page<PayrollCalculationSetting> findMigratable(Pageable pageable);
}
