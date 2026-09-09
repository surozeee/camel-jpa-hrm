package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.LeaveCancellation;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveCancellationRepository extends JpaRepository<LeaveCancellation, Long> {

    @Query(
            value = "SELECT lc.id FROM LeaveCancellation lc",
            countQuery = "SELECT count(lc) FROM LeaveCancellation lc")
    Page<Long> findMigratableIds(Pageable pageable);

    @Query("""
            SELECT lc FROM LeaveCancellation lc
            JOIN FETCH lc.leaveApplication
            LEFT JOIN FETCH lc.respondByEmp
            WHERE lc.id IN :ids
            """)
    List<LeaveCancellation> findByIdInWithGraph(@Param("ids") Collection<Long> ids);
}
