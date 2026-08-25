package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyFiscalYearClosingParameter;
import com.jojolaptech.camel.model.postgres.company.CompanyFiscalYearClosingParameterEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyFiscalYearEntity;
import com.jojolaptech.camel.model.postgres.company.LeaveTypeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyFiscalYearClosingParameterRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyFiscalYearRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import com.jojolaptech.camel.repository.postgres.company.PgLeaveTypeRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FiscalYearClosingParameterProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(FiscalYearClosingParameterProcessor.class);

    private final PgCompanyRepository companyRepository;
    private final PgCompanyFiscalYearRepository companyFiscalYearRepository;
    private final PgCompanyFiscalYearClosingParameterRepository closingParameterRepository;
    private final PgLeaveTypeRepository leaveTypeRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<CompanyFiscalYearClosingParameter> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> existingIds = closingParameterRepository.findMysqlIdsByMysqlIdIn(
                batch.stream().map(CompanyFiscalYearClosingParameter::getId).toList());

        Set<Long> companyMysqlIds = batch.stream()
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());
        Map<Long, UUID> companyIdByMysqlId = companyRepository.findByMysqlIdIn(companyMysqlIds).stream()
                .collect(Collectors.toMap(
                        company -> company.getMysqlId(), company -> company.getId()));

        Set<Long> fiscalYearMysqlIds = batch.stream()
                .map(row -> row.getFiscalYear().getId())
                .collect(Collectors.toSet());
        Map<Long, CompanyFiscalYearEntity> companyFyByMysqlId =
                companyFiscalYearRepository.findByMysqlIdIn(fiscalYearMysqlIds).stream()
                        .collect(Collectors.toMap(CompanyFiscalYearEntity::getMysqlId, Function.identity()));

        Set<Long> leaveMysqlIds = batch.stream()
                .map(FiscalYearClosingParameterProcessor::parseLeaveMysqlId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, LeaveTypeEntity> leaveTypeByMysqlId = leaveTypeRepository.findByMysqlIdIn(leaveMysqlIds).stream()
                .collect(Collectors.toMap(LeaveTypeEntity::getMysqlId, Function.identity()));

        List<CompanyFiscalYearClosingParameterEntity> toSave = new ArrayList<>();
        for (CompanyFiscalYearClosingParameter source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            UUID companyId = companyIdByMysqlId.get(source.getCompany().getId());
            CompanyFiscalYearEntity companyFy = companyFyByMysqlId.get(source.getFiscalYear().getId());
            if (companyId == null || companyFy == null) {
                log.warn(
                        "Skipping FY closing parameter id={}, company or fiscal year not migrated",
                        source.getId());
                continue;
            }

            Long leaveMysqlId = parseLeaveMysqlId(source);
            UUID leaveTypeId = null;
            if (leaveMysqlId != null) {
                LeaveTypeEntity leaveType = leaveTypeByMysqlId.get(leaveMysqlId);
                if (leaveType == null) {
                    log.warn(
                            "FY closing parameter id={} references leave mysqlId={} which is not migrated",
                            source.getId(),
                            leaveMysqlId);
                } else {
                    leaveTypeId = leaveType.getId();
                }
            }

            toSave.add(CompanyFiscalYearClosingParameterEntity.builder()
                    .mysqlId(source.getId())
                    .companyId(companyId)
                    .companyFiscalYearId(companyFy.getId())
                    .parameterType(FiscalMigrationMapper.closingParameterType(source.getParameterType()))
                    .leaveTypeId(leaveTypeId)
                    .completed(Boolean.TRUE.equals(source.getValue()))
                    .remarks(FiscalMigrationMapper.trimToNull(source.getRespectiveId()))
                    .build());
        }

        if (!toSave.isEmpty()) {
            closingParameterRepository.saveAll(toSave);
        }

        exchange.setProperty("batchImported", toSave.size());
    }

    private static Long parseLeaveMysqlId(CompanyFiscalYearClosingParameter source) {
        String respectiveId = FiscalMigrationMapper.trimToNull(source.getRespectiveId());
        if (respectiveId == null) {
            return null;
        }
        try {
            return Long.parseLong(respectiveId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
