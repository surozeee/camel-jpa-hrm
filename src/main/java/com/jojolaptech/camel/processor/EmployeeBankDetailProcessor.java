package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EmployeePayrollPaymentSetting;
import com.jojolaptech.camel.model.postgres.company.BankDetailEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.enums.AccountTypeEnum;
import com.jojolaptech.camel.model.postgres.master.BankEntity;
import com.jojolaptech.camel.repository.postgres.company.PgBankDetailRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import com.jojolaptech.camel.repository.postgres.master.PgBankRepository;
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
 * Migrates employeePayrollPaymentSetting (with bank + account) → hrm_employee_bank_detail.
 */
@Component
@RequiredArgsConstructor
public class EmployeeBankDetailProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeBankDetailProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgBankRepository bankRepository;
    private final PgBankDetailRepository bankDetailRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<EmployeePayrollPaymentSetting> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> existingIds = bankDetailRepository.findMysqlIdsByMysqlIdIn(
                batch.stream().map(EmployeePayrollPaymentSetting::getId).collect(Collectors.toSet()));

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        Set<Long> bankMysqlIds = batch.stream()
                .filter(row -> row.getBank() != null)
                .map(row -> row.getBank().getId())
                .collect(Collectors.toSet());
        Map<Long, UUID> bankIdByMysqlId = bankRepository.findByMysqlIdIn(bankMysqlIds).stream()
                .collect(Collectors.toMap(BankEntity::getMysqlId, BankEntity::getId, (a, b) -> a));

        List<BankDetailEntity> toSave = new ArrayList<>();
        for (EmployeePayrollPaymentSetting source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getBank() == null
                    || source.getInstitutionIdentity() == null
                    || source.getInstitutionIdentity().isBlank()) {
                continue;
            }
            if (source.getEmployee() == null) {
                log.warn("Skipping paymentSetting id={}, missing employee", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping paymentSetting id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }
            UUID bankId = bankIdByMysqlId.get(source.getBank().getId());
            if (bankId == null) {
                log.warn(
                        "Skipping paymentSetting id={}, bank mysqlId={} not migrated",
                        source.getId(),
                        source.getBank().getId());
                continue;
            }

            String remarks = source.getPaymentMethod() != null
                    ? "paymentMethod=" + source.getPaymentMethod().name()
                    : null;

            toSave.add(BankDetailEntity.builder()
                    .mysqlId(source.getId())
                    .bankId(bankId)
                    .accountNumber(source.getInstitutionIdentity().trim())
                    .bankBranch(PayrollHeadingMigrationMapper.trimToNull(source.getInstitutionDetail()))
                    .accountType(AccountTypeEnum.SALARY)
                    .isPrimary(Boolean.TRUE.equals(source.getStatus()))
                    .remarks(remarks)
                    .employee(employee)
                    .build());
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            bankDetailRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
