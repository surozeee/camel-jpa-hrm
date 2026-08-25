package com.jojolaptech.camel.repository.postgres.master;

import com.jojolaptech.camel.model.postgres.master.EmploymentTypeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PgEmploymentTypeRepository extends JpaRepository<EmploymentTypeEntity, java.util.UUID> {
    Optional<EmploymentTypeEntity> findByCode(String code);
}
