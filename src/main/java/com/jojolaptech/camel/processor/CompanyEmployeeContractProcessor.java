package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyEmployeeContract;
import com.jojolaptech.camel.model.postgres.company.EmployeeContractEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeContractRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import java.util.ArrayList;
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

/**
 * Step 22zd: companyEmployeeContract → hrm_employee_contract.
 */
@Component
@RequiredArgsConstructor
public class CompanyEmployeeContractProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(CompanyEmployeeContractProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgEmployeeContractRepository contractRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<CompanyEmployeeContract> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> mysqlIds = batch.stream().map(CompanyEmployeeContract::getId).collect(Collectors.toSet());
        Set<Long> existingIds = contractRepository.findMysqlIdsByMysqlIdIn(mysqlIds);

        Set<Long> employeeMysqlIds = batch.stream()
                .filter(row -> row.getEmployee() != null)
                .map(row -> row.getEmployee().getId())
                .collect(Collectors.toSet());
        Map<Long, EmployeeEntity> employeeByMysqlId = employeeRepository.findByMysqlIdIn(employeeMysqlIds).stream()
                .collect(Collectors.toMap(EmployeeEntity::getMysqlId, e -> e, (a, b) -> a));

        List<EmployeeContractEntity> toSave = new ArrayList<>();
        for (CompanyEmployeeContract source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getEmployee() == null) {
                log.warn("Skipping companyEmployeeContract id={}, missing employee", source.getId());
                continue;
            }
            if (source.getContractStartDate() == null) {
                log.warn("Skipping companyEmployeeContract id={}, missing contractStartDate", source.getId());
                continue;
            }
            EmployeeEntity employee = employeeByMysqlId.get(source.getEmployee().getId());
            if (employee == null) {
                log.warn(
                        "Skipping companyEmployeeContract id={}, employee mysqlId={} not migrated",
                        source.getId(),
                        source.getEmployee().getId());
                continue;
            }

            EmployeeContractEntity mapped =
                    OrgStructureLeftoversMigrationMapper.fromCompanyEmployeeContract(source, employee.getId());
            if (mapped == null) {
                log.warn("Skipping companyEmployeeContract id={}, missing startDate", source.getId());
                continue;
            }
            toSave.add(mapped);
            existingIds.add(source.getId());
        }

        if (!toSave.isEmpty()) {
            contractRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
