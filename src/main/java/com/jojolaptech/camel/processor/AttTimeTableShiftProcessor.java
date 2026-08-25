package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.AttTimeTable;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.BranchShiftEntity;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchShiftRepository;
import java.util.ArrayList;
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
public class AttTimeTableShiftProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(AttTimeTableShiftProcessor.class);

    private final PgBranchRepository branchRepository;
    private final PgBranchShiftRepository branchShiftRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<AttTimeTable> batch = exchange.getMessage().getBody(List.class);
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

        Set<Long> timeTableIds = batch.stream().map(AttTimeTable::getId).collect(Collectors.toSet());
        Set<Long> branchMysqlIds = branchesByCompany.values().stream()
                .flatMap(List::stream)
                .map(BranchEntity::getMysqlId)
                .collect(Collectors.toSet());
        Set<String> existingKeys = branchShiftRepository.findExistingShiftKeys(timeTableIds, branchMysqlIds).stream()
                .map(row -> AttendanceMigrationMapper.shiftKey((Long) row[0], (Long) row[1]))
                .collect(Collectors.toSet());

        List<BranchShiftEntity> toSave = new ArrayList<>();
        for (AttTimeTable source : batch) {
            if (source.getCompany() == null) {
                log.warn("Skipping attTimeTable id={}, missing company", source.getId());
                continue;
            }
            List<BranchEntity> branches = branchesByCompany.get(source.getCompany().getId());
            if (branches == null || branches.isEmpty()) {
                log.warn(
                        "Skipping attTimeTable id={}, no migrated branches for company mysqlId={}",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }
            for (BranchEntity branch : branches) {
                String key = AttendanceMigrationMapper.shiftKey(source.getId(), branch.getMysqlId());
                if (existingKeys.contains(key)) {
                    continue;
                }
                toSave.add(AttendanceMigrationMapper.toBranchShift(source, branch));
                existingKeys.add(key);
            }
        }

        if (!toSave.isEmpty()) {
            branchShiftRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
