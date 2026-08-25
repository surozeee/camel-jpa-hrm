package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.AttHolidayDate;
import com.jojolaptech.camel.model.mysql.AttHolidayList;
import com.jojolaptech.camel.model.mysql.HolidayBranchDepartment;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.BranchHolidayEntity;
import com.jojolaptech.camel.repository.mysql.HolidayBranchDepartmentRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchHolidayRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import java.time.LocalDate;
import java.util.ArrayList;
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
public class BranchHolidayProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(BranchHolidayProcessor.class);

    private final HolidayBranchDepartmentRepository holidayBranchDepartmentRepository;
    private final PgBranchRepository branchRepository;
    private final PgBranchHolidayRepository branchHolidayRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<AttHolidayDate> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> holidayListIds = batch.stream()
                .map(AttHolidayDate::getAttHolidayList)
                .filter(java.util.Objects::nonNull)
                .map(AttHolidayList::getId)
                .collect(Collectors.toSet());
        Map<Long, Set<Long>> branchMysqlIdsByHolidayList =
                holidayBranchDepartmentRepository.findByAttHolidayListIdIn(holidayListIds).stream()
                        .collect(Collectors.groupingBy(
                                row -> row.getAttHolidayList().getId(),
                                Collectors.mapping(row -> row.getBranch().getId(), Collectors.toSet())));

        Set<Long> companyMysqlIds = batch.stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, List<BranchEntity>> branchesByCompany =
                branchRepository.findByCompanyMysqlIdIn(companyMysqlIds).stream()
                        .collect(Collectors.groupingBy(branch -> branch.getCompany().getMysqlId()));

        Set<Long> mysqlIds = batch.stream().map(AttHolidayDate::getId).collect(Collectors.toSet());
        Set<UUID> branchIds = branchesByCompany.values().stream()
                .flatMap(List::stream)
                .map(BranchEntity::getId)
                .collect(Collectors.toSet());
        Set<String> existingKeys = branchHolidayRepository.findExistingKeys(mysqlIds, branchIds).stream()
                .map(row -> row[0] + ":" + row[1])
                .collect(Collectors.toSet());

        List<BranchHolidayEntity> toSave = new ArrayList<>();
        for (AttHolidayDate source : batch) {
            if (source.getCompany() == null || source.getAttHolidayList() == null || source.getDayHoliday() == null) {
                log.warn("Skipping attHolidayDate id={}, missing company/list/date", source.getId());
                continue;
            }
            LocalDate holidayDate = AttendanceMigrationMapper.toLocalDate(source.getDayHoliday());
            if (holidayDate == null) {
                continue;
            }
            AttHolidayList holidayList = source.getAttHolidayList();
            if (Boolean.FALSE.equals(holidayList.getIsActive())) {
                continue;
            }
            String holidayName = FiscalMigrationMapper.trimToNull(holidayList.getHolidayName());
            if (holidayName == null) {
                holidayName = "Holiday";
            }

            Set<Long> scopedBranchMysqlIds =
                    branchMysqlIdsByHolidayList.getOrDefault(holidayList.getId(), Set.of());
            List<BranchEntity> targetBranches;
            if (scopedBranchMysqlIds.isEmpty()) {
                targetBranches = branchesByCompany.getOrDefault(source.getCompany().getId(), List.of());
            } else {
                targetBranches = branchesByCompany.getOrDefault(source.getCompany().getId(), List.of()).stream()
                        .filter(branch -> scopedBranchMysqlIds.contains(branch.getMysqlId()))
                        .toList();
            }

            String remarks = buildRemarks(holidayList);
            for (BranchEntity branch : targetBranches) {
                String key = source.getId() + ":" + branch.getId();
                if (existingKeys.contains(key)) {
                    continue;
                }
                toSave.add(BranchHolidayEntity.builder()
                        .mysqlId(source.getId())
                        .branchId(branch.getId())
                        .name(holidayName)
                        .holidayDate(holidayDate)
                        .description(holidayList.getApplicableFor())
                        .remarks(remarks)
                        .build());
                existingKeys.add(key);
            }
        }

        if (!toSave.isEmpty()) {
            branchHolidayRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }

    private static String buildRemarks(AttHolidayList holidayList) {
        StringBuilder remarks = new StringBuilder("Migrated from attHolidayList id=").append(holidayList.getId());
        if (holidayList.getApplyForMaleFemale() != null) {
            remarks.append("; gender=").append(holidayList.getApplyForMaleFemale());
        }
        return remarks.toString();
    }
}
