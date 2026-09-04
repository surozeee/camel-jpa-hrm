package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.postgres.company.CostCenterEntity;
import com.jojolaptech.camel.model.postgres.company.DepartmentEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.TeamEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCostCenterRepository;
import com.jojolaptech.camel.repository.postgres.company.PgDepartmentRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgTeamRepository;
import java.util.ArrayList;
import java.util.HashMap;
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

/**
 * Step 22zk: backfill employee.divisionId / teamId / costCenterId from department and branch.
 */
@Component
@RequiredArgsConstructor
public class EmployeeOrgFkBackfillProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeOrgFkBackfillProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgDepartmentRepository departmentRepository;
    private final PgTeamRepository teamRepository;
    private final PgCostCenterRepository costCenterRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeeEntity> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<UUID> departmentIds = batch.stream()
                .map(EmployeeEntity::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, DepartmentEntity> departmentById = departmentIds.isEmpty()
                ? Map.of()
                : departmentRepository.findAllById(departmentIds).stream()
                        .collect(Collectors.toMap(DepartmentEntity::getId, d -> d, (a, b) -> a));

        Map<UUID, TeamEntity> teamByDepartmentId = new HashMap<>();
        for (UUID departmentId : departmentIds) {
            teamRepository.findFirstByDepartmentId(departmentId).ifPresent(team ->
                    teamByDepartmentId.put(departmentId, team));
        }

        Set<UUID> branchIds = batch.stream()
                .map(EmployeeEntity::getBranchId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, CostCenterEntity> costCenterByBranchId = new HashMap<>();
        for (UUID branchId : branchIds) {
            costCenterRepository.findFirstByBranchId(branchId).ifPresent(cc ->
                    costCenterByBranchId.put(branchId, cc));
        }

        // Company-level fallback: resolve companyId via department when branch CC missing
        Map<UUID, CostCenterEntity> companyFallbackCostCenter = new HashMap<>();

        List<EmployeeEntity> toSave = new ArrayList<>();
        int updated = 0;
        for (EmployeeEntity employee : batch) {
            if (employee.getDepartmentId() == null) {
                continue;
            }
            DepartmentEntity department = departmentById.get(employee.getDepartmentId());
            if (department == null) {
                continue;
            }

            boolean changed = false;
            UUID divisionId = department.getDivisionId();
            if (divisionId != null && !Objects.equals(employee.getDivisionId(), divisionId)) {
                employee.setDivisionId(divisionId);
                changed = true;
            }

            TeamEntity team = teamByDepartmentId.get(employee.getDepartmentId());
            if (team != null && !Objects.equals(employee.getTeamId(), team.getId())) {
                employee.setTeamId(team.getId());
                changed = true;
            }

            UUID costCenterId = null;
            if (employee.getBranchId() != null) {
                CostCenterEntity byBranch = costCenterByBranchId.get(employee.getBranchId());
                if (byBranch != null) {
                    costCenterId = byBranch.getId();
                }
            }
            if (costCenterId == null && department.getCompanyId() != null) {
                CostCenterEntity fallback = companyFallbackCostCenter.computeIfAbsent(
                        department.getCompanyId(),
                        companyId -> costCenterRepository
                                .findFirstByCompanyIdAndBranchIdIsNull(companyId)
                                .or(() -> costCenterRepository.findByCompanyId(companyId).stream().findFirst())
                                .orElse(null));
                if (fallback != null) {
                    costCenterId = fallback.getId();
                }
            }
            if (costCenterId != null && !Objects.equals(employee.getCostCenterId(), costCenterId)) {
                employee.setCostCenterId(costCenterId);
                changed = true;
            }

            if (changed) {
                toSave.add(employee);
                updated++;
            }
        }

        if (!toSave.isEmpty()) {
            employeeRepository.saveAll(toSave);
        }

        log.info("Employee org-FK backfill updated {} of {} employees", updated, batch.size());
        exchange.setProperty("batchImported", updated);
    }
}
