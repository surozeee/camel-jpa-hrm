package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.PayType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PayTypeRepository extends JpaRepository<PayType, Long> {

    @Query(
            value = "SELECT p FROM PayType p",
            countQuery = "SELECT count(p) FROM PayType p")
    Page<PayType> findMigratable(Pageable pageable);
}
