package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.postgres.company.DepartmentEntity;
import com.jojolaptech.camel.model.postgres.company.TeamEntity;
import com.jojolaptech.camel.repository.postgres.company.PgDepartmentRepository;
import com.jojolaptech.camel.repository.postgres.company.PgTeamRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Step 21c: Team shell per department (leader null).
 */
@Component
@RequiredArgsConstructor
public class TeamSeedProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(TeamSeedProcessor.class);

    private final PgDepartmentRepository departmentRepository;
    private final PgTeamRepository teamRepository;

    @Override
    public void process(Exchange exchange) {
        List<DepartmentEntity> departments = departmentRepository.findAll();
        if (departments.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Map<Long, TeamEntity> existingByMysqlId = teamRepository
                .findByMysqlIdIn(departments.stream()
                        .map(d -> OrgMigrationMapper.orgMasterMysqlId(d.getMysqlId(), d.getMysqlBranchId()))
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(TeamEntity::getMysqlId, t -> t, (a, b) -> a));

        List<TeamEntity> toSave = new ArrayList<>();
        for (DepartmentEntity dept : departments) {
            long mysqlId = OrgMigrationMapper.orgMasterMysqlId(dept.getMysqlId(), dept.getMysqlBranchId());
            TeamEntity team = existingByMysqlId.get(mysqlId);
            if (team == null) {
                team = TeamEntity.builder()
                        .mysqlId(mysqlId)
                        .code(truncate("TM-" + dept.getMysqlId() + "-" + dept.getMysqlBranchId(), 64))
                        .name(dept.getName())
                        .description("Derived from department (migrated)")
                        .companyId(dept.getCompanyId())
                        .branchId(dept.getBranchId())
                        .departmentId(dept.getId())
                        .leaderEmployeeId(null)
                        .build();
                existingByMysqlId.put(mysqlId, team);
            } else {
                team.setCode(truncate("TM-" + dept.getMysqlId() + "-" + dept.getMysqlBranchId(), 64));
                team.setName(dept.getName());
                team.setDescription("Derived from department (migrated)");
                team.setCompanyId(dept.getCompanyId());
                team.setBranchId(dept.getBranchId());
                team.setDepartmentId(dept.getId());
            }
            toSave.add(team);
        }

        if (!toSave.isEmpty()) {
            teamRepository.saveAll(toSave);
        }

        log.info("Team seed upserted {} of {} departments", toSave.size(), departments.size());
        exchange.setProperty("batchImported", toSave.size());
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
