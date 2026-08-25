package com.jojolaptech.camel.repository.postgres.master;

import com.jojolaptech.camel.model.postgres.master.CountryEntity;
import com.jojolaptech.camel.model.postgres.enums.CountryEnum;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PgCountryRepository extends JpaRepository<CountryEntity, java.util.UUID> {
    Optional<CountryEntity> findByIso2(CountryEnum iso2);
}
