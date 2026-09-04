package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.PricingEstimateEmailDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PricingEstimateEmailDetailsRepository
        extends JpaRepository<PricingEstimateEmailDetails, Long> {

    @Query(
            value = "SELECT p FROM PricingEstimateEmailDetails p",
            countQuery = "SELECT count(p) FROM PricingEstimateEmailDetails p")
    Page<PricingEstimateEmailDetails> findMigratable(Pageable pageable);
}
