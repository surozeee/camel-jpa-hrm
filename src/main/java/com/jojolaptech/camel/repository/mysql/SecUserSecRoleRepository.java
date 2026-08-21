package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.SecUserSecRole;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SecUserSecRoleRepository extends JpaRepository<SecUserSecRole, SecUserSecRole.Pk> {

    @Query("""
            select s from SecUserSecRole s
            join fetch s.secUser
            join fetch s.secRole
            where s.secUser.id in :userIds
            """)
    List<SecUserSecRole> findBySecUserIdIn(@Param("userIds") Collection<Long> userIds);
}
