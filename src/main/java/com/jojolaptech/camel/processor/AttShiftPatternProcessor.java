package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.AttShift;
import com.jojolaptech.camel.model.mysql.AttShiftDetails;
import com.jojolaptech.camel.model.mysql.AttTimeTable;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.BranchShiftEntity;
import com.jojolaptech.camel.model.postgres.company.BranchShiftWeekdayEntity;
import com.jojolaptech.camel.model.postgres.company.BranchShiftWeekendEntity;
import com.jojolaptech.camel.repository.mysql.AttShiftDetailsRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchShiftRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchShiftWeekdayRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchShiftWeekendRepository;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
public class AttShiftPatternProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(AttShiftPatternProcessor.class);

    private final AttShiftDetailsRepository attShiftDetailsRepository;
    private final PgBranchRepository branchRepository;
    private final PgBranchShiftRepository branchShiftRepository;
    private final PgBranchShiftWeekdayRepository branchShiftWeekdayRepository;
    private final PgBranchShiftWeekendRepository branchShiftWeekendRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<AttShift> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> shiftIds = batch.stream().map(AttShift::getId).collect(Collectors.toSet());
        Map<Long, List<AttShiftDetails>> detailsByShift = attShiftDetailsRepository.findActiveByAttShiftIdIn(shiftIds)
                .stream()
                .collect(Collectors.groupingBy(detail -> detail.getAttShift().getId()));

        Set<Long> companyMysqlIds = batch.stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, List<BranchEntity>> branchesByCompany =
                branchRepository.findByCompanyMysqlIdIn(companyMysqlIds).stream()
                        .collect(Collectors.groupingBy(branch -> branch.getCompany().getMysqlId()));

        Set<Long> timeTableIds = detailsByShift.values().stream()
                .flatMap(List::stream)
                .map(AttShiftDetails::getAttTimeTable)
                .filter(java.util.Objects::nonNull)
                .map(AttTimeTable::getId)
                .collect(Collectors.toSet());
        Set<Long> branchMysqlIds = branchesByCompany.values().stream()
                .flatMap(List::stream)
                .map(BranchEntity::getMysqlId)
                .collect(Collectors.toSet());

        Map<String, BranchShiftEntity> shiftByKey = branchShiftRepository
                .findByMysqlIdInAndMysqlBranchIdIn(timeTableIds, branchMysqlIds)
                .stream()
                .collect(Collectors.toMap(
                        shift -> AttendanceMigrationMapper.shiftKey(shift.getMysqlId(), shift.getMysqlBranchId()),
                        shift -> shift,
                        (left, right) -> left));

        List<BranchShiftWeekdayEntity> weekdays = new ArrayList<>();
        List<BranchShiftWeekendEntity> weekends = new ArrayList<>();
        Set<String> pendingWeekdayKeys = new HashSet<>();
        Set<String> pendingWeekendKeys = new HashSet<>();

        for (AttShift shift : batch) {
            if (shift.getCompany() == null) {
                continue;
            }
            List<BranchEntity> branches = branchesByCompany.get(shift.getCompany().getId());
            if (branches == null || branches.isEmpty()) {
                continue;
            }
            List<AttShiftDetails> details = detailsByShift.getOrDefault(shift.getId(), List.of());
            for (AttShiftDetails detail : details) {
                AttTimeTable timeTable = detail.getAttTimeTable();
                if (timeTable == null) {
                    log.warn("Skipping attShiftDetails id={}, missing attTimeTable", detail.getId());
                    continue;
                }
                DayOfWeek dayOfWeek = AttendanceMigrationMapper.legacyDayOfWeek(detail.getDays());
                LocalTime start = timeTable.getOnTime() != null
                        ? timeTable.getOnTime().toLocalTime()
                        : null;
                LocalTime end = timeTable.getOffTime() != null
                        ? timeTable.getOffTime().toLocalTime()
                        : null;

                for (BranchEntity branch : branches) {
                    String shiftKey =
                            AttendanceMigrationMapper.shiftKey(timeTable.getId(), branch.getMysqlId());
                    BranchShiftEntity branchShift = shiftByKey.get(shiftKey);
                    if (branchShift == null) {
                        continue;
                    }
                    UUID branchShiftId = branchShift.getId();
                    if (branchShiftId == null) {
                        continue;
                    }

                    String weekdayKey = branchShiftId + ":" + dayOfWeek;
                    if (!pendingWeekdayKeys.contains(weekdayKey)) {
                        weekdays.add(BranchShiftWeekdayEntity.builder()
                                .branchShiftId(branchShiftId)
                                .dayOfWeek(dayOfWeek)
                                .off(Boolean.TRUE.equals(detail.getIsOffDay()))
                                .startTime(start)
                                .endTime(end)
                                .build());
                        pendingWeekdayKeys.add(weekdayKey);
                    }

                    if (Boolean.TRUE.equals(detail.getIsOffDay())
                            && (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY)) {
                        String weekendKey = branchShiftId + ":" + dayOfWeek;
                        if (!pendingWeekendKeys.contains(weekendKey)) {
                            weekends.add(BranchShiftWeekendEntity.builder()
                                    .branchShiftId(branchShiftId)
                                    .dayOfWeek(dayOfWeek)
                                    .build());
                            pendingWeekendKeys.add(weekendKey);
                        }
                    }
                }
            }
        }

        int imported = 0;
        Set<UUID> shiftIdsForLookup = weekdays.stream()
                .map(BranchShiftWeekdayEntity::getBranchShiftId)
                .collect(Collectors.toSet());
        shiftIdsForLookup.addAll(weekends.stream()
                .map(BranchShiftWeekendEntity::getBranchShiftId)
                .collect(Collectors.toSet()));

        if (!weekdays.isEmpty()) {
            Set<String> existingWeekdayKeys = branchShiftWeekdayRepository.findExistingKeys(shiftIdsForLookup).stream()
                    .map(row -> row[0] + ":" + row[1])
                    .collect(Collectors.toSet());
            List<BranchShiftWeekdayEntity> newWeekdays = weekdays.stream()
                    .filter(row -> !existingWeekdayKeys.contains(row.getBranchShiftId() + ":" + row.getDayOfWeek()))
                    .toList();
            if (!newWeekdays.isEmpty()) {
                branchShiftWeekdayRepository.saveAll(newWeekdays);
                imported += newWeekdays.size();
            }
        }

        if (!weekends.isEmpty()) {
            Set<String> existingWeekendKeys = branchShiftWeekendRepository.findExistingKeys(shiftIdsForLookup).stream()
                    .map(row -> row[0] + ":" + row[1])
                    .collect(Collectors.toSet());
            List<BranchShiftWeekendEntity> newWeekends = weekends.stream()
                    .filter(row -> !existingWeekendKeys.contains(row.getBranchShiftId() + ":" + row.getDayOfWeek()))
                    .toList();
            if (!newWeekends.isEmpty()) {
                branchShiftWeekendRepository.saveAll(newWeekends);
                imported += newWeekends.size();
            }
        }

        exchange.setProperty("batchImported", imported);
    }
}
