package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyEmployee;
import com.jojolaptech.camel.model.mysql.EmployeeSkill;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.SkillEntity;
import com.jojolaptech.camel.repository.mysql.CompanyEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgSkillRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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

/**
 * Step 22t: upsert distinct employeeSkill.skill names into hrm_skill per company.
 */
@Component
@RequiredArgsConstructor
public class SkillMasterProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(SkillMasterProcessor.class);

    private final CompanyEmployeeRepository companyEmployeeRepository;
    private final PgCompanyRepository companyRepository;
    private final PgSkillRepository skillRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeeSkill> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, Long> companyMysqlIdByEmployee = new HashMap<>();
        for (CompanyEmployee ce : companyEmployeeRepository.findByEmployeeIdIn(employeeMysqlIds)) {
            if (ce.getEmployee() == null || ce.getCompany() == null) {
                continue;
            }
            companyMysqlIdByEmployee.putIfAbsent(ce.getEmployee().getId(), ce.getCompany().getId());
        }

        Set<Long> companyMysqlIds = new HashSet<>(companyMysqlIdByEmployee.values());
        Map<Long, CompanyEntity> companyByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, c -> c, (a, b) -> a));

        Set<UUID> companyIds = companyByMysqlId.values().stream().map(CompanyEntity::getId).collect(Collectors.toSet());
        Map<String, SkillEntity> existingByKey = new HashMap<>();
        if (!companyIds.isEmpty()) {
            for (SkillEntity skill : skillRepository.findByCompanyIdIn(companyIds)) {
                existingByKey.put(skillKey(skill.getCompanyId(), skill.getName()), skill);
            }
        }

        List<SkillEntity> toSave = new ArrayList<>();
        Set<String> pendingKeys = new HashSet<>();
        for (EmployeeSkill source : batch) {
            String skillName = OrgMigrationMapper.trimToNull(source.getSkill());
            if (skillName == null || source.getEmployee() == null) {
                log.warn("Skipping skill master from employeeSkill id={}, missing skill/employee", source.getId());
                continue;
            }
            Long companyMysqlId = companyMysqlIdByEmployee.get(source.getEmployee().getId());
            if (companyMysqlId == null) {
                log.warn(
                        "Skipping skill master from employeeSkill id={}, no companyEmployee",
                        source.getId());
                continue;
            }
            CompanyEntity company = companyByMysqlId.get(companyMysqlId);
            if (company == null) {
                log.warn(
                        "Skipping skill master from employeeSkill id={}, company mysqlId={} not migrated",
                        source.getId(),
                        companyMysqlId);
                continue;
            }
            String key = skillKey(company.getId(), skillName);
            if (existingByKey.containsKey(key) || pendingKeys.contains(key)) {
                continue;
            }
            SkillEntity created = EmployeeProfileMigrationMapper.newSkill(company.getId(), skillName, source.getId());
            if (created == null) {
                continue;
            }
            toSave.add(created);
            pendingKeys.add(key);
            existingByKey.put(key, created);
        }

        if (!toSave.isEmpty()) {
            skillRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }

    private static String skillKey(UUID companyId, String name) {
        return companyId + "|" + name.trim().toLowerCase(Locale.ROOT);
    }
}
