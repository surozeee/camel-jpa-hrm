package com.jojolaptech.camel.repository.postgres.master;

import com.jojolaptech.camel.model.postgres.master.SalutationEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PgSalutationRepository extends JpaRepository<SalutationEntity, java.util.UUID> {
    Optional<SalutationEntity> findByCode(String code);
}
