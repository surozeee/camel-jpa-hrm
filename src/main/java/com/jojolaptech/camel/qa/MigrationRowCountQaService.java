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
                    "9",
                    "payrollCalculationSetting → payroll_rule",
                    "SELECT COUNT(DISTINCT company_id) FROM payrollCalculationSetting",
                    "SELECT COUNT(*) FROM payroll_rule",
                    MigrationComparisonMode.INFO,
                    "Rules are FY-scoped; company settings overlay params"),
            new MigrationRowCountCheck(
                    "9a",
                    "companyPayrollHeading → hrm_branch_salary_breakdown",
                    "SELECT COUNT(*) FROM companyPayrollHeading WHERE status = 1 AND payrollHeading_id IS NOT NULL",
                    "SELECT COUNT(*) FROM hrm_branch_salary_breakdown WHERE mysql_id IS NOT NULL AND mysql_id < "
                            + 8_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips PARENT / inactive / unmigrated company"),
            new MigrationRowCountCheck(
                    "9b",
                    "payrollHeading (PMS) → hrm_branch_salary_breakdown",
                    "SELECT COUNT(*) FROM payrollHeading",
                    "SELECT COUNT(*) FROM hrm_branch_salary_breakdown WHERE mysql_id >= " + 8_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Offset mysql_id for PMS headings"),
            new MigrationRowCountCheck(
                    "9c",
                    "jobLevel → hrm_grade",
                    "SELECT COUNT(*) FROM jobLevel WHERE status = 1",
                    "SELECT COUNT(*) FROM hrm_grade WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips unmigrated company"),
            new MigrationRowCountCheck(
                    "9d",
                    "jobLevelGradeValue → hrm_grade_pay_step",
                    "SELECT COUNT(*) FROM jobLevelGradeValue WHERE status = 1",
                    "SELECT COUNT(*) FROM hrm_grade_pay_step WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Requires migrated grade"),
            new MigrationRowCountCheck(
                    "9e",
                    "jobLevelPayroll → hrm_grade_component_value",
                    "SELECT COUNT(*) FROM jobLevelPayroll WHERE status = 1",
                    "SELECT COUNT(*) FROM hrm_grade_component_value WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Requires grade + breakdown"),
            new MigrationRowCountCheck(
                    "9g",
                    "bank → bank (master)",
                    "SELECT COUNT(*) FROM bank",
                    "SELECT COUNT(*) FROM bank WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips blank/duplicate names"),
            new MigrationRowCountCheck(
                    "9h",
                    "costType → module_pricing_package",
                    "SELECT COUNT(*) FROM costType",
                    "SELECT COUNT(*) FROM module_pricing_package WHERE mysql_id IS NOT NULL AND mysql_id < "
                            + 23_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "SaaS billing catalog; not hrm_cost_center"),
            new MigrationRowCountCheck(
                    "9i",
                    "payPlan → module_pricing_package",
                    "SELECT COUNT(*) FROM payPlan",
                    "SELECT COUNT(*) FROM module_pricing_package WHERE mysql_id >= " + 23_000_000_000_000L
                            + " AND mysql_id < " + 24_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "mysql_id offset 23e12"),
            new MigrationRowCountCheck(
                    "9j",
                    "modulePricing → module_pricing_package",
                    "SELECT COUNT(*) FROM modulePricing",
                    "SELECT COUNT(*) FROM module_pricing_package WHERE mysql_id >= " + 26_000_000_000_000L
                            + " AND mysql_id < " + 27_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Pricing tiers only; module scopes stay ERP-seeded (no module_pricing_scope)"),
            new MigrationRowCountCheck(
                    "9k",
                    "payType → module_pricing_package",
                    "SELECT COUNT(*) FROM payType",
                    "SELECT COUNT(*) FROM module_pricing_package WHERE mysql_id >= " + 34_000_000_000_000L
                            + " AND mysql_id < " + 35_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "mysql_id offset 34e12; skips duplicate package_code"),
            new MigrationRowCountCheck(
                    "9l",
                    "payrollInstitution → master_lookup",
                    "SELECT COUNT(*) FROM payrollInstitution",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 44_000_000_000_000L
                            + " AND mysql_id < " + 45_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "PAYROLL_INSTITUTION; mysql_id offset 44e12"),
            new MigrationRowCountCheck(
                    "9m",
                    "companyPayroll → hrm_company_bank",
                    "SELECT COUNT(*) FROM companyPayroll",
                    "SELECT COUNT(*) FROM hrm_company_bank WHERE mysql_id >= " + 45_000_000_000_000L
                            + " AND mysql_id < " + 46_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "mysql_id offset 45e12; requires migrated company + bank"),
            new MigrationRowCountCheck(
                    "9n",
                    "companyPayrollInstitution → master_lookup",
                    "SELECT COUNT(*) FROM companyPayrollInstitution",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 46_000_000_000_000L
                            + " AND mysql_id < " + 47_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "COMPANY_PAYROLL_INSTITUTION; passwords not migrated; offset 46e12"),
            new MigrationRowCountCheck(
                    "9o",
                    "parentPayrollHeading → master_lookup",
                    "SELECT COUNT(*) FROM parentPayrollHeading",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 47_000_000_000_000L
                            + " AND mysql_id < " + 48_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "PARENT_PAYROLL_HEADING; offset 47e12"),
            new MigrationRowCountCheck(
                    "9o2",
                    "childPayrollHeading → master_lookup",
                    "SELECT COUNT(*) FROM childPayrollHeading",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 55_000_000_000_000L
                            + " AND mysql_id < " + 56_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "CHILD_PAYROLL_HEADING; offset 55e12"),
            new MigrationRowCountCheck(
                    "9p",
                    "companyBranchPayrollHeading → breakdown.branch_id (enrich)",
                    "SELECT COUNT(*) FROM companyBranchPayrollHeading WHERE status = true",
                    "SELECT COUNT(*) FROM hrm_branch_salary_breakdown WHERE branch_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Enrich-only: sets branch_id on existing breakdowns; weak PG proxy"),
            new MigrationRowCountCheck(
                    "9q",
                    "payrollLabel → master_lookup",
                    "SELECT COUNT(*) FROM payrollLabel",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 48_000_000_000_000L
                            + " AND mysql_id < " + 49_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "PAYROLL_LABEL; offset 48e12"),
            new MigrationRowCountCheck(
                    "9r",
                    "payrollHeadingPriority → master_lookup",
                    "SELECT COUNT(*) FROM payrollHeadingPriority",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 49_000_000_000_000L
                            + " AND mysql_id < " + 50_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "PAYROLL_HEADING_PRIORITY; offset 49e12"),
            new MigrationRowCountCheck(
                    "9s",
                    "payrollHeadingTemplate → master_lookup",
                    "SELECT COUNT(*) FROM payrollHeadingTemplate",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 50_000_000_000_000L
                            + " AND mysql_id < " + 51_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "PAYROLL_HEADING_TEMPLATE; offset 50e12"),
            new MigrationRowCountCheck(
                    "9t",
                    "payrollHeadingDate → master_lookup",
                    "SELECT COUNT(*) FROM payrollHeadingDate",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 51_000_000_000_000L
                            + " AND mysql_id < " + 52_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "LEGACY_PAYROLL_HEADING_DATE; offset 51e12"),
            new MigrationRowCountCheck(
                    "9u",
                    "payrollHeadingCalculation → master_lookup",
                    "SELECT COUNT(*) FROM payrollHeadingCalculation",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 52_000_000_000_000L
                            + " AND mysql_id < " + 53_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "LEGACY_PAYROLL_HEADING_CALC; offset 52e12"),
            new MigrationRowCountCheck(
                    "9v",
                    "payPeriodSpecificHeading → master_lookup",
                    "SELECT COUNT(*) FROM payPeriodSpecificHeading",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 53_000_000_000_000L
                            + " AND mysql_id < " + 54_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "LEGACY_PAY_PERIOD_HEADING; offset 53e12"),
            new MigrationRowCountCheck(
                    "9w",
                    "branchPayPeriod → master_lookup",
                    "SELECT COUNT(*) FROM branchPayPeriod",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 54_000_000_000_000L
                            + " AND mysql_id < " + 55_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "LEGACY_BRANCH_PAY_PERIOD; offset 54e12"),
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
                    "22e",
                    "employeeGrade → employee.grade_id",
                    "SELECT COUNT(*) FROM employeeGrade WHERE status = 1 AND endDate IS NULL",
                    "SELECT COUNT(*) FROM employee WHERE grade_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Open grades only; requires migrated employee/grade"),
            new MigrationRowCountCheck(
                    "22f",
                    "employeePayrollPaymentSetting → bank detail",
                    "SELECT COUNT(*) FROM employeePayrollPaymentSetting WHERE bank_id IS NOT NULL AND NULLIF(TRIM(institutionIdentity), '') IS NOT NULL",
                    "SELECT COUNT(*) FROM hrm_employee_bank_detail WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Requires migrated employee + master bank"),
            new MigrationRowCountCheck(
                    "22j",
                    "employeeExperience → hrm_experience",
                    "SELECT COUNT(*) FROM employeeExperience",
                    "SELECT COUNT(*) FROM hrm_experience WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips rows missing start/joinDate"),
            new MigrationRowCountCheck(
                    "22k",
                    "employeeAward → hrm_employee_award",
                    "SELECT COUNT(*) FROM employeeAward",
                    "SELECT COUNT(*) FROM hrm_employee_award WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips missing award/awardedBy"),
            new MigrationRowCountCheck(
                    "22l",
                    "employeeLanguage → hrm_employee_language",
                    "SELECT COUNT(*) FROM employeeLanguage",
                    "SELECT COUNT(*) FROM hrm_employee_language WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips missing language name"),
            new MigrationRowCountCheck(
                    "22m",
                    "employeeSeminar → hrm_employee_seminar",
                    "SELECT COUNT(*) FROM employeeSeminar",
                    "SELECT COUNT(*) FROM hrm_employee_seminar WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips missing seminar name"),
            new MigrationRowCountCheck(
                    "22n",
                    "employeePublication → hrm_employee_publication",
                    "SELECT COUNT(*) FROM employeePublication",
                    "SELECT COUNT(*) FROM hrm_employee_publication WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips missing publication name"),
            new MigrationRowCountCheck(
                    "22o",
                    "employeeHealth → hrm_employee_health",
                    "SELECT COUNT(*) FROM employeeHealth",
                    "SELECT COUNT(*) FROM hrm_employee_health WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Requires migrated employee"),
            new MigrationRowCountCheck(
                    "22p",
                    "employeeTraining → hrm_training",
                    "SELECT COUNT(*) FROM employeeTraining",
                    "SELECT COUNT(*) FROM hrm_training WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips missing name/startDate"),
            new MigrationRowCountCheck(
                    "22q",
                    "jobDescription → hrm_employee_job_description",
                    "SELECT COUNT(*) FROM jobDescription",
                    "SELECT COUNT(*) FROM hrm_employee_job_description WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips missing position"),
            new MigrationRowCountCheck(
                    "22r",
                    "employmentSuspension → hrm_employment_suspension",
                    "SELECT COUNT(*) FROM employmentSuspension",
                    "SELECT COUNT(*) FROM hrm_employment_suspension WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips missing fromDate"),
            new MigrationRowCountCheck(
                    "22s",
                    "employeeInsurance → hrm_employee_insurance",
                    "SELECT COUNT(*) FROM employeeInsurance",
                    "SELECT COUNT(*) FROM hrm_employee_insurance WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Requires migrated employee + insuranceCompany"),
            new MigrationRowCountCheck(
                    "22t",
                    "distinct employeeSkill → hrm_skill",
                    "SELECT COUNT(DISTINCT CONCAT(ce.company_id, ':', TRIM(es.skill))) FROM employeeSkill es JOIN companyEmployee ce ON ce.employee_id = es.employee_id WHERE NULLIF(TRIM(es.skill), '') IS NOT NULL",
                    "SELECT COUNT(*) FROM hrm_skill WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Upsert per company+name; PG ≤ distinct MySQL skills"),
            new MigrationRowCountCheck(
                    "22u",
                    "employeeSkill → hrm_employee_skill",
                    "SELECT COUNT(*) FROM employeeSkill",
                    "SELECT COUNT(*) FROM hrm_employee_skill WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Requires skill master + migrated employee"),
            new MigrationRowCountCheck(
                    "22v",
                    "jobTitle → hrm_employee_designation",
                    "SELECT COUNT(*) FROM jobTitle",
                    "SELECT COUNT(*) FROM hrm_employee_designation WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Primary branch only"),
            new MigrationRowCountCheck(
                    "22w",
                    "active employeeJob → employee.designation_id",
                    "SELECT COUNT(*) FROM employeeJob WHERE isactive = 'Y' OR enddate IS NULL",
                    "SELECT COUNT(*) FROM employee WHERE designation_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Requires migrated designation"),
            new MigrationRowCountCheck(
                    "22x",
                    "employeeContact emergency → hrm_employee_detail",
                    "SELECT COUNT(*) FROM employeeContact WHERE contactType = 'Emergency' OR NULLIF(TRIM(emergencyContactName), '') IS NOT NULL OR NULLIF(TRIM(emergencyContactPhone), '') IS NOT NULL OR NULLIF(TRIM(emergencyContactMobile), '') IS NOT NULL",
                    "SELECT COUNT(*) FROM hrm_employee_detail WHERE emergency_contact_name IS NOT NULL OR emergency_contact_phone IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Upsert per employee; PG ≤ emergency contacts"),
            new MigrationRowCountCheck(
                    "22y",
                    "employeeTermination → employee.termination_date",
                    "SELECT COUNT(*) FROM employeeTermination WHERE endDate IS NOT NULL",
                    "SELECT COUNT(*) FROM employee WHERE termination_date IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Only fills when employee.termination_date was null"),
            new MigrationRowCountCheck(
                    "22z",
                    "active branchDepartmentHead → employee.is_department_head",
                    "SELECT COUNT(*) FROM branchDepartmentHead WHERE endDate IS NULL",
                    "SELECT COUNT(*) FROM employee WHERE is_department_head = true",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Only active heads; skips unmigrated employees"),
            new MigrationRowCountCheck(
                    "22za",
                    "employeeJobLevel → employment history GRADE_CHANGE",
                    "SELECT COUNT(*) FROM employeeJobLevel",
                    "SELECT COUNT(*) FROM hrm_employee_employment_history WHERE mysql_id IS NOT NULL AND mysql_id < "
                            + 16_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "mysql_id = source id (below 16e12 offsets)"),
            new MigrationRowCountCheck(
                    "22zb",
                    "jobStatus → employment history EMPLOYMENT_TYPE_CHANGE",
                    "SELECT COUNT(*) FROM jobStatus",
                    "SELECT COUNT(*) FROM hrm_employee_employment_history WHERE mysql_id >= "
                            + 16_000_000_000_000L
                            + " AND mysql_id < "
                            + 17_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "mysql_id offset 16e12"),
            new MigrationRowCountCheck(
                    "22zc",
                    "jobPosition → hire_date/contracts",
                    "SELECT COUNT(*) FROM jobPosition",
                    "SELECT COUNT(*) FROM hrm_employee_contract WHERE mysql_id >= "
                            + 18_000_000_000_000L
                            + " AND mysql_id < "
                            + 19_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Contracts use mysql_id offset 18e12; hire_date fill-only"),
            new MigrationRowCountCheck(
                    "22zd",
                    "companyEmployeeContract → hrm_employee_contract",
                    "SELECT COUNT(*) FROM companyEmployeeContract",
                    "SELECT COUNT(*) FROM hrm_employee_contract WHERE mysql_id IS NOT NULL AND mysql_id < "
                            + 18_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "mysql_id = source id (below 18e12 jobPosition offset)"),
            new MigrationRowCountCheck(
                    "22ze",
                    "employeeJob → employment history DESIGNATION_CHANGE",
                    "SELECT COUNT(*) FROM employeeJob",
                    "SELECT COUNT(*) FROM hrm_employee_employment_history WHERE mysql_id >= "
                            + 17_000_000_000_000L
                            + " AND mysql_id < "
                            + 18_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "All rows incl closed; mysql_id offset 17e12"),
            new MigrationRowCountCheck(
                    "22zf",
                    "document → hrm_employee_document (metadata)",
                    "SELECT COUNT(*) FROM document",
                    "SELECT COUNT(*) FROM hrm_employee_document WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Metadata only; skips unmigrated employees"),
            new MigrationRowCountCheck(
                    "22zg",
                    "employeeProject → hrm_experience",
                    "SELECT COUNT(*) FROM employeeProject",
                    "SELECT COUNT(*) FROM hrm_experience WHERE mysql_id >= "
                            + 22_000_000_000_000L
                            + " AND mysql_id < "
                            + 23_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "mysql_id offset 22e12; skips blank name / missing employee"),
            new MigrationRowCountCheck(
                    "22zh",
                    "jobCategory → hrm_skill_category (fan-out)",
                    "SELECT COUNT(*) FROM jobCategory",
                    "SELECT COUNT(*) FROM hrm_skill_category WHERE mysql_id >= "
                            + 20_000_000_000_000L
                            + " AND mysql_id < "
                            + 21_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_LEAST_MYSQL,
                    "Fan-out: one row per category × migrated company; mysql_id offset 20e12"),
            new MigrationRowCountCheck(
                    "22zi",
                    "jobCategories → hrm_skill_category",
                    "SELECT COUNT(*) FROM jobCategories",
                    "SELECT COUNT(*) FROM hrm_skill_category WHERE mysql_id >= "
                            + 21_000_000_000_000L
                            + " AND mysql_id < "
                            + 22_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "mysql_id offset 21e12; skips blank jobName / unmigrated company"),
            new MigrationRowCountCheck(
                    "23",
                    "leaveAccumulation → leave_accumulation",
                    "SELECT COUNT(*) FROM leaveAccumulation",
                    "SELECT COUNT(*) FROM leave_accumulation WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips missing employee/leave/date"),
            new MigrationRowCountCheck(
                    "23l",
                    "leaveBalance → hrm_leave_balance",
                    "SELECT COUNT(*) FROM leaveBalance",
                    "SELECT COUNT(*) FROM hrm_leave_balance WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Collapsed to latest per employee+leave type; PG ≤ MySQL"),
            new MigrationRowCountCheck(
                    "23m",
                    "leaveAdjustment → hrm_leave_opening_adjustment (ADD/DEDUCT)",
                    "SELECT COUNT(*) FROM leaveAdjustment",
                    "SELECT COUNT(*) FROM hrm_leave_opening_adjustment WHERE action IN ('ADD','DEDUCT') AND mysql_id < "
                            + 9_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "SET_OPENING audits use mysql_id offset ≥ 9e12"),
            new MigrationRowCountCheck(
                    "23n",
                    "leaveApplication → hrm_leave",
                    "SELECT COUNT(*) FROM leaveApplication",
                    "SELECT COUNT(*) FROM hrm_leave WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Requires migrated employee + leave type"),
            new MigrationRowCountCheck(
                    "23o",
                    "leaveCancellation → hrm_leave.mysql_cancellation_id",
                    "SELECT COUNT(*) FROM leaveCancellation",
                    "SELECT COUNT(*) FROM hrm_leave WHERE mysql_cancellation_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Denied cancellations skipped; requires migrated leaveApplication"),
            new MigrationRowCountCheck(
                    "23p",
                    "calculatedAutoLeaveAccumulation → hrm_leave_credit",
                    "SELECT COUNT(*) FROM calculatedAutoLeaveAccumulation",
                    "SELECT COUNT(*) FROM hrm_leave_credit WHERE mysql_id IS NOT NULL AND mysql_id < "
                            + 11_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Historical LEAVE_ACCUMULATION credits; skips missing employee/branch/leave"),
            new MigrationRowCountCheck(
                    "23q",
                    "calculatedOTLeaveBalance → hrm_ot_leave_accrual_line",
                    "SELECT COUNT(*) FROM calculatedOTLeaveBalance",
                    "SELECT COUNT(*) FROM hrm_ot_leave_accrual_line WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "One line per OT balance; run grouped by company+till date"),
            new MigrationRowCountCheck(
                    "23a",
                    "employeePayrollHeading → hrm_employee_salary_component",
                    "SELECT COUNT(*) FROM employeePayrollHeading WHERE status = 1 AND endDate IS NULL",
                    "SELECT COUNT(*) FROM hrm_employee_salary_component WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Requires open employeePayrollDate + migrated breakdown"),
            new MigrationRowCountCheck(
                    "23b",
                    "openingPayrollBalance → opening lines",
                    "SELECT COUNT(*) FROM openingPayrollBalance",
                    "SELECT COUNT(*) FROM hrm_payroll_opening_balance_line WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips missing company/fy/employee"),
            new MigrationRowCountCheck(
                    "23c",
                    "employeePayrollPayment → month-wise salary",
                    "SELECT COUNT(*) FROM employeePayrollPayment WHERE status = 1",
                    "SELECT COUNT(*) FROM hrm_branch_employee_month_wise_salary WHERE mysql_id IS NOT NULL AND mysql_id < "
                            + 7_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "One row per employee+payPeriod; skips unmigrated employee/branch"),
            new MigrationRowCountCheck(
                    "23d",
                    "payrollTransaction → month-wise (PMS fill)",
                    "SELECT COUNT(DISTINCT CONCAT(employee_id, ':', payrollMonth_id)) FROM payrollTransaction",
                    "SELECT COUNT(*) FROM hrm_branch_employee_month_wise_salary WHERE mysql_id >= "
                            + 7_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Only months without modern payment row"),
            new MigrationRowCountCheck(
                    "23e",
                    "massIncrement → mass salary adjustment",
                    "SELECT COUNT(*) FROM massIncrement",
                    "SELECT COUNT(*) FROM hrm_mass_salary_adjustment WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Requires company/branch/grade/breakdown"),
            new MigrationRowCountCheck(
                    "23f",
                    "employeeLoan → loan account",
                    "SELECT COUNT(*) FROM employeeLoan",
                    "SELECT COUNT(*) FROM hrm_loan_account WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Requires migrated employee + branch company"),
            new MigrationRowCountCheck(
                    "23g",
                    "employeeLoanPayment → loan payment",
                    "SELECT COUNT(*) FROM employeeLoanPayment",
                    "SELECT COUNT(*) FROM hrm_loan_payment WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Requires migrated loan account"),
            new MigrationRowCountCheck(
                    "18a",
                    "attDeviceMAC → hrm_device_mac",
                    "SELECT COUNT(*) FROM attDeviceMAC",
                    "SELECT COUNT(*) FROM hrm_device_mac WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips missing company/branch or duplicate mac"),
            new MigrationRowCountCheck(
                    "18b",
                    "companySettingParams → master_lookup",
                    "SELECT COUNT(*) FROM companySettingParams",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 40_000_000_000_000L
                            + " AND mysql_id < " + 41_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "COMPANY_SETTING_PARAM; offset 40e12; known flags also enrich company"),
            new MigrationRowCountCheck(
                    "18c",
                    "companyAdminParams → master_lookup",
                    "SELECT COUNT(*) FROM companyAdminParams",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 41_000_000_000_000L
                            + " AND mysql_id < " + 42_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "COMPANY_ADMIN_PARAM; offset 41e12"),
            new MigrationRowCountCheck(
                    "18d",
                    "companyEmployeeParams → master_lookup",
                    "SELECT COUNT(*) FROM companyEmployeeParams",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 42_000_000_000_000L
                            + " AND mysql_id < " + 43_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "EMPLOYEE_SETTING_PARAM; offset 42e12"),
            new MigrationRowCountCheck(
                    "18e",
                    "employeeSummary → employee.notes (enrich)",
                    "SELECT COUNT(*) FROM employeeSummary",
                    "SELECT COUNT(*) FROM employee WHERE notes LIKE '%[migrated-summary:%'",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Enrich-only notes append with [migrated-summary:{id}] marker"),
            new MigrationRowCountCheck(
                    "22h",
                    "attEmpTempShift → temp shift",
                    "SELECT COUNT(*) FROM attEmpTempShift",
                    "SELECT COUNT(*) FROM hrm_employee_temp_shift WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Requires migrated employee/company/shift"),
            new MigrationRowCountCheck(
                    "23h",
                    "attLogs → attendance_log",
                    "SELECT COUNT(*) FROM attLogs WHERE isDeleted IS NULL OR isDeleted = 'N'",
                    "SELECT COUNT(*) FROM hrm_attendance_log WHERE mysql_id IS NOT NULL AND mysql_id < "
                            + 12_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips deleted / missing enroll employee; residual device logs use ≥12e12"),
            new MigrationRowCountCheck(
                    "23i",
                    "attendanceTransaction → attendance",
                    "SELECT COUNT(*) FROM attendanceTransaction",
                    "SELECT COUNT(*) FROM hrm_attendance WHERE mysql_id IS NOT NULL AND mysql_id < "
                            + 14_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Log shells may lack mysql_id; old archive uses ≥14e12"),
            new MigrationRowCountCheck(
                    "23j",
                    "attendanceForgot → time request",
                    "SELECT COUNT(*) FROM attendanceForgot",
                    "SELECT COUNT(*) FROM hrm_attendance_time_request WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Requires migrated employee/company"),
            new MigrationRowCountCheck(
                    "23r",
                    "deviceLogs → attendance_log (residual)",
                    "SELECT COUNT(*) FROM deviceLogs",
                    "SELECT COUNT(*) FROM hrm_attendance_log WHERE mysql_id >= " + 12_000_000_000_000L
                            + " AND mysql_id < " + 13_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Deduped against AttLogs enroll+datetime; offset 12e12"),
            new MigrationRowCountCheck(
                    "23s",
                    "tempDeviceLogs → attendance_log (residual)",
                    "SELECT COUNT(*) FROM tempDeviceLogs",
                    "SELECT COUNT(*) FROM hrm_attendance_log WHERE mysql_id >= " + 13_000_000_000_000L
                            + " AND mysql_id < " + 14_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Deduped against AttLogs/deviceLogs; offset 13e12"),
            new MigrationRowCountCheck(
                    "23t",
                    "oldAttendanceTransaction → attendance (fill-only)",
                    "SELECT COUNT(*) FROM oldAttendanceTransaction",
                    "SELECT COUNT(*) FROM hrm_attendance WHERE mysql_id >= " + 14_000_000_000_000L
                            + " AND mysql_id < " + 15_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips when employee+date already exists; offset 14e12"),
            new MigrationRowCountCheck(
                    "23u",
                    "workShift → hrm_roster_shift_slot",
                    "SELECT COUNT(*) FROM workShift",
                    "SELECT COUNT(*) FROM hrm_roster_shift_slot WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Fan-out to company branches; mysql_id only on primary branch"),
            new MigrationRowCountCheck(
                    "23v",
                    "editedOvertimeDetails → attendance (enrich)",
                    "SELECT COUNT(*) FROM editedOvertimeDetails",
                    "SELECT COUNT(*) FROM hrm_attendance WHERE overtime_manually_edited = true"
                            + " AND remarks LIKE '%[migrated-edited-ot:%'",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Enrich-only; marker [migrated-edited-ot:{id}]"),
            new MigrationRowCountCheck(
                    "23w",
                    "payrollSetting → master_lookup",
                    "SELECT COUNT(*) FROM payrollSetting",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 56_000_000_000_000L
                            + " AND mysql_id < " + 57_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "LEGACY_PAYROLL_SETTING; offset 56e12"),
            new MigrationRowCountCheck(
                    "23x",
                    "payrollOvertime → master_lookup",
                    "SELECT COUNT(*) FROM payrollOvertime",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 57_000_000_000_000L
                            + " AND mysql_id < " + 58_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "LEGACY_PAYROLL_OVERTIME; offset 57e12"),
            new MigrationRowCountCheck(
                    "23y",
                    "calculatedTypeValue → master_lookup",
                    "SELECT COUNT(*) FROM calculatedTypeValue",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 58_000_000_000_000L
                            + " AND mysql_id < " + 59_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "LEGACY_CALCULATED_TYPE_VALUE; offset 58e12"),
            new MigrationRowCountCheck(
                    "23z",
                    "payByOnlineTransaction → payment history",
                    "SELECT COUNT(*) FROM payByOnlineTransaction",
                    "SELECT COUNT(*) FROM subscription_payment_history WHERE mysql_id >= "
                            + 59_000_000_000_000L
                            + " OR remarks LIKE '%[migrated-pbo:%'",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Insert at 59e12 or enrich existing validity payment with [migrated-pbo:{id}]"),
            new MigrationRowCountCheck(
                    "24a",
                    "companyValidity → company_subscription + payment history",
                    "SELECT COUNT(*) FROM companyValidity",
                    "SELECT (SELECT COUNT(*) FROM company_subscription WHERE mysql_id IS NOT NULL)"
                            + " + (SELECT COUNT(*) FROM subscription_payment_history WHERE mysql_id >= "
                            + 24_000_000_000_000L
                            + " AND mysql_id < "
                            + 25_000_000_000_000L
                            + ")",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "One subscription per company (latest validTill); payments use mysql_id ≥ 24e12"),
            new MigrationRowCountCheck(
                    "24b",
                    "subscriptionPayment → subscription_payment_history",
                    "SELECT COUNT(*) FROM subscriptionPayment",
                    "SELECT COUNT(*) FROM subscription_payment_history WHERE mysql_id IS NOT NULL AND mysql_id < "
                            + 24_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Skips when company not migrated"),
            new MigrationRowCountCheck(
                    "24c",
                    "userLicense → subscription enrich + payment history",
                    "SELECT COUNT(*) FROM userLicense",
                    "SELECT (SELECT COUNT(*) FROM subscription_payment_history WHERE mysql_id >= "
                            + 35_000_000_000_000L
                            + " AND mysql_id < "
                            + 36_000_000_000_000L
                            + ") + (SELECT COUNT(*) FROM company_subscription WHERE mysql_id >= "
                            + 39_000_000_000_000L
                            + " AND mysql_id < "
                            + 40_000_000_000_000L
                            + ")",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Payments 35e12; new subs 39e12; may enrich existing validity subs without new row"),
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
                    "One detail per migrated user"),
            new MigrationRowCountCheck(
                    "27a",
                    "vacancy → hrm_recruitment_vacancy",
                    "SELECT COUNT(*) FROM vacancy",
                    "SELECT COUNT(*) FROM hrm_recruitment_vacancy WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Requires migrated company + branch"),
            new MigrationRowCountCheck(
                    "27b",
                    "vacancyNewspaper → vacancy_publication",
                    "SELECT COUNT(*) FROM vacancyNewspaper",
                    "SELECT COUNT(*) FROM hrm_recruitment_vacancy_publication WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Requires migrated vacancy"),
            new MigrationRowCountCheck(
                    "27c",
                    "stages → interview_stage",
                    "SELECT COUNT(*) FROM stages",
                    "SELECT COUNT(*) FROM hrm_recruitment_interview_stage WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    null),
            new MigrationRowCountCheck(
                    "27d",
                    "screeningQuestion → screening_question",
                    "SELECT COUNT(*) FROM screeningQuestion",
                    "SELECT COUNT(*) FROM hrm_recruitment_vacancy_screening_question WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    null),
            new MigrationRowCountCheck(
                    "27e",
                    "applicant → application (+ candidate ≥25e12)",
                    "SELECT COUNT(*) FROM applicant",
                    "SELECT COUNT(*) FROM hrm_recruitment_application WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Candidate mysql_id = 25e12 + employee; internal applicants only"),
            new MigrationRowCountCheck(
                    "27f",
                    "screeningAnswer → screening_answer",
                    "SELECT COUNT(*) FROM screeningAnswer",
                    "SELECT COUNT(*) FROM hrm_recruitment_application_screening_answer WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    null),
            new MigrationRowCountCheck(
                    "27g",
                    "applicantsTransaction → status_history",
                    "SELECT COUNT(*) FROM applicantsTransaction",
                    "SELECT COUNT(*) FROM hrm_recruitment_application_status_history WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    null),
            new MigrationRowCountCheck(
                    "27i",
                    "evaluation → screening evaluation",
                    "SELECT COUNT(*) FROM evaluation",
                    "SELECT COUNT(*) FROM hrm_recruitment_screening WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Requires migrated application"),
            new MigrationRowCountCheck(
                    "28a",
                    "notice → hrm_company_notice",
                    "SELECT COUNT(*) FROM notice",
                    "SELECT COUNT(*) FROM hrm_company_notice WHERE mysql_id >= " + 27_000_000_000_000L
                            + " AND mysql_id < " + 28_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "mysql_id offset 27e12"),
            new MigrationRowCountCheck(
                    "28b",
                    "message → hrm_company_notice",
                    "SELECT COUNT(*) FROM message",
                    "SELECT COUNT(*) FROM hrm_company_notice WHERE mysql_id >= " + 28_000_000_000_000L
                            + " AND mysql_id < " + 29_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "mysql_id offset 28e12"),
            new MigrationRowCountCheck(
                    "28c",
                    "companyMessageCompany → hrm_company_notice",
                    "SELECT COUNT(*) FROM companyMessageCompany",
                    "SELECT COUNT(*) FROM hrm_company_notice WHERE mysql_id >= " + 29_000_000_000_000L
                            + " AND mysql_id < " + 30_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "One notice per company×message junction; offset 29e12"),
            new MigrationRowCountCheck(
                    "28d",
                    "happening → hrm_company_notice",
                    "SELECT COUNT(*) FROM happening",
                    "SELECT COUNT(*) FROM hrm_company_notice WHERE mysql_id >= " + 30_000_000_000_000L
                            + " AND mysql_id < " + 31_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "mysql_id offset 30e12"),
            new MigrationRowCountCheck(
                    "28e",
                    "event → hrm_company_notice",
                    "SELECT COUNT(*) FROM event",
                    "SELECT COUNT(*) FROM hrm_company_notice WHERE mysql_id >= " + 31_000_000_000_000L
                            + " AND mysql_id < " + 32_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "mysql_id offset 31e12"),
            new MigrationRowCountCheck(
                    "28f",
                    "notification → hrm_company_notice",
                    "SELECT COUNT(*) FROM notification",
                    "SELECT COUNT(*) FROM hrm_company_notice WHERE mysql_id >= " + 32_000_000_000_000L
                            + " AND mysql_id < " + 33_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "mysql_id offset 32e12"),
            new MigrationRowCountCheck(
                    "28g",
                    "notificationViewed → hrm_employee_notice_read",
                    "SELECT COUNT(*) FROM notificationViewed",
                    "SELECT COUNT(*) FROM hrm_employee_notice_read WHERE mysql_id IS NOT NULL",
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "Requires migrated notification notice + user"),
            new MigrationRowCountCheck(
                    "29a",
                    "marketingPersonDetail → master_lookup",
                    "SELECT COUNT(*) FROM marketingPersonDetail",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 36_000_000_000_000L
                            + " AND mysql_id < " + 37_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "MARKETING_PERSON; skips blank fullname"),
            new MigrationRowCountCheck(
                    "29b",
                    "pricingEstimateEmailDetails → master_lookup",
                    "SELECT COUNT(*) FROM pricingEstimateEmailDetails",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 37_000_000_000_000L
                            + " AND mysql_id < " + 38_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "PRICING_ESTIMATE_LEAD; skips blank email"),
            new MigrationRowCountCheck(
                    "29c",
                    "applicationModule → master_lookup (reference)",
                    "SELECT COUNT(*) FROM applicationModule",
                    "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 38_000_000_000_000L
                            + " AND mysql_id < " + 39_000_000_000_000L,
                    MigrationComparisonMode.PG_AT_MOST_MYSQL,
                    "LEGACY_APP_MODULE reference only — ERP module tree stays seeded"));

    private static final Map<String, String> PIPELINE_PG_COUNT_SQL = new LinkedHashMap<>();

    static {
        PIPELINE_PG_COUNT_SQL.put("privilegeCount", "SELECT COUNT(*) FROM permission WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("roleCount", "SELECT COUNT(*) FROM role WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("companyCount", "SELECT COUNT(*) FROM company WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("organizationCount", "SELECT COUNT(*) FROM organization");
        PIPELINE_PG_COUNT_SQL.put("companyAddressCount", "SELECT COUNT(*) FROM hrm_company_address WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("branchCount", "SELECT COUNT(*) FROM branch WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("payrollRuleCount", "SELECT COUNT(*) FROM payroll_rule");
        PIPELINE_PG_COUNT_SQL.put(
                "salaryBreakdownCount",
                "SELECT COUNT(*) FROM hrm_branch_salary_breakdown WHERE mysql_id IS NOT NULL AND mysql_id < "
                        + 8_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "pmsSalaryBreakdownCount",
                "SELECT COUNT(*) FROM hrm_branch_salary_breakdown WHERE mysql_id >= " + 8_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put("gradeCount", "SELECT COUNT(*) FROM hrm_grade WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "gradePayStepCount", "SELECT COUNT(*) FROM hrm_grade_pay_step WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "gradeComponentValueCount",
                "SELECT COUNT(*) FROM hrm_grade_component_value WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("legacyBankCount", "SELECT COUNT(*) FROM bank WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "costTypePackageCount",
                "SELECT COUNT(*) FROM module_pricing_package WHERE mysql_id IS NOT NULL AND mysql_id < "
                        + 23_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "payPlanPackageCount",
                "SELECT COUNT(*) FROM module_pricing_package WHERE mysql_id >= " + 23_000_000_000_000L
                        + " AND mysql_id < " + 24_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "modulePricingPackageCount",
                "SELECT COUNT(*) FROM module_pricing_package WHERE mysql_id >= " + 26_000_000_000_000L
                        + " AND mysql_id < " + 27_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "payTypePackageCount",
                "SELECT COUNT(*) FROM module_pricing_package WHERE mysql_id >= " + 34_000_000_000_000L
                        + " AND mysql_id < " + 35_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put("leaveTypeCount", "SELECT COUNT(*) FROM hrm_leave_type WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("attTimeTableShiftCount", "SELECT COUNT(*) FROM hrm_branch_shift WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("branchHolidayCount", "SELECT COUNT(*) FROM hrm_branch_holiday WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("leaveAccumulationRuleCount",
                "SELECT COUNT(*) FROM hrm_branch_leave_accumulation_rule WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("departmentCount", "SELECT COUNT(*) FROM department WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("divisionCount", "SELECT COUNT(*) FROM hrm_division WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("costCenterCount", "SELECT COUNT(*) FROM hrm_cost_center WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("teamCount", "SELECT COUNT(*) FROM hrm_team WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "orgMasterHeadLinkCount",
                "SELECT COUNT(*) FROM hrm_team WHERE leader_employee_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "employeeOrgFkBackfillCount",
                "SELECT COUNT(*) FROM employee WHERE mysql_id IS NOT NULL AND ("
                        + "division_id IS NOT NULL OR team_id IS NOT NULL OR cost_center_id IS NOT NULL)");
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
        PIPELINE_PG_COUNT_SQL.put(
                "employeeBankDetailCount",
                "SELECT COUNT(*) FROM hrm_employee_bank_detail WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("employeeLeaveAccumulationCount",
                "SELECT COUNT(*) FROM leave_accumulation WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("leaveBalanceCount",
                "SELECT COUNT(*) FROM hrm_leave_balance WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("leaveAdjustmentCount",
                "SELECT COUNT(*) FROM hrm_leave_opening_adjustment WHERE action IN ('ADD','DEDUCT') AND mysql_id < "
                        + 9_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put("leaveApplicationCount",
                "SELECT COUNT(*) FROM hrm_leave WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("leaveCancellationCount",
                "SELECT COUNT(*) FROM hrm_leave WHERE mysql_cancellation_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "calculatedAutoLeaveCreditCount",
                "SELECT COUNT(*) FROM hrm_leave_credit WHERE mysql_id IS NOT NULL AND mysql_id < "
                        + 11_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "calculatedOtLeaveAccrualCount",
                "SELECT COUNT(*) FROM hrm_ot_leave_accrual_line WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("employeeSalaryCount",
                "SELECT COUNT(*) FROM hrm_employee_salary_component WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("payrollOpeningBalanceCount",
                "SELECT COUNT(*) FROM hrm_payroll_opening_balance_line WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("monthWiseSalaryCount",
                "SELECT COUNT(*) FROM hrm_branch_employee_month_wise_salary WHERE mysql_id IS NOT NULL AND mysql_id < "
                        + 7_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put("payrollTransactionHistoryCount",
                "SELECT COUNT(*) FROM hrm_branch_employee_month_wise_salary WHERE mysql_id >= "
                        + 7_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "massSalaryAdjustmentCount",
                "SELECT COUNT(*) FROM hrm_mass_salary_adjustment WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "employeeLoanCount", "SELECT COUNT(*) FROM hrm_loan_account WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "employeeLoanPaymentCount", "SELECT COUNT(*) FROM hrm_loan_payment WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("deviceMacCount", "SELECT COUNT(*) FROM hrm_device_mac WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "employeeDeviceEnrollCount",
                "SELECT COUNT(*) FROM hrm_employee_device_enroll WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "empTempShiftCount", "SELECT COUNT(*) FROM hrm_employee_temp_shift WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "attendanceLogCount",
                "SELECT COUNT(*) FROM hrm_attendance_log WHERE mysql_id IS NOT NULL AND mysql_id < "
                        + 12_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "attendanceTransactionCount",
                "SELECT COUNT(*) FROM hrm_attendance WHERE mysql_id IS NOT NULL AND mysql_id < "
                        + 14_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "attendanceForgotCount",
                "SELECT COUNT(*) FROM hrm_attendance_time_request WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "deviceLogsCount",
                "SELECT COUNT(*) FROM hrm_attendance_log WHERE mysql_id >= " + 12_000_000_000_000L
                        + " AND mysql_id < " + 13_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "tempDeviceLogsCount",
                "SELECT COUNT(*) FROM hrm_attendance_log WHERE mysql_id >= " + 13_000_000_000_000L
                        + " AND mysql_id < " + 14_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "oldAttendanceTransactionCount",
                "SELECT COUNT(*) FROM hrm_attendance WHERE mysql_id >= " + 14_000_000_000_000L
                        + " AND mysql_id < " + 15_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "workShiftCount", "SELECT COUNT(*) FROM hrm_roster_shift_slot WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "companyValiditySubscriptionCount",
                "SELECT (SELECT COUNT(*) FROM company_subscription WHERE mysql_id IS NOT NULL)"
                        + " + (SELECT COUNT(*) FROM subscription_payment_history WHERE mysql_id >= "
                        + 24_000_000_000_000L
                        + " AND mysql_id < "
                        + 25_000_000_000_000L
                        + ")");
        PIPELINE_PG_COUNT_SQL.put(
                "subscriptionPaymentHistoryCount",
                "SELECT COUNT(*) FROM subscription_payment_history WHERE mysql_id IS NOT NULL AND mysql_id < "
                        + 24_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "userLicenseSubscriptionCount",
                "SELECT (SELECT COUNT(*) FROM subscription_payment_history WHERE mysql_id >= "
                        + 35_000_000_000_000L
                        + " AND mysql_id < "
                        + 36_000_000_000_000L
                        + ") + (SELECT COUNT(*) FROM company_subscription WHERE mysql_id >= "
                        + 39_000_000_000_000L
                        + " AND mysql_id < "
                        + 40_000_000_000_000L
                        + ")");
        PIPELINE_PG_COUNT_SQL.put("userCount", "SELECT COUNT(*) FROM users WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put("userDetailCount", "SELECT COUNT(*) FROM user_detail");
        PIPELINE_PG_COUNT_SQL.put(
                "vacancyCount", "SELECT COUNT(*) FROM hrm_recruitment_vacancy WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "vacancyNewspaperCount",
                "SELECT COUNT(*) FROM hrm_recruitment_vacancy_publication WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "interviewStageCount",
                "SELECT COUNT(*) FROM hrm_recruitment_interview_stage WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "screeningQuestionCount",
                "SELECT COUNT(*) FROM hrm_recruitment_vacancy_screening_question WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "applicantCount", "SELECT COUNT(*) FROM hrm_recruitment_application WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "screeningAnswerCount",
                "SELECT COUNT(*) FROM hrm_recruitment_application_screening_answer WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "applicantsTransactionCount",
                "SELECT COUNT(*) FROM hrm_recruitment_application_status_history WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "recruitersCount",
                "SELECT COUNT(*) FROM hrm_recruitment_vacancy WHERE mysql_id IS NOT NULL AND recruiter_employee_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "evaluationCount", "SELECT COUNT(*) FROM hrm_recruitment_screening WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "noticeCount",
                "SELECT COUNT(*) FROM hrm_company_notice WHERE mysql_id >= " + 27_000_000_000_000L
                        + " AND mysql_id < " + 28_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "messageNoticeCount",
                "SELECT COUNT(*) FROM hrm_company_notice WHERE mysql_id >= " + 28_000_000_000_000L
                        + " AND mysql_id < " + 29_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "companyMessageNoticeCount",
                "SELECT COUNT(*) FROM hrm_company_notice WHERE mysql_id >= " + 29_000_000_000_000L
                        + " AND mysql_id < " + 30_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "happeningNoticeCount",
                "SELECT COUNT(*) FROM hrm_company_notice WHERE mysql_id >= " + 30_000_000_000_000L
                        + " AND mysql_id < " + 31_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "eventNoticeCount",
                "SELECT COUNT(*) FROM hrm_company_notice WHERE mysql_id >= " + 31_000_000_000_000L
                        + " AND mysql_id < " + 32_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "notificationNoticeCount",
                "SELECT COUNT(*) FROM hrm_company_notice WHERE mysql_id >= " + 32_000_000_000_000L
                        + " AND mysql_id < " + 33_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "notificationViewedCount",
                "SELECT COUNT(*) FROM hrm_employee_notice_read WHERE mysql_id IS NOT NULL");
        PIPELINE_PG_COUNT_SQL.put(
                "marketingPersonDetailCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 36_000_000_000_000L
                        + " AND mysql_id < " + 37_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "pricingEstimateEmailDetailsCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 37_000_000_000_000L
                        + " AND mysql_id < " + 38_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "applicationModuleLookupCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 38_000_000_000_000L
                        + " AND mysql_id < " + 39_000_000_000_000L);

        PIPELINE_PG_COUNT_SQL.put(
                "payrollInstitutionCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 44_000_000_000_000L
                        + " AND mysql_id < " + 45_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "companyPayrollBankCount",
                "SELECT COUNT(*) FROM hrm_company_bank WHERE mysql_id >= " + 45_000_000_000_000L
                        + " AND mysql_id < " + 46_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "companyPayrollInstitutionCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 46_000_000_000_000L
                        + " AND mysql_id < " + 47_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "parentPayrollHeadingCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 47_000_000_000_000L
                        + " AND mysql_id < " + 48_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "childPayrollHeadingCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 55_000_000_000_000L
                        + " AND mysql_id < " + 56_000_000_000_000L);
        // companyBranchPayrollHeadingCount omitted — enrich-only (no stable PG insert count)
        PIPELINE_PG_COUNT_SQL.put(
                "payrollLabelCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 48_000_000_000_000L
                        + " AND mysql_id < " + 49_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "payrollHeadingPriorityCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 49_000_000_000_000L
                        + " AND mysql_id < " + 50_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "payrollHeadingTemplateCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 50_000_000_000_000L
                        + " AND mysql_id < " + 51_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "payrollHeadingDateCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 51_000_000_000_000L
                        + " AND mysql_id < " + 52_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "payrollHeadingCalculationCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 52_000_000_000_000L
                        + " AND mysql_id < " + 53_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "payPeriodSpecificHeadingCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 53_000_000_000_000L
                        + " AND mysql_id < " + 54_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "branchPayPeriodCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 54_000_000_000_000L
                        + " AND mysql_id < " + 55_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "companySettingParamsCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 40_000_000_000_000L
                        + " AND mysql_id < " + 41_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "companyAdminParamsCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 41_000_000_000_000L
                        + " AND mysql_id < " + 42_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "companyEmployeeParamsCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 42_000_000_000_000L
                        + " AND mysql_id < " + 43_000_000_000_000L);
        // employeeSummaryCount omitted — enrich-only notes marker
        // editedOvertimeDetailsCount omitted — enrich-only attendance updates
        PIPELINE_PG_COUNT_SQL.put(
                "payrollSettingCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 56_000_000_000_000L
                        + " AND mysql_id < " + 57_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "payrollOvertimeCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 57_000_000_000_000L
                        + " AND mysql_id < " + 58_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "calculatedTypeValueCount",
                "SELECT COUNT(*) FROM master_lookup WHERE mysql_id >= " + 58_000_000_000_000L
                        + " AND mysql_id < " + 59_000_000_000_000L);
        PIPELINE_PG_COUNT_SQL.put(
                "payByOnlineTransactionCount",
                "SELECT COUNT(*) FROM subscription_payment_history WHERE mysql_id >= "
                        + 59_000_000_000_000L
                        + " OR remarks LIKE '%[migrated-pbo:%'");

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
