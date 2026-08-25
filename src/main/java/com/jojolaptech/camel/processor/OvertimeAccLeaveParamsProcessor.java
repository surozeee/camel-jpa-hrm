package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.OvertimeAccLeaveParams;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.LeavePolicyEntity;
import com.jojolaptech.camel.repository.mysql.OvertimeAccLeaveParamsRepository;
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
import lombok.Getter;
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

        Map<String, List<OvertimeAccLeaveParams>> bundles = rows.stream()
                .filter(row -> row.getCompany() != null && row.getParamDate() != null)
                .collect(Collectors.groupingBy(row -> row.getCompany().getId() + ":" + row.getParamDate().getTime()));

        Set<Long> companyMysqlIds = bundles.values().stream()
                .map(list -> list.getFirst().getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companies = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, company -> company, (left, right) -> left));

        Map<Long, List<com.jojolaptech.camel.model.postgres.company.BranchEntity>> branchesByCompany =
                branchRepository.findByCompanyMysqlIdIn(companyMysqlIds).stream()
                        .collect(Collectors.groupingBy(branch -> branch.getCompany().getMysqlId()));

        Set<Long> branchMysqlIds = branchesByCompany.values().stream()
                .flatMap(List::stream)
                .map(com.jojolaptech.camel.model.postgres.company.BranchEntity::getMysqlId)
                .collect(Collectors.toSet());
        Map<Long, LeavePolicyEntity> policyByBranchMysqlId =
                leavePolicyRepository.findByMysqlBranchIdIn(branchMysqlIds).stream()
                        .collect(Collectors.toMap(
                                LeavePolicyEntity::getMysqlBranchId, policy -> policy, (left, right) -> left));

        int imported = 0;
        List<CompanyEntity> companiesToUpdate = new ArrayList<>();
        List<LeavePolicyEntity> policiesToUpdate = new ArrayList<>();
        Set<Long> touchedCompanies = new HashSet<>();

        for (List<OvertimeAccLeaveParams> params : bundles.values()) {
            OvertimeAccLeaveParams sample = params.getFirst();
            Long companyMysqlId = sample.getCompany().getId();
            CompanyEntity company = companies.get(companyMysqlId);
            if (company == null) {
                continue;
            }
            OtLeaveValues values = OtLeaveValues.fromParams(params);
            if (!touchedCompanies.contains(companyMysqlId)) {
                company.setEnableTimeOvertime(true);
                companiesToUpdate.add(company);
                touchedCompanies.add(companyMysqlId);
                imported++;
            }

            String remarkSuffix = values.toRemarks();
            for (com.jojolaptech.camel.model.postgres.company.BranchEntity branch :
                    branchesByCompany.getOrDefault(companyMysqlId, List.of())) {
                LeavePolicyEntity policy = policyByBranchMysqlId.get(branch.getMysqlId());
                if (policy == null) {
                    continue;
                }
                if (values.isEnabled()) {
                    policy.setEnableAutomaticAccrual(true);
                    policy.setEnableLeaveAccumulation(true);
                }
                policy.setRemarks(appendRemarks(policy.getRemarks(), remarkSuffix));
                policiesToUpdate.add(policy);
                imported++;
            }
        }

        if (!companiesToUpdate.isEmpty()) {
            companyRepository.saveAll(companiesToUpdate.stream().distinct().toList());
        }
        if (!policiesToUpdate.isEmpty()) {
            leavePolicyRepository.saveAll(policiesToUpdate.stream().distinct().toList());
        }
        exchange.setProperty("batchImported", imported);
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

    @Getter
    private static final class OtLeaveValues {
        private boolean enabled = true;
        private Long leaveMysqlId;
        private String daysToAdd;
        private String minutesToAdd;

        static OtLeaveValues fromParams(List<OvertimeAccLeaveParams> params) {
            OtLeaveValues values = new OtLeaveValues();
            for (OvertimeAccLeaveParams param : params) {
                if (param.getParamName() == null || param.getParamValue() == null) {
                    continue;
                }
                switch (param.getParamName().trim()) {
                    case "LeaveId" -> values.leaveMysqlId = parseLong(param.getParamValue());
                    case "DaysToAdd" -> values.daysToAdd = param.getParamValue().trim();
                    case "MinutesToAdd" -> values.minutesToAdd = param.getParamValue().trim();
                    case "isEditable", "enabled", "isActive" ->
                            values.enabled = !"false".equalsIgnoreCase(param.getParamValue().trim());
                    default -> {
                        // ignore unknown params
                    }
                }
            }
            return values;
        }

        String toRemarks() {
            StringBuilder remarks = new StringBuilder("OT leave acc from overtimeAccLeaveParams");
            if (leaveMysqlId != null) {
                remarks.append("; leaveId=").append(leaveMysqlId);
            }
            if (daysToAdd != null) {
                remarks.append("; daysToAdd=").append(daysToAdd);
            }
            if (minutesToAdd != null) {
                remarks.append("; minutesToAdd=").append(minutesToAdd);
            }
            return remarks.toString();
        }

        private static Long parseLong(String value) {
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }
}
