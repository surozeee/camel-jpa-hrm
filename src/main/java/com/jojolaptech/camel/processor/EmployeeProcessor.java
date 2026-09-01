package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyEmployee;
import com.jojolaptech.camel.model.mysql.Employee;
import com.jojolaptech.camel.model.mysql.EmployeeBranch;
import com.jojolaptech.camel.model.mysql.EmployeeBranchDepartment;
import com.jojolaptech.camel.model.mysql.EmployeeSecUser;
import com.jojolaptech.camel.model.mysql.SecUser;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.model.postgres.company.DepartmentEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.repository.mysql.CompanyEmployeeRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeBranchDepartmentRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeBranchRepository;
import com.jojolaptech.camel.repository.mysql.EmployeeSecUserRepository;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import com.jojolaptech.camel.repository.postgres.company.PgDepartmentRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
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
public class EmployeeProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeProcessor.class);

    private final CompanyEmployeeRepository companyEmployeeRepository;
    private final EmployeeBranchDepartmentRepository employeeBranchDepartmentRepository;
    private final EmployeeBranchRepository employeeBranchRepository;
    private final EmployeeSecUserRepository employeeSecUserRepository;
    private final PgBranchRepository branchRepository;
    private final PgDepartmentRepository departmentRepository;
    private final PgEmployeeRepository employeeRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Employee> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> employeeIds = batch.stream().map(Employee::getId).collect(Collectors.toSet());
        Set<Long> existingIds = employeeRepository.findMysqlIdsByMysqlIdIn(employeeIds);
        Set<String> reservedEmails = new HashSet<>(employeeRepository.findExistingEmailsLowerCase(List.of()));
        Set<String> reservedEmployeeCodes = new HashSet<>(employeeRepository.findAllEmployeeCodes());

        Map<Long, List<CompanyEmployee>> companyEmployeesByEmployeeId =
                companyEmployeeRepository.findByEmployeeIdIn(employeeIds).stream()
                        .collect(Collectors.groupingBy(row -> row.getEmployee().getId()));
        Map<Long, List<EmployeeBranchDepartment>> branchDepartmentsByEmployeeId =
                employeeBranchDepartmentRepository.findByEmployeeIdIn(employeeIds).stream()
                        .collect(Collectors.groupingBy(row -> row.getEmployee().getId()));
        Map<Long, List<EmployeeBranch>> branchesByEmployeeId = employeeBranchRepository.findByEmployeeIdIn(employeeIds)
                .stream()
                .collect(Collectors.groupingBy(row -> row.getEmployee().getId()));
        Map<Long, SecUser> secUserByEmployeeId = employeeSecUserRepository.findByEmployeeIdInWithUser(employeeIds)
                .stream()
                .collect(Collectors.toMap(
                        link -> link.getEmployee().getId(),
                        EmployeeSecUser::getUser,
                        (left, right) -> left));

        Set<Long> branchMysqlIds = branchDepartmentsByEmployeeId.values().stream()
                .flatMap(List::stream)
                .filter(row -> row.getBranch() != null)
                .map(row -> row.getBranch().getId())
                .collect(Collectors.toSet());
        branchMysqlIds.addAll(branchesByEmployeeId.values().stream()
                .flatMap(List::stream)
                .filter(row -> row.getBranch() != null)
                .map(row -> row.getBranch().getId())
                .collect(Collectors.toSet()));

        Set<Long> companyMysqlIds = companyEmployeesByEmployeeId.values().stream()
                .flatMap(List::stream)
                .filter(row -> row.getCompany() != null)
                .map(row -> row.getCompany().getId())
                .collect(Collectors.toSet());

        Map<Long, BranchEntity> branchByMysqlId = branchRepository.findByMysqlIdIn(branchMysqlIds).stream()
                .collect(Collectors.toMap(BranchEntity::getMysqlId, branch -> branch, (left, right) -> left));
        Map<Long, List<BranchEntity>> branchesByCompanyMysqlId =
                branchRepository.findByCompanyMysqlIdIn(companyMysqlIds).stream()
                        .collect(Collectors.groupingBy(branch -> branch.getCompany().getMysqlId()));

        Set<Long> departmentMysqlIds = branchDepartmentsByEmployeeId.values().stream()
                .flatMap(List::stream)
                .filter(row -> row.getDepartment() != null)
                .map(row -> row.getDepartment().getId())
                .collect(Collectors.toSet());
        departmentMysqlIds.addAll(branchesByCompanyMysqlId.values().stream()
                .flatMap(List::stream)
                .map(BranchEntity::getMysqlId)
                .collect(Collectors.toSet()));
        Map<String, DepartmentEntity> departmentByKey =
                departmentRepository
                        .findByMysqlIdInAndMysqlBranchIdIn(departmentMysqlIds, branchMysqlIds).stream()
                        .collect(Collectors.toMap(
                                row -> EmployeeMigrationMapper.departmentKey(row.getMysqlId(), row.getMysqlBranchId()),
                                row -> row,
                                (left, right) -> left));

        List<EmployeeEntity> toSave = new ArrayList<>();
        for (Employee source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            CompanyEmployee companyEmployee =
                    EmployeeMigrationMapper.pickActiveCompanyEmployee(
                            companyEmployeesByEmployeeId.getOrDefault(source.getId(), List.of()));
            EmployeeBranchDepartment branchDepartment =
                    EmployeeMigrationMapper.pickActiveBranchDepartment(
                            branchDepartmentsByEmployeeId.getOrDefault(source.getId(), List.of()));
            EmployeeBranch employeeBranch = EmployeeMigrationMapper.pickActiveBranch(
                    branchesByEmployeeId.getOrDefault(source.getId(), List.of()));

            EmployeeEntity entity = EmployeeMigrationMapper.toEmployee(
                    source,
                    companyEmployee,
                    branchDepartment,
                    employeeBranch,
                    branchByMysqlId,
                    branchesByCompanyMysqlId,
                    departmentByKey,
                    secUserByEmployeeId,
                    reservedEmails,
                    reservedEmployeeCodes);
            if (entity.getBranchId() == null) {
                log.warn(
                        "Employee id={} migrated without branch (no assignment and no company branch fallback)",
                        source.getId());
            }
            toSave.add(entity);
        }

        if (!toSave.isEmpty()) {
            employeeRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
    }
}
