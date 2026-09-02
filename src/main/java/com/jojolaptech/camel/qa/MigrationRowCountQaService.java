package com.jojolaptech.camel.qa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class MigrationRowCountQaService {

    private static final Logger log = LoggerFactory.getLogger(MigrationRowCountQaService.class);

    /** Must match EmployeeProfileMigrationMapper.EMPLOYEE_MASTER_ADDRESS_MYSQL_ID_OFFSET */
    private static final long MASTER_ADDRESS_MYSQL_ID_OFFSET = 9_000_000_000_000L;

    private final JdbcTemplate mysqlJdbc;
    private final JdbcTemplate postgresJdbc;

    public MigrationRowCountQaService(
            @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbc,
            @Qualifier("postgresJdbcTemplate") JdbcTemplate postgresJdbc) {
        this.mysqlJdbc = mysqlJdbc;
        this.postgresJdbc = postgresJdbc;
    }

    private static final List<MigrationRowCountCheck> CHECKS = List.of(
            new MigrationRowCountCheck(
                    "1",
                    "privilege (requestmap → permission)",
                    "SELECT COUNT(*) FROM requestmap",
                    "SELECT COUNT(*) FROM permission WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.EQUAL,
                    null),
            new MigrationRowCountCheck(
                    "2",
                    "role (secRole → role)",
                    "SELECT COUNT(*) FROM secRole",
                    "SELECT COUNT(*) FROM role WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.EQUAL,
                    null),
            new MigrationRowCountCheck(
                    "3",
                    "company → company",
                    "SELECT COUNT(*) FROM company",
                    "SELECT COUNT(*) FROM company WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.EQUAL,
                    null),
            new MigrationRowCountCheck(
                    "3b",
                    "organization (1 per company)",
                    "SELECT COUNT(*) FROM company",
                    "SELECT COUNT(*) FROM organization",
                    MigrationComparisonMode.EQUAL,
                    "Synthetic org rows; should match company count"),
            new MigrationRowCountCheck(
                    "4",
                    "company address",
                    "SELECT COUNT(*) FROM company WHERE NULLIF(TRIM(address), '') IS NOT NULL OR NULLIF(TRIM(fax), '') IS NOT NULL",
                    "SELECT COUNT(*) FROM hrm_company_address WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips when company not migrated"),
            new MigrationRowCountCheck(
                    "5",
                    "branch → branch",
                    "SELECT COUNT(*) FROM branch",
                    "SELECT COUNT(*) FROM branch WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.EQUAL,
                    null),
            new MigrationRowCountCheck(
                    "6",
                    "branch address",
                    "SELECT COUNT(*) FROM branch WHERE NULLIF(TRIM(address), '') IS NOT NULL OR NULLIF(TRIM(faxNo), '') IS NOT NULL",
                    "SELECT COUNT(*) FROM branch WHERE address_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Branch address rows have no mysql_id"),
            new MigrationRowCountCheck(
                    "8",
                    "taxation → nepali_tax",
                    "SELECT COUNT(*) FROM taxation",
                    "SELECT COUNT(*) FROM nepali_tax",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "nepali_tax has no mysql_id column"),
            new MigrationRowCountCheck(
                    "10",
                    "leaves → hrm_leave_type",
                    "SELECT COUNT(*) FROM leaves",
                    "SELECT COUNT(*) FROM hrm_leave_type WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.EQUAL,
                    null),
            new MigrationRowCountCheck(
                    "14",
                    "attHolidayDate → hrm_branch_holiday",
                    "SELECT COUNT(*) FROM attHolidayDate",
                    "SELECT COUNT(*) FROM hrm_branch_holiday WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips when branch not migrated"),
            new MigrationRowCountCheck(
                    "22",
                    "employee → employee",
                    "SELECT COUNT(*) FROM employee",
                    "SELECT COUNT(*) FROM employee WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "May skip unmigratable rows"),
            new MigrationRowCountCheck(
                    "22a",
                    "employeeAddress → address",
                    "SELECT COUNT(*) FROM employeeAddress",
                    "SELECT COUNT(*) FROM address WHERE employee_id IS NOT NULL AND mysql_id IS NOT NULL AND mysql_id < "
                            + MASTER_ADDRESS_MYSQL_ID_OFFSET,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips rows without street/address or employee FK"),
            new MigrationRowCountCheck(
                    "22b",
                    "employee master addresses",
                    "SELECT COUNT(*) FROM employee WHERE NULLIF(TRIM(permanentAdd), '') IS NOT NULL",
                    "SELECT COUNT(*) FROM address WHERE mysql_id >= " + MASTER_ADDRESS_MYSQL_ID_OFFSET
                            + " AND MOD(mysql_id, 10) = 1",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "permanentAdd only; suffix 1"),
            new MigrationRowCountCheck(
                    "22b",
                    "employee master temp addresses",
                    "SELECT COUNT(*) FROM employee WHERE NULLIF(TRIM(temperoryAdd), '') IS NOT NULL",
                    "SELECT COUNT(*) FROM address WHERE mysql_id >= " + MASTER_ADDRESS_MYSQL_ID_OFFSET
                            + " AND MOD(mysql_id, 10) = 2",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "temperoryAdd only; suffix 2"),
            new MigrationRowCountCheck(
                    "22c",
                    "employeeEducation → hrm_employee_education",
                    "SELECT COUNT(*) FROM employeeEducation",
                    "SELECT COUNT(*) FROM hrm_employee_education WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips rows missing institution/level"),
            new MigrationRowCountCheck(
                    "22d",
                    "family → employee_family_detail",
                    "SELECT COUNT(*) FROM family",
                    "SELECT COUNT(*) FROM employee_family_detail WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips rows without name"),
            new MigrationRowCountCheck(
                    "23",
                    "leaveAccumulation → leave_accumulation",
                    "SELECT COUNT(*) FROM leaveAccumulation",
                    "SELECT COUNT(*) FROM leave_accumulation WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips missing employee/leave/date"),
            new MigrationRowCountCheck(
                    "24",
                    "secUser → users",
                    "SELECT COUNT(*) FROM secUser",
                    "SELECT COUNT(*) FROM users WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    null),
            new MigrationRowCountCheck(
                    "25",
                    "user_detail",
                    "SELECT COUNT(*) FROM secUser",
                    "SELECT COUNT(*) FROM user_detail",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "One detail per migrated user"));

    private static final Map<String, String> PIPELINE_PG_COUNT_SQL = new LinkedHashMap<>();

    static {
        PIPELINE_PG_COUNT_SQL.put("privilegeCount", "SELECT COUNT(*) FROM permission WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("roleCount", "SELECT COUNT(*) FROM role WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("companyCount", "SELECT COUNT(*) FROM company WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("organizationCount", "SELECT COUNT(*) FROM organization");
        PIPELINE_PG_COUNT_SQL.put("companyAddressCount", "SELECT COUNT(*) FROM hrm_company_address WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("branchCount", "SELECT COUNT(*) FROM branch WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("leaveTypeCount", "SELECT COUNT(*) FROM hrm_leave_type WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("attTimeTableShiftCount", "SELECT COUNT(*) FROM hrm_branch_shift WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("branchHolidayCount", "SELECT COUNT(*) FROM hrm_branch_holiday WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("leaveAccumulationRuleCount",
                "SELECT COUNT(*) FROM hrm_branch_leave_accumulation_rule WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("departmentCount", "SELECT COUNT(*) FROM department WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("employeeCount", "SELECT COUNT(*) FROM employee WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("employeeAddressCount",
                "SELECT COUNT(*) FROM address WHERE employee_id IS NOT NULL AND mysql_id IS NOT NULL AND mysql_id < "
                        + MASTER_ADDRESS_MYSQL_ID_OFFSET);
        PIPELINE_PG_COUNT_SQL.put("employeeMasterAddressCount",
                "SELECT COUNT(*) FROM address WHERE mysql_id >= " + MASTER_ADDRESS_MYSQL_ID_OFFSET);
        PIPELINE_PG_COUNT_SQL.put("employeeEducationCount",
                "SELECT COUNT(*) FROM hrm_employee_education WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("employeeFamilyCount",
                "SELECT COUNT(*) FROM employee_family_detail WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("employeeLeaveAccumulationCount",
                "SELECT COUNT(*) FROM leave_accumulation WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("userCount", "SELECT COUNT(*) FROM users WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("userDetailCount", "SELECT COUNT(*) FROM user_detail");
    }

    public List<MigrationRowCountResult> runSourceChecks() {
        List<MigrationRowCountResult> results = new ArrayList<>();
        for (MigrationRowCountCheck check : CHECKS) {
            results.add(runCheck(check));
        }
        return results;
    }

    public List<MigrationRowCountResult> runPipelineChecks(Exchange exchange) {
        List<MigrationRowCountResult> results = new ArrayList<>();
        for (Map.Entry<String, String> entry : PIPELINE_PG_COUNT_SQL.entrySet()) {
            int imported = exchange.getProperty(entry.getKey(), 0, Integer.class);
            long pgCount = countPostgres(entry.getValue());
            long delta = pgCount - imported;
            boolean passed = imported == pgCount;
            results.add(new MigrationRowCountResult(
                    "pipeline",
                    entry.getKey(),
                    imported,
                    pgCount,
                    delta,
                    MigrationComparisonMode.EQUAL,
                    passed,
                    "Imported count vs PG mysql_id rows"));
        }
        return results;
    }

    public void logReport(List<MigrationRowCountResult> sourceChecks, List<MigrationRowCountResult> pipelineChecks) {
        log.info("==========================================");
        log.info("Migration row-count QA report");
        log.info("==========================================");
        log.info("--- MySQL source vs PostgreSQL ---");
        logSourceTable(sourceChecks);
        log.info("--- Pipeline imported vs PostgreSQL ---");
        logSourceTable(pipelineChecks);
        long failed = sourceChecks.stream().filter(result -> !result.passed()).count()
                + pipelineChecks.stream().filter(result -> !result.passed()).count();
        log.info("QA summary: {} failed / {} checks", failed, sourceChecks.size() + pipelineChecks.size());
        log.info("==========================================");
    }

    private void logSourceTable(List<MigrationRowCountResult> results) {
        for (MigrationRowCountResult result : results) {
            String status = result.passed() ? "PASS" : "FAIL";
            log.info(
                    "{} | step {} | {} | mysql={} pg={} delta={} | {}",
                    status,
                    result.step(),
                    result.label(),
                    result.mysqlCount(),
                    result.postgresCount(),
                    result.delta(),
                    result.notes() != null ? result.notes() : "");
        }
    }

    private MigrationRowCountResult runCheck(MigrationRowCountCheck check) {
        long mysqlCount = countMysql(check.mysqlSql());
        long postgresCount = countPostgres(check.postgresSql());
        long delta = postgresCount - mysqlCount;
        boolean passed = evaluate(check.mode(), mysqlCount, postgresCount);
        return new MigrationRowCountResult(
                check.step(),
                check.label(),
                mysqlCount,
                postgresCount,
                delta,
                check.mode(),
                passed,
                check.notes());
    }

    private static boolean evaluate(MigrationComparisonMode mode, long mysqlCount, long postgresCount) {
        return switch (mode) {
            case EQUAL -> postgresCount == mysqlCount;
            case PG_AT_MOST_MYSQL -> postgresCount <= mysqlCount;
            case PG_AT_LEAST_MYSQL -> postgresCount >= mysqlCount;
            case INFO -> true;
        };
    }

    private long countMysql(String sql) {
        try {
            Long count = mysqlJdbc.queryForObject(sql, Long.class);
            return count != null ? count : 0L;
        } catch (Exception ex) {
            log.warn("MySQL count failed for [{}]: {}", sql, ex.getMessage());
            return -1L;
        }
    }

    private long countPostgres(String sql) {
        try {
            Long count = postgresJdbc.queryForObject(sql, Long.class);
            return count != null ? count : 0L;
        } catch (Exception ex) {
            log.warn("PostgreSQL count failed for [{}]: {}", sql, ex.getMessage());
            return -1L;
        }
    }
}
