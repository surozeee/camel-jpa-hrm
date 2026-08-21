package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.EmployeeSecUser;
import java.util.Collection;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeSecUserRepository extends JpaRepository<EmployeeSecUser, Long> {

    @Query("select e.user.id from EmployeeSecUser e where e.user.id in :userIds")
    Set<Long> findEmployeeUserIds(@Param("userIds") Collection<Long> userIds);
}
