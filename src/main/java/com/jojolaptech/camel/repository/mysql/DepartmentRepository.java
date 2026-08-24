package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Query(
            value = """
                    select d from Department d
                    join fetch d.company
                    left join fetch d.parentDepartment
                    """,
            countQuery = "select count(d) from Department d")
    Page<Department> findMigratable(Pageable pageable);
}
