package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.OpeningPayrollBalance;
import com.jojolaptech.camel.model.postgres.company.BranchSalaryBreakdownEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyFiscalYearEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.PayrollOpeningBalanceEntity;
import com.jojolaptech.camel.model.postgres.company.PayrollOpeningBalanceLineEntity;
import com.jojolaptech.camel.model.postgres.company.enums.PayrollOpeningBalanceTypeEnum;
import com.jojolaptech.camel.repository.postgres.company.PgBranchSalaryBreakdownRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyFiscalYearRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgPayrollOpeningBalanceLineRepository;
import com.jojolaptech.camel.repository.postgres.company.PgPayrollOpeningBalanceRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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

/**
 * Migrates openingPayrollBalance → hrm_payroll_opening_balance (+ lines).
 * Skips historical PayrollTransaction / payment runs.
 */
@Component
@RequiredArgsConstructor
public class PayrollOpeningBalanceProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(PayrollOpeningBalanceProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgCompanyFiscalYearRepository companyFiscalYearRepository;
    private final PgEmployeeRepository employeeRepository;
    private final PgBranchSalaryBreakdownRepository breakdownRepository;
    private final PgPayrollOpeningBalanceRepository openingBalanceRepository;
    private final PgPayrollOpeningBalanceLineRepository openingBalanceLineRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<OpeningPayrollBalance> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> lineMysqlIds = batch.stream().map(OpeningPayrollBalance::getId).collect(Collectors.toSet());
        Set<Long> existingLineIds = openingBalanceLineRepository.findMysqlIdsByMysqlIdIn(lineMysqlIds);

        Set<Long> companyMysqlIds = batch.stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, UUID> companyIdByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, CompanyEntity::getId, (a, b) -> a));

        Set<Long> fyMysqlIds = batch.stream()
                .filter(row -> row.getFiscalYear() != null)
                .map(row -> row.getFiscalYear().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyFiscalYearEntity> fyByMysqlId =
                companyFiscalYearRepository.findByMysqlIdIn(fyMysqlIds).stream()
                        .collect(Collectors.toMap(
                                CompanyFiscalYearEntity::getMysqlId, row -> row, (a, b) -> a));

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, row -> row, (a, b) -> a));

        Set<Long> pmsHeadingMysqlIds = batch.stream()
                .filter(row -> row.getPayrollheading() != null)
                .map(row -> PayrollHeadingMigrationMapper.pmsMysqlId(row.getPayrollheading().getId()))
                .collect(Collectors.toSet());
        Map<Long, BranchSalaryBreakdownEntity> breakdownByPmsMysqlId =
                breakdownRepository.findByMysqlIdIn(pmsHeadingMysqlIds).stream()
                        .collect(Collectors.toMap(
                                BranchSalaryBreakdownEntity::getMysqlId, row -> row, (a, b) -> a));

        Map<Long, PayrollOpeningBalanceEntity> headerByFyMysqlId = new HashMap<>();
        Set<Long> headersLoaded = new HashSet<>();
        for (Long fyMysqlId : fyMysqlIds) {
            openingBalanceRepository.findByMysqlId(fyMysqlId).ifPresent(header -> {
                headerByFyMysqlId.put(fyMysqlId, header);
                headersLoaded.add(fyMysqlId);
            });
        }

        List<PayrollOpeningBalanceEntity> headersToSave = new ArrayList<>();
        List<PayrollOpeningBalanceLineEntity> linesToSave = new ArrayList<>();

        for (OpeningPayrollBalance source : batch) {
            if (existingLineIds.contains(source.getId())) {
                continue;
            }
            if (source.getCompany() == null
                    || source.getFiscalYear() == null
                    || source.getEmployee() == null
                    || source.getPayrollheading() == null) {
                log.warn("Skipping openingPayrollBalance id={}, missing FK", source.getId());
                continue;
            }

            UUID companyId = companyIdByMysqlId.get(source.getCompany().getId());
            CompanyFiscalYearEntity companyFy = fyByMysqlId.get(source.getFiscalYear().getId());
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (companyId == null || companyFy == null || employee == null) {
                log.warn(
                        "Skipping openingPayrollBalance id={}, company/fy/employee not migrated",
                        source.getId());
                continue;
            }

            Long fyMysqlId = source.getFiscalYear().getId();
            PayrollOpeningBalanceEntity header = headerByFyMysqlId.get(fyMysqlId);
            if (header == null) {
                header = PayrollOpeningBalanceEntity.builder()
                        .mysqlId(fyMysqlId)
                        .companyId(companyId)
                        .companyFiscalYearId(companyFy.getId())
                        .monthsAlreadyProcessed(0)
                        .cutoverDate(LocalDate.now())
                        .remarks("migrated from openingPayrollBalance")
                        .locked(false)
                        .build();
                headerByFyMysqlId.put(fyMysqlId, header);
                headersToSave.add(header);
            } else if (!headersLoaded.contains(fyMysqlId) && !headersToSave.contains(header)) {
                // already queued
            }

            long pmsMysqlId = PayrollHeadingMigrationMapper.pmsMysqlId(source.getPayrollheading().getId());
            BranchSalaryBreakdownEntity breakdown = breakdownByPmsMysqlId.get(pmsMysqlId);
            UUID breakdownId = breakdown != null ? breakdown.getId() : null;

            PayrollOpeningBalanceTypeEnum balanceType = PayrollHeadingMigrationMapper.inferOpeningBalanceType(
                    source.getTitle(), source.getPayrollheading().getHeadingType());

            linesToSave.add(PayrollOpeningBalanceLineEntity.builder()
                    .mysqlId(source.getId())
                    .openingBalance(header)
                    .employeeId(employee.getId())
                    .balanceType(balanceType)
                    .amount(BigDecimal.valueOf(source.getAmount()))
                    .label(PayrollHeadingMigrationMapper.trimToNull(source.getTitle()))
                    .branchSalaryBreakdownId(breakdownId)
                    .sortOrder(source.getPriority())
                    .build());
            existingLineIds.add(source.getId());
        }

        if (!headersToSave.isEmpty()) {
            openingBalanceRepository.saveAll(headersToSave);
        }
        if (!linesToSave.isEmpty()) {
            openingBalanceLineRepository.saveAll(linesToSave);
        }
        exchange.setProperty("batchImported", linesToSave.size());
    }
}
