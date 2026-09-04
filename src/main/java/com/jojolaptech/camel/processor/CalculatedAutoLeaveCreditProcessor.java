package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CalculatedAutoLeaveAccumulation;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyFiscalYearEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.LeaveCreditEntity;
import com.jojolaptech.camel.model.postgres.company.LeaveTypeEntity;
import com.jojolaptech.camel.model.postgres.company.enums.AccumulationPeriodEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveCreditOperationType;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveCreditStatus;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveCreditTimingEnum;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyFiscalYearRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLeaveCreditRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLeaveTypeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class CalculatedAutoLeaveCreditProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(CalculatedAutoLeaveCreditProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgCompanyRepository companyRepository;
    private final PgLeaveTypeRepository leaveTypeRepository;
    private final PgCompanyFiscalYearRepository companyFiscalYearRepository;
    private final PgLeaveCreditRepository leaveCreditRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<CalculatedAutoLeaveAccumulation> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(CalculatedAutoLeaveAccumulation::getId).collect(Collectors.toSet());
        Set<Long> existingMysqlIds = new HashSet<>(leaveCreditRepository.findMysqlIdsByMysqlIdIn(mysqlIds));

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<Long> companyMysqlIds = batch.stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyEntity> companyByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, c -> c, (a, b) -> a));

        Set<Long> leaveMysqlIds = batch.stream()
                .filter(row -> row.getLeave() != null)
                .map(row -> row.getLeave().getId())
                .collect(Collectors.toSet());
        Map<Long, LeaveTypeEntity> leaveTypeByMysqlId = leaveTypeRepository.findByMysqlIdIn(leaveMysqlIds).stream()
                .collect(Collectors.toMap(LeaveTypeEntity::getMysqlId, l -> l, (a, b) -> a));

        Set<Long> fiscalMysqlIds = batch.stream()
                .filter(row -> row.getFiscalYear() != null)
                .map(row -> row.getFiscalYear().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyFiscalYearEntity> fiscalByMysqlId =
                companyFiscalYearRepository.findByMysqlIdIn(fiscalMysqlIds).stream()
                        .collect(Collectors.toMap(CompanyFiscalYearEntity::getMysqlId, f -> f, (a, b) -> a));

        List<LeaveCreditEntity> toSave = new ArrayList<>();
        int imported = 0;

        for (CalculatedAutoLeaveAccumulation source : batch) {
            if (existingMysqlIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployee() == null || source.getCompany() == null || source.getLeave() == null) {
                log.warn(
                        "Skipping calculatedAutoLeaveAccumulation id={}, missing employee/company/leave",
                        source.getId());
                continue;
            }

            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping calculatedAutoLeaveAccumulation id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }
            if (employee.getBranchId() == null) {
                log.warn(
                        "Skipping calculatedAutoLeaveAccumulation id={}, employee mysqlId={} has null branchId",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }

            CompanyEntity company = companyByMysqlId.get(source.getCompany().getId());
            if (company == null) {
                log.warn(
                        "Skipping calculatedAutoLeaveAccumulation id={}, company mysqlId={} not migrated",
                        source.getId(),
                        source.getCompany().getId());
                continue;
            }

            LeaveTypeEntity leaveType = leaveTypeByMysqlId.get(source.getLeave().getId());
            if (leaveType == null) {
                log.warn(
                        "Skipping calculatedAutoLeaveAccumulation id={}, leaveType mysqlId={} not migrated",
                        source.getId(),
                        source.getLeave().getId());
                continue;
            }

            LocalDate periodStart = CalculatedLeaveMigrationMapper.monthStart(source.getCalYear(), source.getCalMonth());
            LocalDate periodEnd = CalculatedLeaveMigrationMapper.monthEnd(source.getCalYear(), source.getCalMonth());
            LocalDate createdDate = CalculatedLeaveMigrationMapper.toLocalDate(source.getCreatedDate());
            LocalDate effectiveDate = periodEnd != null ? periodEnd : createdDate;
            if (effectiveDate == null) {
                log.warn(
                        "Skipping calculatedAutoLeaveAccumulation id={}, missing effective date",
                        source.getId());
                continue;
            }

            boolean verified = Boolean.TRUE.equals(source.getVerified());
            LeaveCreditStatus creditStatus =
                    verified ? LeaveCreditStatus.POSTED : LeaveCreditStatus.PENDING_VERIFICATION;
            LocalDateTime createdAt = CalculatedLeaveMigrationMapper.toLocalDateTime(source.getCreatedDate());

            CompanyFiscalYearEntity fiscal = source.getFiscalYear() != null
                    ? fiscalByMysqlId.get(source.getFiscalYear().getId())
                    : null;

            String remarks = CalculatedLeaveMigrationMapper.truncate(
                    "leaveValue="
                            + source.getLeaveValue()
                            + "|calculated="
                            + source.getCalculatedValue()
                            + "|totalLeave="
                            + source.getTotalLeave()
                            + "|verified="
                            + source.getVerified(),
                    1000);

            LeaveCreditEntity entity = LeaveCreditEntity.builder()
                    .mysqlId(source.getId())
                    .idempotencyKey("MIGRATE-AUTO-ACC-" + source.getId())
                    .companyId(company.getId())
                    .branchId(employee.getBranchId())
                    .employeeId(employee.getId())
                    .leaveTypeId(leaveType.getId())
                    .leaveType(CalculatedLeaveMigrationMapper.mapLeaveTypeEnum(source.getLeave().getLeaveName()))
                    .operationType(LeaveCreditOperationType.LEAVE_ACCUMULATION)
                    .creditStatus(creditStatus)
                    .quantity(CalculatedLeaveMigrationMapper.toBigDecimal(source.getLeaveValue()))
                    .effectiveDate(effectiveDate)
                    .periodStart(periodStart)
                    .periodEnd(periodEnd)
                    .accumulationPeriod(AccumulationPeriodEnum.MONTH)
                    .creditTiming(LeaveCreditTimingEnum.END_OF_PERIOD)
                    .accumulationMonth(CalculatedLeaveMigrationMapper.accumulationMonth(
                            source.getCalYear(), source.getCalMonth()))
                    .fiscalYearId(fiscal != null ? fiscal.getId() : null)
                    .systemGenerated(true)
                    .reason("Migrated auto leave accumulation")
                    .remarks(remarks)
                    .postedAt(creditStatus == LeaveCreditStatus.POSTED ? createdAt : null)
                    .verifiedAt(verified ? createdAt : null)
                    .build();

            toSave.add(entity);
            existingMysqlIds.add(source.getId());
            imported++;
        }

        if (!toSave.isEmpty()) {
            leaveCreditRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", imported);
    }
}
