package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.CompanySecUser;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanySecUserRepository extends JpaRepository<CompanySecUser, CompanySecUser.Pk> {

    @Query("select c from CompanySecUser c where c.secUserId in :userIds")
    List<CompanySecUser> findBySecUserIdIn(@Param("userIds") Collection<Long> userIds);
}
