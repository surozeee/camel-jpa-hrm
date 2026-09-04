package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.MarketingPersonDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketingPersonDetailRepository extends JpaRepository<MarketingPersonDetail, Long> {

    @Query(
            value = "SELECT m FROM MarketingPersonDetail m",
            countQuery = "SELECT count(m) FROM MarketingPersonDetail m")
    Page<MarketingPersonDetail> findMigratable(Pageable pageable);
}
