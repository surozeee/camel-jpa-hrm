package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.AutoLeaveAccParams;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.BranchLeaveAccumulationRuleEntity;
import com.jojolaptech.camel.model.postgres.company.BranchLeaveTypeEntity;
import com.jojolaptech.camel.processor.AutoLeaveAccMigrationMapper.AccValues;
import com.jojolaptech.camel.processor.AutoLeaveAccMigrationMapper.ConfigBundleKey;
import com.jojolaptech.camel.repository.mysql.AutoLeaveAccParamsRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchLeaveAccumulationRuleRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchLeaveTypeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import java.util.ArrayList;
import java.util.Date;
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
public class LeaveAccumulationRuleProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(LeaveAccumulationRuleProcessor.class);

    private final AutoLeaveAccParamsRepository autoLeaveAccParamsRepository;
    private final PgBranchRepository branchRepository;
    private final PgBranchLeaveTypeRepository branchLeaveTypeRepository;
    private final PgBranchLeaveAccumulationRuleRepository accumulationRuleRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Date> paramDates = exchange.getMessage().getBody(List.class);
        if (paramDates == null || paramDates.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        List<AutoLeaveAccParams> rows = autoLeaveAccParamsRepository.findActiveByParamDateIn(paramDates);
        if (rows.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Map<String, List<AutoLeaveAccParams>> rowsByCompanyAndDate = rows.stream()
                .filter(row -> row.getCompany() != null && row.getParamDate() != null)
                .collect(Collectors.groupingBy(row -> row.getCompany().getId() + ":" + row.getParamDate().getTime()));

        Map<ConfigBundleKey, List<AutoLeaveAccParams>> bundles = rowsByCompanyAndDate.values().stream()
                .collect(Collectors.toMap(ConfigBundleKey::from, group -> group, (left, right) -> left));

        Set<Long> companyMysqlIds = bundles.keySet().stream()
                .map(ConfigBundleKey::companyMysqlId)
                .collect(Collectors.toSet());
        Map<Long, List<BranchEntity>> branchesByCompany =
                branchRepository.findByCompanyMysqlIdIn(companyMysqlIds).stream()
                        .collect(Collectors.groupingBy(branch -> branch.getCompany().getMysqlId()));

        Set<Long> leaveMysqlIds = bundles.values().stream()
                .map(AutoLeaveAccMigrationMapper::resolveLeaveMysqlId)
                .filter(id -> id > 0)
                .collect(Collectors.toSet());
        Set<Long> branchMysqlIds = branchesByCompany.values().stream()
                .flatMap(List::stream)
                .map(BranchEntity::getMysqlId)
                .collect(Collectors.toSet());
        Map<String, BranchLeaveTypeEntity> branchLeaveTypeByKey =
                branchLeaveTypeRepository
                        .findByMysqlLeaveIdInAndMysqlBranchIdIn(leaveMysqlIds, branchMysqlIds)
                        .stream()
                        .collect(Collectors.toMap(
                                row -> row.getMysqlLeaveId() + ":" + row.getMysqlBranchId(),
                                row -> row,
                                (left, right) -> left));

        Set<Long> ruleMysqlIds = new HashSet<>();
        for (Map.Entry<ConfigBundleKey, List<AutoLeaveAccParams>> entry : bundles.entrySet()) {
            long bundleId = AutoLeaveAccMigrationMapper.bundleMysqlId(entry.getValue());
            List<BranchEntity> branches =
                    branchesByCompany.getOrDefault(entry.getKey().companyMysqlId(), List.of());
            for (BranchEntity branch : branches) {
                ruleMysqlIds.add(AutoLeaveAccMigrationMapper.ruleMysqlId(bundleId, branch.getMysqlId()));
            }
        }
        Set<Long> existingRuleMysqlIds = accumulationRuleRepository.findMysqlIdsByMysqlIdIn(ruleMysqlIds);

        Set<UUID> branchLeaveTypeIds = branchLeaveTypeByKey.values().stream()
                .map(BranchLeaveTypeEntity::getId)
                .collect(Collectors.toSet());
        Set<UUID> existingBranchLeaveTypeIds =
                accumulationRuleRepository.findExistingBranchLeaveTypeIds(branchLeaveTypeIds);

        List<BranchLeaveAccumulationRuleEntity> toSave = new ArrayList<>();
        for (Map.Entry<ConfigBundleKey, List<AutoLeaveAccParams>> entry : bundles.entrySet()) {
            List<AutoLeaveAccParams> params = entry.getValue();
            long leaveMysqlId = AutoLeaveAccMigrationMapper.resolveLeaveMysqlId(params);
            if (leaveMysqlId <= 0) {
                log.warn(
                        "Skipping autoLeaveAccParams bundle company mysqlId={}, paramDate={}, missing leave id",
                        entry.getKey().companyMysqlId(),
                        new Date(entry.getKey().paramDateEpochMs()));
                continue;
            }

            AccValues values = AutoLeaveAccMigrationMapper.fromParams(params);
            if (Boolean.FALSE.equals(values.getAccumulationEnabled())) {
                continue;
            }

            long bundleId = AutoLeaveAccMigrationMapper.bundleMysqlId(params);
            List<BranchEntity> branches =
                    branchesByCompany.getOrDefault(entry.getKey().companyMysqlId(), List.of());
            if (branches.isEmpty()) {
                log.warn(
                        "Skipping autoLeaveAccParams bundle company mysqlId={}, leave mysqlId={}, no branches",
                        entry.getKey().companyMysqlId(),
                        leaveMysqlId);
                continue;
            }

            String remarks = buildRemarks(params, values);
            for (BranchEntity branch : branches) {
                BranchLeaveTypeEntity branchLeaveType =
                        branchLeaveTypeByKey.get(leaveMysqlId + ":" + branch.getMysqlId());
                if (branchLeaveType == null) {
                    continue;
                }
                if (existingBranchLeaveTypeIds.contains(branchLeaveType.getId())) {
                    continue;
                }
                long mysqlId = AutoLeaveAccMigrationMapper.ruleMysqlId(bundleId, branch.getMysqlId());
                if (existingRuleMysqlIds.contains(mysqlId)) {
                    continue;
                }

                toSave.add(BranchLeaveAccumulationRuleEntity.builder()
                        .mysqlId(mysqlId)
                        .branchLeaveType(branchLeaveType)
                        .accumulationEnabled(values.getAccumulationEnabled())
                        .unit(values.getUnit())
                        .accumulationPeriod(values.getAccumulationPeriod())
                        .leaveDays(values.getLeaveDays())
                        .requireConfirmation(values.getRequireConfirmation() != null
                                ? values.getRequireConfirmation()
                                : false)
                        .effectiveFrom(values.getEffectiveFrom())
                        .description(values.getDescription())
                        .remarks(remarks)
                        .build());
                existingRuleMysqlIds.add(mysqlId);
                existingBranchLeaveTypeIds.add(branchLeaveType.getId());
            }
        }

        if (!toSave.isEmpty()) {
            accumulationRuleRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }

    private static String buildRemarks(List<AutoLeaveAccParams> params, AccValues values) {
        StringBuilder remarks = new StringBuilder("Migrated from autoLeaveAccParams bundle minId=")
                .append(AutoLeaveAccMigrationMapper.bundleMysqlId(params));
        if (values.getCalendarType() != null) {
            remarks.append("; dateType=").append(values.getCalendarType());
        }
        if (values.getOpeningYear() != null) {
            remarks.append("; openingYear=").append(values.getOpeningYear());
        }
        if (values.getOpeningMonth() != null) {
            remarks.append("; openingMonth=").append(values.getOpeningMonth());
        }
        return remarks.toString();
    }
}
