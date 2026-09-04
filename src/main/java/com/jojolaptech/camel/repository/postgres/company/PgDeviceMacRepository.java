package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.DeviceMacEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgDeviceMacRepository extends JpaRepository<DeviceMacEntity, UUID> {

    @Query("select d.mysqlId from DeviceMacEntity d where d.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<DeviceMacEntity> findByMysqlId(Long mysqlId);

    Optional<DeviceMacEntity> findByMacAddress(String macAddress);

    @Query("select d.macAddress from DeviceMacEntity d where d.macAddress in :macAddresses")
    Set<String> findMacAddressesByMacAddressIn(@Param("macAddresses") Collection<String> macAddresses);

    List<DeviceMacEntity> findByCompanyIdOrderByMysqlIdAsc(UUID companyId);

    @Query("select d from DeviceMacEntity d where d.companyId in :companyIds")
    List<DeviceMacEntity> findByCompanyIdIn(@Param("companyIds") Collection<UUID> companyIds);
}
