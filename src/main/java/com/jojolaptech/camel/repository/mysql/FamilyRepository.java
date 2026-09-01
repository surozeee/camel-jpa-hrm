package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Family;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {

    @Query(
            value = "SELECT f FROM Family f JOIN FETCH f.employee",
            countQuery = "SELECT count(f) FROM Family f")
    Page<Family> findMigratable(Pageable pageable);

    @Query("""
            SELECT f FROM Family f JOIN FETCH f.employee
            WHERE f.employee.id IN :employeeIds
            """)
    List<Family> findByEmployeeIdIn(@Param("employeeIds") Collection<Long> employeeIds);
}
