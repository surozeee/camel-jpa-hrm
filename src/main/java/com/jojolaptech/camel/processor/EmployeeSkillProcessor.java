package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyEmployee;
import com.jojolaptech.camel.model.mysql.EmployeeSkill;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeSkillEntity;
import com.jojolaptech.camel.model.postgres.company.SkillEntity;
import com.jojolaptech.camel.repository.mysql.CompanyEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeSkillRepository;
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

@Component
@RequiredArgsConstructor
public class EmployeeSkillProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeSkillProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgEmployeeSkillRepository employeeSkillRepository;
    private final PgSkillRepository skillRepository;
    private final CompanyEmployeeRepository companyEmployeeRepository;
    private final PgCompanyRepository companyRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeeSkill> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(EmployeeSkill::getId).collect(Collectors.toSet());
        Set<Long> existingIds = employeeSkillRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, row -> row, (left, right) -> left));

        Map<Long, Long> companyMysqlIdByEmployee = new HashMap<>();
        for (CompanyEmployee ce : companyEmployeeRepository.findByEmployeeIdIn(employeeMysqlIds)) {
            if (ce.getEmployee() == null || ce.getCompany() == null) {
                continue;
            }
            companyMysqlIdByEmployee.putIfAbsent(ce.getEmployee().getId(), ce.getCompany().getId());
        }
        Map<Long, CompanyEntity> companyByMysqlId =
                companyRepository.findByMysqlIdIn(new HashSet<>(companyMysqlIdByEmployee.values())).stream()
                        .collect(Collectors.toMap(CompanyEntity::getMysqlId, c -> c, (a, b) -> a));

        Set<UUID> companyIds = companyByMysqlId.values().stream().map(CompanyEntity::getId).collect(Collectors.toSet());
        Map<String, UUID> skillIdByKey = new HashMap<>();
        if (!companyIds.isEmpty()) {
            for (SkillEntity skill : skillRepository.findByCompanyIdIn(companyIds)) {
                skillIdByKey.put(skillKey(skill.getCompanyId(), skill.getName()), skill.getId());
            }
        }

        List<EmployeeSkillEntity> toSave = new ArrayList<>();
        for (EmployeeSkill source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployee() == null) {
                log.warn("Skipping employeeSkill id={}, missing employee", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn("Skipping employeeSkill id={}, employee not migrated", source.getId());
                continue;
            }
            String skillName = OrgMigrationMapper.trimToNull(source.getSkill());
            if (skillName == null) {
                log.warn("Skipping employeeSkill id={}, missing skill name", source.getId());
                continue;
            }
            Long companyMysqlId = companyMysqlIdByEmployee.get(source.getEmployee().getId());
            CompanyEntity company = companyMysqlId != null ? companyByMysqlId.get(companyMysqlId) : null;
            if (company == null) {
                log.warn("Skipping employeeSkill id={}, company not resolved", source.getId());
                continue;
            }
            UUID skillId = skillIdByKey.get(skillKey(company.getId(), skillName));
            if (skillId == null) {
                log.warn(
                        "Skipping employeeSkill id={}, skill master missing for company={} name={}",
                        source.getId(),
                        company.getId(),
                        skillName);
                continue;
            }
            EmployeeSkillEntity mapped =
                    EmployeeProfileMigrationMapper.fromEmployeeSkill(source, employee.getId(), skillId);
            if (mapped == null) {
                continue;
            }
            toSave.add(mapped);
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            employeeSkillRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }

    private static String skillKey(UUID companyId, String name) {
        return companyId + "|" + name.trim().toLowerCase(Locale.ROOT);
    }
}
