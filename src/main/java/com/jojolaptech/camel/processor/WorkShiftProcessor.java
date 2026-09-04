package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.WorkShift;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.RosterShiftSlotEntity;
import com.jojolaptech.camel.model.postgres.company.enums.RosterShiftSlotEnum;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgRosterShiftSlotRepository;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkShiftProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(WorkShiftProcessor.class);

    private final PgBranchRepository branchRepository;
    private final PgRosterShiftSlotRepository rosterShiftSlotRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<WorkShift> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> companyMysqlIds = batch.stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, List<BranchEntity>> branchesByCompany =
                branchRepository.findByCompanyMysqlIdIn(companyMysqlIds).stream()
                        .collect(Collectors.groupingBy(branch -> branch.getCompany().getMysqlId()));

        Set<UUID> branchIds = branchesByCompany.values().stream()
                .flatMap(List::stream)
                .map(BranchEntity::getId)
                .collect(Collectors.toSet());
        Map<String, RosterShiftSlotEntity> existingByBranchSlot = new HashMap<>();
        if (!branchIds.isEmpty()) {
            for (RosterShiftSlotEntity slot : rosterShiftSlotRepository.findByBranchIdIn(branchIds)) {
                existingByBranchSlot.put(branchSlotKey(slot.getBranchId(), slot.getShiftSlot()), slot);
            }
        }

        Set<Long> workShiftMysqlIds = batch.stream()
                .map(row -> AttendancePunchMigrationMapper.workShiftMysqlId(row.getId()))
                .collect(Collectors.toSet());
        Set<Long> existingMysqlIds = new HashSet<>(rosterShiftSlotRepository.findMysqlIdsByMysqlIdIn(workShiftMysqlIds));

        List<RosterShiftSlotEntity> toSave = new ArrayList<>();
        int imported = 0;

        for (WorkShift source : batch) {
            if (source.getCompany() == null) {
                log.warn("Skipping workShift id={}, missing company", source.getId());
                continue;
            }
            if (source.getStartTime() == null || source.getEndTime() == null) {
                log.warn("Skipping workShift id={}, missing start/end time", source.getId());
                continue;
            }

            List<BranchEntity> branches = branchesByCompany.getOrDefault(source.getCompany().getId(), List.of());
            if (branches.isEmpty()) {
                log.warn(
                        "Skipping workShift id={}, no migrated branches for company mysqlId={}",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }

            List<BranchEntity> ordered = branches.stream()
                    .sorted(Comparator.comparing(
                            b -> b.getMysqlId() == null ? Long.MAX_VALUE : b.getMysqlId()))
                    .toList();
            BranchEntity primaryBranch = ordered.get(0);

            RosterShiftSlotEnum shiftSlot = AttendancePunchMigrationMapper.mapWorkShiftName(source.getShift());
            LocalTime startTime = AttendancePunchMigrationMapper.toLocalTimeFromDate(source.getStartTime());
            LocalTime endTime = AttendancePunchMigrationMapper.toLocalTimeFromDate(source.getEndTime());
            String label = source.getShift() == null || source.getShift().isBlank()
                    ? shiftSlot.getDisplayName()
                    : source.getShift().trim();
            long primaryMysqlId = AttendancePunchMigrationMapper.workShiftMysqlId(source.getId());

            for (BranchEntity branch : ordered) {
                String key = branchSlotKey(branch.getId(), shiftSlot);
                RosterShiftSlotEntity existing = existingByBranchSlot.get(key);
                boolean isPrimary = Objects.equals(branch.getId(), primaryBranch.getId());

                if (existing != null) {
                    boolean sameTimes = Objects.equals(existing.getStartTime(), startTime)
                            && Objects.equals(existing.getEndTime(), endTime)
                            && Objects.equals(existing.getLabel(), label);
                    boolean mysqlOk = existing.getMysqlId() == null
                            || Objects.equals(existing.getMysqlId(), primaryMysqlId)
                            || !isPrimary;
                    if (sameTimes && mysqlOk) {
                        if (isPrimary
                                && existing.getMysqlId() == null
                                && !existingMysqlIds.contains(primaryMysqlId)) {
                            existing.setMysqlId(primaryMysqlId);
                            existingMysqlIds.add(primaryMysqlId);
                            toSave.add(existing);
                            imported++;
                        }
                        continue;
                    }
                    if (isPrimary
                            && existing.getMysqlId() != null
                            && !Objects.equals(existing.getMysqlId(), primaryMysqlId)) {
                        log.warn(
                                "Skipping workShift id={} primary branch slot update; mysqlId conflict existing={}",
                                source.getId(),
                                existing.getMysqlId());
                        continue;
                    }
                    if (isPrimary
                            && existing.getMysqlId() == null
                            && !existingMysqlIds.contains(primaryMysqlId)) {
                        existing.setMysqlId(primaryMysqlId);
                        existingMysqlIds.add(primaryMysqlId);
                    }
                    existing.setLabel(label);
                    existing.setStartTime(startTime);
                    existing.setEndTime(endTime);
                    if (existing.getEnabled() == null) {
                        existing.setEnabled(true);
                    }
                    toSave.add(existing);
                    if (existing.getMysqlId() != null) {
                        imported++;
                    }
                    continue;
                }

                boolean assignMysqlId = isPrimary && !existingMysqlIds.contains(primaryMysqlId);
                if (isPrimary && !assignMysqlId) {
                    log.warn(
                            "Skipping workShift id={} primary mysqlId assign; offset mysqlId={} already exists",
                            source.getId(),
                            primaryMysqlId);
                }

                RosterShiftSlotEntity created = RosterShiftSlotEntity.builder()
                        .mysqlId(assignMysqlId ? primaryMysqlId : null)
                        .branchId(branch.getId())
                        .shiftSlot(shiftSlot)
                        .label(label)
                        .startTime(startTime)
                        .endTime(endTime)
                        .enabled(true)
                        .build();
                if (assignMysqlId) {
                    existingMysqlIds.add(primaryMysqlId);
                }
                existingByBranchSlot.put(key, created);
                toSave.add(created);
                if (assignMysqlId) {
                    imported++;
                }
            }
        }

        if (!toSave.isEmpty()) {
            rosterShiftSlotRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", imported);
    }

    private static String branchSlotKey(UUID branchId, RosterShiftSlotEnum shiftSlot) {
        return branchId + ":" + shiftSlot.name();
    }
}
