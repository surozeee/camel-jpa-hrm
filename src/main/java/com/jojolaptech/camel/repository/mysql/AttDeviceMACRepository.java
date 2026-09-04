package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.AttDeviceMAC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AttDeviceMACRepository extends JpaRepository<AttDeviceMAC, Long> {

    @Query(
            value = "SELECT d FROM AttDeviceMAC d JOIN FETCH d.company",
            countQuery = "SELECT count(d) FROM AttDeviceMAC d")
    Page<AttDeviceMAC> findMigratable(Pageable pageable);
}
