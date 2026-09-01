package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.OvertimeAccLeaveParams;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.BranchLeaveAccumulationRuleEntity;
import com.jojolaptech.camel.model.postgres.company.BranchLeaveTypeEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.LeavePolicyEntity;
import com.jojolaptech.camel.processor.OvertimeAccLeaveMigrationMapper.OtAccValues;
import com.jojolaptech.camel.processor.OvertimeAccLeaveMigrationMapper.OtBundleKey;
import com.jojolaptech.camel.repository.mysql.OvertimeAccLeaveParamsRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchLeaveAccumulationRuleRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchLeaveTypeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLeavePolicyRepository;
import java.util.ArrayList;
import java.util.Date;
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
public class OvertimeAccLeaveParamsProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(OvertimeAccLeaveParamsProcessor.class);

    private final OvertimeAccLeaveParamsRepository overtimeAccLeaveParamsRepository;
    private final PgCompanyRepository companyRepository;
    private final PgBranchRepository branchRepository;
    private final PgLeavePolicyRepository leavePolicyRepository;
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

        List<OvertimeAccLeaveParams> rows = overtimeAccLeaveParamsRepository.findByParamDateIn(paramDates);
        if (rows.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Map<String, List<OvertimeAccLeaveParams>> rowsByCompanyAndDate = rows.stream()
                .filter(row -> row.getCompany() != null && row.getParamDate() != null)
                .collect(Collectors.groupingBy(row -> row.getCompany().getId() + ":" + row.getParamDate().getTime()));

        Map<OtBundleKey, List<OvertimeAccLeaveParams>> bundles = rowsByCompanyAndDate.values().stream()
                .collect(Collectors.toMap(OtBundleKey::from, group -> group, (left, right) -> left));

        Set<Long> companyMysqlIds = bundles.keySet().stream()
                .map(OtBundleKey::companyMysqlId)
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companies = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, company -> company, (left, right) -> left));

        Map<Long, List<BranchEntity>> branchesByCompany =
                branchRepository.findByCompanyMysqlIdIn(companyMysqlIds).stream()
                        .collect(Collectors.groupingBy(branch -> branch.getCompany().getMysqlId()));

        Set<Long> leaveMysqlIds = bundles.values().stream()
                .map(OvertimeAccLeaveMigrationMapper::fromParams)
                .map(OtAccValues::getLeaveMysqlId)
                .filter(id -> id != null && id > 0)
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

        Map<Long, LeavePolicyEntity> policyByBranchMysqlId =
                leavePolicyRepository.findByMysqlBranchIdIn(branchMysqlIds).stream()
                        .collect(Collectors.toMap(
                                LeavePolicyEntity::getMysqlBranchId, policy -> policy, (left, right) -> left));

        Set<Long> ruleMysqlIds = new HashSet<>();
        for (Map.Entry<OtBundleKey, List<OvertimeAccLeaveParams>> entry : bundles.entrySet()) {
            long bundleId = OvertimeAccLeaveMigrationMapper.bundleMysqlId(entry.getValue());
            List<BranchEntity> branches =
                    branchesByCompany.getOrDefault(entry.getKey().companyMysqlId(), List.of());
            for (BranchEntity branch : branches) {
                ruleMysqlIds.add(OvertimeAccLeaveMigrationMapper.ruleMysqlId(bundleId, branch.getMysqlId()));
            }
        }
        Set<Long> existingRuleMysqlIds = accumulationRuleRepository.findMysqlIdsByMysqlIdIn(ruleMysqlIds);

        int imported = 0;
        List<CompanyEntity> companiesToUpdate = new ArrayList<>();
        List<LeavePolicyEntity> policiesToUpdate = new ArrayList<>();
        List<BranchLeaveAccumulationRuleEntity> rulesToSave = new ArrayList<>();
        Set<Long> touchedCompanies = new HashSet<>();

        for (Map.Entry<OtBundleKey, List<OvertimeAccLeaveParams>> entry : bundles.entrySet()) {
            List<OvertimeAccLeaveParams> params = entry.getValue();
            OtAccValues values = OvertimeAccLeaveMigrationMapper.fromParams(params);
            Long companyMysqlId = entry.getKey().companyMysqlId();
            CompanyEntity company = companies.get(companyMysqlId);
            if (company == null) {
                continue;
            }

            if (!touchedCompanies.contains(companyMysqlId)) {
                company.setEnableTimeOvertime(true);
                companiesToUpdate.add(company);
                touchedCompanies.add(companyMysqlId);
                imported++;
            }

            String remarkSuffix = buildPolicyRemarks(params, values);
            for (BranchEntity branch : branchesByCompany.getOrDefault(companyMysqlId, List.of())) {
                LeavePolicyEntity policy = policyByBranchMysqlId.get(branch.getMysqlId());
                if (policy != null) {
                    if (values.isEnabled()) {
                        policy.setEnableAutomaticAccrual(true);
                        policy.setEnableLeaveAccumulation(true);
                    }
                    policy.setRemarks(appendRemarks(policy.getRemarks(), remarkSuffix));
                    policiesToUpdate.add(policy);
                    imported++;
                }
            }

            if (!values.isEnabled()) {
                continue;
            }
            Long leaveMysqlId = values.getLeaveMysqlId();
            if (leaveMysqlId == null || leaveMysqlId <= 0) {
                log.warn(
                        "Skipping overtimeAccLeaveParams bundle company mysqlId={}, paramDate={}, missing leave id",
                        companyMysqlId,
                        new Date(entry.getKey().paramDateEpochMs()));
                continue;
            }

            long bundleId = OvertimeAccLeaveMigrationMapper.bundleMysqlId(params);
            List<BranchEntity> branches = branchesByCompany.getOrDefault(companyMysqlId, List.of());
            if (branches.isEmpty()) {
                log.warn(
                        "Skipping overtimeAccLeaveParams bundle company mysqlId={}, leave mysqlId={}, no branches",
                        companyMysqlId,
                        leaveMysqlId);
                continue;
            }

            String ruleRemarks = buildRuleRemarks(params, values);
            for (BranchEntity branch : branches) {
                BranchLeaveTypeEntity branchLeaveType =
                        branchLeaveTypeByKey.get(leaveMysqlId + ":" + branch.getMysqlId());
                if (branchLeaveType == null) {
                    continue;
                }
                long mysqlId = OvertimeAccLeaveMigrationMapper.ruleMysqlId(bundleId, branch.getMysqlId());
                if (existingRuleMysqlIds.contains(mysqlId)) {
                    continue;
                }

                rulesToSave.add(BranchLeaveAccumulationRuleEntity.builder()
                        .mysqlId(mysqlId)
                        .branchLeaveType(branchLeaveType)
                        .accumulationEnabled(true)
                        .unit(values.getUnit())
                        .accumulationPeriod(values.getAccumulationPeriod())
                        .leaveDays(values.getLeaveDays())
                        .requireConfirmation(values.getRequireConfirmation() != null
                                ? values.getRequireConfirmation()
                                : false)
                        .effectiveFrom(values.getEffectiveFrom())
                        .description("OT leave accumulation from overtimeAccLeaveParams")
                        .remarks(ruleRemarks)
                        .build());
                existingRuleMysqlIds.add(mysqlId);
                imported++;
            }
        }

        if (!companiesToUpdate.isEmpty()) {
            companyRepository.saveAll(companiesToUpdate.stream().distinct().toList());
        }
        if (!policiesToUpdate.isEmpty()) {
            leavePolicyRepository.saveAll(policiesToUpdate.stream().distinct().toList());
        }
        if (!rulesToSave.isEmpty()) {
            accumulationRuleRepository.saveAll(rulesToSave);
        }
        exchange.setProperty("batchImported", imported);
    }

    private static String buildPolicyRemarks(List<OvertimeAccLeaveParams> params, OtAccValues values) {
        StringBuilder remarks = new StringBuilder("OT leave acc from overtimeAccLeaveParams bundle minId=")
                .append(OvertimeAccLeaveMigrationMapper.bundleMysqlId(params));
        if (values.getLeaveMysqlId() != null) {
            remarks.append("; leaveId=").append(values.getLeaveMysqlId());
        }
        if (values.getUnit() != null) {
            remarks.append("; unit=").append(values.getUnit());
        }
        remarks.append("; leaveDays=").append(values.getLeaveDays());
        return remarks.toString();
    }

    private static String buildRuleRemarks(List<OvertimeAccLeaveParams> params, OtAccValues values) {
        return buildPolicyRemarks(params, values);
    }

    private static String appendRemarks(String existing, String addition) {
        if (addition == null || addition.isBlank()) {
            return existing;
        }
        if (existing == null || existing.isBlank()) {
            return addition;
        }
        if (existing.contains(addition)) {
            return existing;
        }
        return existing + "; " + addition;
    }
}
