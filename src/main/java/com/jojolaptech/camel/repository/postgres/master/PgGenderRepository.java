package com.jojolaptech.camel.repository.postgres.master;

import com.jojolaptech.camel.model.postgres.master.GenderEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PgGenderRepository extends JpaRepository<GenderEntity, java.util.UUID> {
    Optional<GenderEntity> findByCode(String code);
}
