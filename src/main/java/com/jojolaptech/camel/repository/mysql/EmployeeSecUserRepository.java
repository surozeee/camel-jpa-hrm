package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeSecUser;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeSecUserRepository extends JpaRepository<EmployeeSecUser, Long> {

    @Query("select e.user.id from EmployeeSecUser e where e.user.id in :userIds")
    Set<Long> findEmployeeUserIds(@Param("userIds") Collection<Long> userIds);

    @Query("""
            select e from EmployeeSecUser e
            join fetch e.employee
            where e.user.id in :userIds
            """)
    List<EmployeeSecUser> findByUserIdInWithEmployee(@Param("userIds") Collection<Long> userIds);

    @Query("""
            select e from EmployeeSecUser e
            join fetch e.user
            join fetch e.employee
            where e.employee.id in :employeeIds
            """)
    List<EmployeeSecUser> findByEmployeeIdInWithUser(@Param("employeeIds") Collection<Long> employeeIds);
}
