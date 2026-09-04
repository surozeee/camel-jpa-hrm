package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.TempDeviceLogs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TempDeviceLogsRepository extends JpaRepository<TempDeviceLogs, Long> {

    @Query(
            value = """
                    SELECT d FROM TempDeviceLogs d JOIN FETCH d.company
                    """,
            countQuery = "SELECT count(d) FROM TempDeviceLogs d")
    Page<TempDeviceLogs> findMigratable(Pageable pageable);
}
