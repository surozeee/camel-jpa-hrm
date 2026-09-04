package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.ModulePricing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ModulePricingRepository extends JpaRepository<ModulePricing, Long> {

    @Query(
            value = """
                    SELECT mp FROM ModulePricing mp
                    LEFT JOIN FETCH mp.modulePricingCriteria c
                    LEFT JOIN FETCH c.costType
                    LEFT JOIN FETCH c.payType
                    LEFT JOIN FETCH c.appModule
                    """,
            countQuery = """
                    SELECT count(mp) FROM ModulePricing mp
                    """)
    Page<ModulePricing> findMigratable(Pageable pageable);
}
