package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.MassIncrement;
import com.jojolaptech.camel.model.mysql.enums.PayrollValueType;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.BranchSalaryBreakdownEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.model.postgres.company.GradeEntity;
import com.jojolaptech.camel.model.postgres.company.MassSalaryAdjustmentEntity;
import com.jojolaptech.camel.model.postgres.company.MassSalaryAdjustmentLineEntity;
import com.jojolaptech.camel.model.postgres.company.enums.MassSalaryDirectionEnum;
import com.jojolaptech.camel.model.postgres.company.enums.MassSalaryValueTypeEnum;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchSalaryBreakdownRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgGradeRepository;
import com.jojolaptech.camel.repository.postgres.company.PgMassSalaryAdjustmentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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

/** Migrates massIncrement → hrm_mass_salary_adjustment (+ one line). */
@Component
@RequiredArgsConstructor
public class MassSalaryAdjustmentProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(MassSalaryAdjustmentProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgBranchRepository branchRepository;
    private final PgGradeRepository gradeRepository;
    private final PgBranchSalaryBreakdownRepository breakdownRepository;
    private final PgMassSalaryAdjustmentRepository massSalaryAdjustmentRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<MassIncrement> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> existingIds = massSalaryAdjustmentRepository.findMysqlIdsByMysqlIdIn(
                batch.stream().map(MassIncrement::getId).collect(Collectors.toSet()));

        Set<Long> companyMysqlIds = batch.stream()
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        companyMysqlIds.addAll(batch.stream()
                .filter(row -> row.getJobLevel() != null && row.getJobLevel().getCompany() != null)
                .map(row -> row.getJobLevel().getCompany().getId())
                .collect(Collectors.toSet()));
        Map<Long, UUID> companyIdByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(CompanyEntity::getMysqlId, CompanyEntity::getId, (a, b) -> a));

        Set<Long> branchMysqlIds = batch.stream()
                .filter(row -> row.getBranch() != null)
                .map(row -> row.getBranch().getId())
                .collect(Collectors.toSet());
        Map<Long, BranchEntity> branchByMysqlId = branchRepository.findByMysqlIdIn(branchMysqlIds).stream()
                .collect(Collectors.toMap(BranchEntity::getMysqlId, b -> b, (a, b) -> a));

        Set<Long> gradeMysqlIds = batch.stream()
                .filter(row -> row.getJobLevel() != null)
                .map(row -> row.getJobLevel().getId())
                .collect(Collectors.toSet());
        Map<Long, GradeEntity> gradeByMysqlId = gradeRepository.findByMysqlIdIn(gradeMysqlIds).stream()
                .collect(Collectors.toMap(GradeEntity::getMysqlId, g -> g, (a, b) -> a));

        Set<UUID> companyIds = companyIdByMysqlId.values().stream().collect(Collectors.toSet());
        for (BranchEntity branch : branchByMysqlId.values()) {
            if (branch.getCompany() != null) {
                companyIds.add(branch.getCompany().getId());
            }
        }
        Map<UUID, List<BranchSalaryBreakdownEntity>> breakdownsByCompany = new HashMap<>();
        if (!companyIds.isEmpty()) {
            for (BranchSalaryBreakdownEntity row : breakdownRepository.findByCompanyIdIn(companyIds)) {
                breakdownsByCompany.computeIfAbsent(row.getCompanyId(), k -> new ArrayList<>()).add(row);
            }
        }

        List<MassSalaryAdjustmentEntity> toSave = new ArrayList<>();
        for (MassIncrement source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getPayrollHeading() == null) {
                log.warn("Skipping massIncrement id={}, missing payrollHeading", source.getId());
                continue;
            }

            UUID companyId = resolveCompanyId(source, companyIdByMysqlId, branchByMysqlId);
            if (companyId == null) {
                log.warn("Skipping massIncrement id={}, company not resolved/migrated", source.getId());
                continue;
            }

            UUID branchId = null;
            if (source.getBranch() != null) {
                BranchEntity branch = branchByMysqlId.get(source.getBranch().getId());
                if (branch == null) {
                    log.warn(
                            "Skipping massIncrement id={}, branch mysqlId={} not migrated",
                            source.getId(),
                            source.getBranch().getId());
                    continue;
                }
                branchId = branch.getId();
            }

            UUID gradeId = null;
            if (source.getJobLevel() != null) {
                GradeEntity grade = gradeByMysqlId.get(source.getJobLevel().getId());
                if (grade == null) {
                    log.warn(
                            "Skipping massIncrement id={}, grade mysqlId={} not migrated",
                            source.getId(),
                            source.getJobLevel().getId());
                    continue;
                }
                gradeId = grade.getId();
            }

            String lineName = source.getPayrollHeading().getHeadingName();
            BranchSalaryBreakdownEntity breakdown = findBreakdown(breakdownsByCompany.get(companyId), lineName);
            if (breakdown == null) {
                log.warn(
                        "Skipping massIncrement id={}, no breakdown for company={} lineName={}",
                        source.getId(),
                        companyId,
                        lineName);
                continue;
            }

            LocalDate effectiveDate = PayrollHeadingMigrationMapper.toLocalDate(source.getIncrementDate());
            if (effectiveDate == null) {
                effectiveDate = LocalDate.now();
            }

            MassSalaryDirectionEnum direction = Boolean.TRUE.equals(source.getDecrement())
                    ? MassSalaryDirectionEnum.DECREMENT
                    : MassSalaryDirectionEnum.INCREMENT;
            MassSalaryValueTypeEnum valueType = source.getValueType() == PayrollValueType.PERCENTAGE
                    ? MassSalaryValueTypeEnum.PERCENTAGE
                    : MassSalaryValueTypeEnum.FLAT_AMOUNT;

            MassSalaryAdjustmentEntity header = MassSalaryAdjustmentEntity.builder()
                    .mysqlId(source.getId())
                    .companyId(companyId)
                    .branchId(branchId)
                    .gradeId(gradeId)
                    .effectiveDate(effectiveDate)
                    .direction(direction)
                    .remarks("migrated from massIncrement#" + source.getId())
                    .eligibleCount(0)
                    .processedCount(0)
                    .skippedCount(0)
                    .failedCount(0)
                    .appliedAt(LocalDateTime.now())
                    .dryRun(false)
                    .lines(new ArrayList<>())
                    .build();

            MassSalaryAdjustmentLineEntity line = MassSalaryAdjustmentLineEntity.builder()
                    .mysqlId(source.getId())
                    .adjustment(header)
                    .branchSalaryBreakdownId(breakdown.getId())
                    .lineName(lineName)
                    .valueType(valueType)
                    .adjustmentValue(
                            source.getIncrementValue() != null ? source.getIncrementValue() : BigDecimal.ZERO)
                    .build();
            header.getLines().add(line);
            toSave.add(header);
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            massSalaryAdjustmentRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }

    private static UUID resolveCompanyId(
            MassIncrement source,
            Map<Long, UUID> companyIdByMysqlId,
            Map<Long, BranchEntity> branchByMysqlId) {
        if (source.getCompany() != null) {
            UUID id = companyIdByMysqlId.get(source.getCompany().getId());
            if (id != null) {
                return id;
            }
        }
        if (source.getJobLevel() != null && source.getJobLevel().getCompany() != null) {
            UUID id = companyIdByMysqlId.get(source.getJobLevel().getCompany().getId());
            if (id != null) {
                return id;
            }
        }
        if (source.getBranch() != null) {
            BranchEntity branch = branchByMysqlId.get(source.getBranch().getId());
            if (branch != null && branch.getCompany() != null) {
                return branch.getCompany().getId();
            }
        }
        return null;
    }

    private static BranchSalaryBreakdownEntity findBreakdown(
            List<BranchSalaryBreakdownEntity> rows, String lineName) {
        if (rows == null || lineName == null) {
            return null;
        }
        String target = lineName.trim();
        for (BranchSalaryBreakdownEntity row : rows) {
            if (row.getLineName() != null && row.getLineName().equalsIgnoreCase(target)) {
                return row;
            }
        }
        return null;
    }
}
