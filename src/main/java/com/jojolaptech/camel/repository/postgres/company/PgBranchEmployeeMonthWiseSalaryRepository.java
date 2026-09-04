package com.jojolaptech.camel.repository.postgres.company;

import com.jojolaptech.camel.model.postgres.company.BranchEmployeeMonthWiseSalaryEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PgBranchEmployeeMonthWiseSalaryRepository
        extends JpaRepository<BranchEmployeeMonthWiseSalaryEntity, UUID> {

    @Query("select s.mysqlId from BranchEmployeeMonthWiseSalaryEntity s where s.mysqlId in :mysqlIds")
    Set<Long> findMysqlIdsByMysqlIdIn(@Param("mysqlIds") Collection<Long> mysqlIds);

    Optional<BranchEmployeeMonthWiseSalaryEntity> findByMysqlId(Long mysqlId);

    Optional<BranchEmployeeMonthWiseSalaryEntity> findByBranchIdAndEmployeeIdAndSalaryMonthAndSalaryYear(
            UUID branchId, UUID employeeId, Integer salaryMonth, Integer salaryYear);

    @Query("""
            select s from BranchEmployeeMonthWiseSalaryEntity s
            where s.employeeId in :employeeIds
            """)
    List<BranchEmployeeMonthWiseSalaryEntity> findByEmployeeIdIn(@Param("employeeIds") Collection<UUID> employeeIds);
}
