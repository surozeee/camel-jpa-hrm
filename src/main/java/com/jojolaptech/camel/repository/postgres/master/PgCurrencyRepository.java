package com.jojolaptech.camel.repository.postgres.master;

import com.jojolaptech.camel.model.postgres.master.CurrencyEntity;
import com.jojolaptech.camel.model.postgres.enums.CurrencyEnum;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PgCurrencyRepository extends JpaRepository<CurrencyEntity, java.util.UUID> {
    Optional<CurrencyEntity> findByCode(CurrencyEnum code);
}
