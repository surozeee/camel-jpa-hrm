package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyPayrollHeading;
import com.jojolaptech.camel.model.mysql.PayrollHeading;
import com.jojolaptech.camel.model.mysql.PayrollSystemHeading;
import com.jojolaptech.camel.model.mysql.enums.PayrollHeadingType;
import com.jojolaptech.camel.model.postgres.company.BranchSalaryBreakdownEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.repository.postgres.company.PgBranchSalaryBreakdownRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
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

/**
 * Migrates company salary component structure:
 * companyPayrollHeading / pms payrollHeading → hrm_branch_salary_breakdown.
 */
@Component
@RequiredArgsConstructor
public class BranchSalaryBreakdownProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(BranchSalaryBreakdownProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgBranchSalaryBreakdownRepository breakdownRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        String sourceType = exchange.getProperty("payrollHeadingSource", String.class);
        if ("pms".equalsIgnoreCase(sourceType)) {
            int imported = processPmsHeadings(exchange.getMessage().getBody(List.class));
            exchange.setProperty("batchImported", imported);
            return;
        }
        int imported = processCompanyHeadings(exchange.getMessage().getBody(List.class));
        exchange.setProperty("batchImported", imported);
    }

    private int processCompanyHeadings(List<CompanyPayrollHeading> batch) {
        if (batch == null || batch.isEmpty()) {
            return 0;
        }

        Set<Long> mysqlIds = batch.stream().map(CompanyPayrollHeading::getId).collect(Collectors.toSet());
        Set<Long> existingIds = breakdownRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> companyMysqlIds = batch.stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, UUID> companyIdByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, CompanyEntity::getId, (a, b) -> a));

        List<BranchSalaryBreakdownEntity> toSave = new ArrayList<>();
        for (CompanyPayrollHeading source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (!PayrollHeadingMigrationMapper.isMigratableCompanyHeading(source)) {
                continue;
            }
            PayrollSystemHeading system = source.getPayrollHeading();
            if (system == null || system.getHeadingType() == PayrollHeadingType.PARENT) {
                continue;
            }
            if (source.getCompany() == null) {
                log.warn("Skipping companyPayrollHeading id={}, missing company", source.getId());
                continue;
            }
            UUID companyId = companyIdByMysqlId.get(source.getCompany().getId());
            if (companyId == null) {
                log.warn(
                        "Skipping companyPayrollHeading id={}, company mysqlId={} not migrated",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }

            toSave.add(PayrollHeadingMigrationMapper.fromCompanyHeading(source, companyId));
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            breakdownRepository.saveAll(toSave);
        }
        return toSave.size();
    }

    private int processPmsHeadings(List<PayrollHeading> batch) {
        if (batch == null || batch.isEmpty()) {
            return 0;
        }

        Set<Long> mysqlIds = batch.stream()
                .map(row -> PayrollHeadingMigrationMapper.pmsMysqlId(row.getId()))
                .collect(Collectors.toSet());
        Set<Long> existingIds = breakdownRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> companyMysqlIds = batch.stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, UUID> companyIdByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, CompanyEntity::getId, (a, b) -> a));

        List<BranchSalaryBreakdownEntity> toSave = new ArrayList<>();
        for (PayrollHeading source : batch) {
            long mysqlId = PayrollHeadingMigrationMapper.pmsMysqlId(source.getId());
            if (existingIds.contains(mysqlId)) {
                continue;
            }
            if (source.getCompany() == null) {
                log.warn("Skipping payrollHeading id={}, missing company", source.getId());
                continue;
            }
            UUID companyId = companyIdByMysqlId.get(source.getCompany().getId());
            if (companyId == null) {
                log.warn(
                        "Skipping payrollHeading id={}, company mysqlId={} not migrated",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }
            toSave.add(PayrollHeadingMigrationMapper.fromPmsHeading(source, companyId));
            existingIds.add(mysqlId);
        }

        if (!toSave.isEmpty()) {
            breakdownRepository.saveAll(toSave);
        }
        return toSave.size();
    }
}
