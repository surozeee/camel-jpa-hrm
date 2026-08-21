package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.SecUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecUserRepository extends JpaRepository<SecUser, Long> {}
