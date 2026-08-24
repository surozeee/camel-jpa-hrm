package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.BranchDepartment;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchDepartmentRepository extends JpaRepository<BranchDepartment, Long> {

    @Query(
            value = """
                    select bd from BranchDepartment bd
                    join fetch bd.branch b
                    join fetch b.company
                    join fetch bd.department d
                    left join fetch d.parentDepartment
                    where bd.department is not null
                    """,
            countQuery = "select count(bd) from BranchDepartment bd where bd.department is not null")
    Page<BranchDepartment> findMigratable(Pageable pageable);

    @Query("select bd.department.id from BranchDepartment bd where bd.department.id in :departmentIds")
    Set<Long> findDepartmentIdsLinkedToBranch(@Param("departmentIds") Collection<Long> departmentIds);

    @Query("select bd from BranchDepartment bd join fetch bd.branch where bd.department.id = :departmentId")
    List<BranchDepartment> findByDepartment_Id(@Param("departmentId") Long departmentId);

    boolean existsByDepartment_Id(Long departmentId);
}
