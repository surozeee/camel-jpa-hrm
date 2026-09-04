package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.DeviceLogs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceLogsRepository extends JpaRepository<DeviceLogs, Long> {

    @Query(
            value = """
                    SELECT d FROM DeviceLogs d JOIN FETCH d.company
                    """,
            countQuery = "SELECT count(d) FROM DeviceLogs d")
    Page<DeviceLogs> findMigratable(Pageable pageable);
}
