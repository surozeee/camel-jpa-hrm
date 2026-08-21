package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.SecRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecRoleRepository extends JpaRepository<SecRole, Long> {}
