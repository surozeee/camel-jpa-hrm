package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.AttDeviceMAC;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.DeviceMacEntity;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgDeviceMacRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttDeviceMacProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(AttDeviceMacProcessor.class);

    private final PgDeviceMacRepository deviceMacRepository;
    private final PgCompanyRepository companyRepository;
    private final PgBranchRepository branchRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<AttDeviceMAC> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(AttDeviceMAC::getId).collect(Collectors.toSet());
        Set<Long> existingMysqlIds = deviceMacRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<String> macAddresses = batch.stream()
                .map(AttDeviceMAC::getMacId)
                .filter(mac -> mac != null && !mac.isBlank())
                .map(String::trim)
                .collect(Collectors.toSet());
        Set<String> existingMacs = macAddresses.isEmpty()
                ? Set.of()
                : deviceMacRepository.findMacAddressesByMacAddressIn(macAddresses);

        Set<Long> companyMysqlIds = batch.stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companyByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, c -> c, (a, b) -> a));

        Map<Long, List<BranchEntity>> branchesByCompanyMysqlId =
                branchRepository.findByCompanyMysqlIdIn(companyMysqlIds).stream()
                        .collect(Collectors.groupingBy(b -> b.getCompany().getMysqlId()));

        List<DeviceMacEntity> toSave = new ArrayList<>();
        Set<String> pendingMacs = new HashSet<>(existingMacs);
        for (AttDeviceMAC source : batch) {
            if (existingMysqlIds.contains(source.getId())) {
                continue;
            }
            if (source.getCompany() == null) {
                log.warn("Skipping attDeviceMAC id={}, missing company", source.getId());
                continue;
            }
            String macAddress = source.getMacId() == null ? null : source.getMacId().trim();
            if (macAddress == null || macAddress.isEmpty()) {
                log.warn("Skipping attDeviceMAC id={}, blank macId", source.getId());
                continue;
            }
            if (pendingMacs.contains(macAddress)) {
                log.warn("Skipping attDeviceMAC id={}, macAddress={} already exists", source.getId(), macAddress);
                continue;
            }
            CompanyEntity company = companyByMysqlId.get(source.getCompany().getId());
            if (company == null) {
                log.warn(
                        "Skipping attDeviceMAC id={}, company mysqlId={} not migrated",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }
            List<BranchEntity> branches =
                    branchesByCompanyMysqlId.getOrDefault(source.getCompany().getId(), List.of());
            if (branches.isEmpty()) {
                log.warn(
                        "Skipping attDeviceMAC id={}, no migrated branch for company mysqlId={}",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }
            BranchEntity primaryBranch = branches.stream()
                    .sorted((a, b) -> Long.compare(
                            a.getMysqlId() == null ? Long.MAX_VALUE : a.getMysqlId(),
                            b.getMysqlId() == null ? Long.MAX_VALUE : b.getMysqlId()))
                    .findFirst()
                    .orElse(branches.get(0));

            String deviceName = source.getDeviceName() == null || source.getDeviceName().isBlank()
                    ? macAddress
                    : source.getDeviceName().trim();

            toSave.add(DeviceMacEntity.builder()
                    .mysqlId(source.getId())
                    .macAddress(macAddress)
                    .deviceName(deviceName)
                    .deviceSerialNumber(source.getDeviceSn())
                    .companyId(company.getId())
                    .branchId(primaryBranch.getId())
                    .build());
            existingMysqlIds.add(source.getId());
            pendingMacs.add(macAddress);
        }

        if (!toSave.isEmpty()) {
            deviceMacRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
