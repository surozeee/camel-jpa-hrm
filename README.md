# Camel JPA HRM importer (MySQL legacy → PostgreSQL ERP)

Spring Boot + Apache Camel pipeline that migrates legacy HRM master data from MySQL into the PostgreSQL ERP schema. The `master-import` timer route runs all 30 migration steps once on startup.

## Prerequisites
- Java 25 (or compatible toolchain)
- Gradle 8+
- MySQL legacy HRM database (source)
- PostgreSQL ERP database (target) with schema created by ERP services

## Configure

### Staging / dev (`.env`)

1. Copy `.env.example` to `.env` and point at staging databases:
   ```bash
   cp .env.example .env
   ```

2. Set `MYSQL_URL`, `POSTGRES_URL`, and credentials for the staging pair.

3. Run with the `dev` profile:
   ```bash
   gradlew bootRun --args='--spring.profiles.active=dev'
   ```

The app loads `.env` on startup (`CamelJpaTnApplication`).

### Local defaults

Edit `src/main/resources/application.yml` for local MySQL/PostgreSQL hosts.

## Run migration on staging (P0)

1. **Prepare target DB** — empty ERP schema or truncated migration tables on staging PostgreSQL.
2. **Start importer** — `gradlew bootRun --args='--spring.profiles.active=dev'`.
3. **Watch logs** — each step logs `Total records imported`; final block lists all 30 steps.
4. **Row-count QA** — runs automatically after step 26. Search logs for `Migration row-count QA report`.
   - Compares MySQL source counts vs PostgreSQL `mysql_id` rows.
   - Compares pipeline imported counts vs PostgreSQL totals.
   - Set `migration.qa.fail-on-mismatch=true` to abort on mismatch (default: log only).
5. **Manual spot-check** — `scripts/staging-row-count-qa.sql` has paired MySQL/PG queries.

### QA configuration

```yaml
migration:
  qa:
    enabled: true          # set false to skip post-migration QA
    fail-on-mismatch: false # set true on CI/staging gates
```

Exchange properties after QA: `migrationQaPassed`, `migrationQaFailureCount`.

## Pipeline overview

Timer `master-import` → steps 1–26 (+ 9a–9g payroll/grade/bank, 18a device, 22a–22zi profile/shift/PIMS/org leftovers, 23a–23u payroll/attendance/leave) → row-count QA → summary log.

See `ImportRouteBuilder.java` for the full route list.

### Payroll headings (new)

| Step | Source (MySQL) | Target (PostgreSQL) |
|------|----------------|---------------------|
| 9a | `companyPayrollHeading` + system/parent meta | `hrm_branch_salary_breakdown` |
| 9b | `payrollHeading` (PMS) | `hrm_branch_salary_breakdown` (mysql_id offset) |
| 9c | `jobLevel` | `hrm_grade` |
| 9d | `jobLevelGradeValue` | `hrm_grade_pay_step` |
| 9e | `jobLevelPayroll` | `hrm_grade_component_value` |
| 9f | active `templatePayrollHeading` × companies | seed missing breakdown lines (`migratedFromTemplate=…`) |
| 9g | `bank` | master `bank` |
| 22e | open `employeeGrade` | `employee.grade_id` |
| 22f | `employeePayrollPaymentSetting` (bank + account) | `hrm_employee_bank_detail` |
| 23a | open `employeePayrollHeading` + `employeePayrollDate` | `hrm_employee_salary` / `hrm_employee_salary_component` |
| 23b | `openingPayrollBalance` | `hrm_payroll_opening_balance` + lines |
| 23c | `employeePayrollPayment` + heading lines | `hrm_branch_employee_month_wise_salary` (+ jsonb snapshot) |
| 23d | `payrollTransaction` (PMS, by month) | month-wise salary when payment row missing |
| 23e | `massIncrement` | `hrm_mass_salary_adjustment` + line |
| 23f | `employeeLoan` | default policy + `hrm_loan_request` / `hrm_loan_account` |
| 23g | `employeeLoanPayment` | `hrm_loan_payment` |

### Attendance punch & emp-shift

| Step | Source (MySQL) | Target (PostgreSQL) |
|------|----------------|---------------------|
| 18a | `attDeviceMAC` | `hrm_device_mac` (mysql_id + unique mac) |
| 22g | migrated `employee.enrollId` | `hrm_employee_device_enroll` (first company device) |
| 22h | `attEmpTempShift` | `hrm_employee_temp_shift` |
| 22i | `attEmpShift` (latest per employee via `attShiftDetails` → timetable) | `employee.branch_shift_id` |
| 23h | `attLogs` (non-deleted) | `hrm_attendance_log` + day shell `hrm_attendance` |
| 23i | `attendanceTransaction` | upsert `hrm_attendance` (sets mysql_id) |
| 23j | `attendanceForgot` | `hrm_attendance_time_request` |
| 23k | `attendanceRemark` | append into existing `hrm_attendance.remarks` |
| 23r | `deviceLogs` | residual `hrm_attendance_log` (+ day shell); mysql_id ≥ 12e12; skips enroll+datetime already present |
| 23s | `tempDeviceLogs` | residual `hrm_attendance_log` (+ day shell); mysql_id ≥ 13e12; skips enroll+datetime already present |
| 23t | `oldAttendanceTransaction` | fill-only `hrm_attendance`; mysql_id ≥ 14e12; skips when employee+date exists |
| 23u | `workShift` | `hrm_roster_shift_slot` for all company branches; mysql_id ≥ 15e12 on primary branch only |

### Employee profile extensions (PIMS)

| Step | Source (MySQL) | Target (PostgreSQL) |
|------|----------------|---------------------|
| 22j | `employeeExperience` | `hrm_experience` |
| 22k | `employeeAward` | `hrm_employee_award` |
| 22l | `employeeLanguage` | `hrm_employee_language` |
| 22m | `employeeSeminar` | `hrm_employee_seminar` |
| 22n | `employeePublication` | `hrm_employee_publication` |
| 22o | `employeeHealth` | `hrm_employee_health` |
| 22p | `employeeTraining` | `hrm_training` |
| 22q | `jobDescription` | `hrm_employee_job_description` |
| 22r | `employmentSuspension` | `hrm_employment_suspension` |
| 22s | `employeeInsurance` (+ `insuranceCompany.name`) | `hrm_employee_insurance` |
| 22t | distinct `employeeSkill.skill` | `hrm_skill` (upsert per company+name) |
| 22u | `employeeSkill` | `hrm_employee_skill` |
| 22v | `jobTitle` | `hrm_employee_designation` (primary branch) |
| 22w | active `employeeJob` | `employee.designation_id` |
| 22x | emergency `employeeContact` | `hrm_employee_detail` emergency fields |
| 22y | `employeeTermination` | `employee.termination_date` (if null) |

### Org masters (derived)

There are **no** MySQL `Team` / `Division` / `CostCenter` tables. These ERP org masters are **derived** from already-migrated PostgreSQL data:

| Step | Derived from | Target |
|------|--------------|--------|
| 21a | Root departments (`parent_department_id` null) | `hrm_division` + `hrm_department.division_id` on subtree |
| 21b | Each migrated branch | `hrm_cost_center` (one per branch; `mysql_id` = branch.mysql_id) |
| 21c | Each department | `hrm_team` shell (`leader_employee_id` null) |
| 22zj | Active `branchDepartmentHead` | `hrm_division.head_employee_id` + `hrm_team.leader_employee_id` |
| 22zk | Migrated employees | `employee.division_id` / `team_id` / `cost_center_id` |

Stable synthetic keys: Division/Team `mysql_id = department.mysqlId * 1_000_000 + department.mysqlBranchId`. Org `hrm_cost_center` is derived from branches (21b) — **not** from MySQL `CostType` (SaaS billing; see 9h–9i / 24a–24b).

### SaaS billing (professional fit)

MySQL `CostType` is subscription pricing (billing cycle + discount), not an org cost center. Migrates into Master/User subscription tables:

| Step | Source (MySQL) | Target (PostgreSQL) |
|------|----------------|---------------------|
| 9h | `costType` | `module_pricing_package` (`LEGACY-CT-{id}`; price 0; billing cycle from `subscription`) |
| 9i | `payPlan` | `module_pricing_package` (priced packages; mysql_id ≥ 23e12) |
| 9j | `modulePricing` | `module_pricing_package` (user-tier amounts; mysql_id ≥ 26e12; **no** `module_pricing_scope`) |
| 24a | `companyValidity` | `company_subscription` (latest `validTill` per company) + `subscription_payment_history` (mysql_id ≥ 24e12) |
| 24b | `subscriptionPayment` | `subscription_payment_history` |

**Module scopes / tree from ERP:** `module`, `modulesList`, `applicationModule`, `payPlanModule`, and ERP `module_pricing_scope` are **not** migrated — provision via Master-Service seed. Legacy app-module name is kept as package `features` text only on 9j.

Still out of scope: marketing/pricing-estimate emails.

### Org structure leftovers

JobTitle → designation (22v), active employeeJob → designation_id (22w), and grades (9c–9e / 22e) are covered earlier. Global `jobCategory` / company `jobCategories` migrate to skill categories in 22zh–22zi.

| Step | Source (MySQL) | Target (PostgreSQL) |
|------|----------------|---------------------|
| 22z | active `branchDepartmentHead` | `employee.is_department_head=true` |
| 22za | `employeeJobLevel` | `hrm_employee_employment_history` (`GRADE_CHANGE`) |
| 22zb | `jobStatus` | `hrm_employee_employment_history` (`EMPLOYMENT_TYPE_CHANGE`; mysql_id ≥ 16e12) |
| 22zc | `jobPosition` | fill `employee.hire_date` if null + `hrm_employee_contract` (mysql_id ≥ 18e12) |
| 22zd | `companyEmployeeContract` | `hrm_employee_contract` |
| 22ze | all `employeeJob` (incl closed) | `hrm_employee_employment_history` (`DESIGNATION_CHANGE`; mysql_id ≥ 17e12) |

### PIMS leftovers (professional fit)

Metadata and taxonomy fits that map into existing ERP tables (no dedicated MySQL→ERP 1:1 for these leftovers).

| Step | Source (MySQL) | Target (PostgreSQL) |
|------|----------------|---------------------|
| 22zf | `document` | `hrm_employee_document` (metadata only; `imageUrl` = path or `migrated://mysql-document/{id}` placeholder — **binary/MinIO files are not copied**) |
| 22zg | `employeeProject` | `hrm_experience` (designation=`Project`; mysql_id ≥ 22e12 to avoid collide with 22j) |
| 22zh | `jobCategory` | `hrm_skill_category` (**fan-out** one row per migrated company; mysql_id ≥ 20e12) |
| 22zi | `jobCategories` | `hrm_skill_category` (company-scoped; mysql_id ≥ 21e12) |

Out of scope (not migrated): `EmployeeBranch` / `BranchDepartment` (step 22), standalone `Insurance` entity (provider denormalized in 22s), SaaS module tree/scopes (`module` / `applicationModule` / `payPlanModule` / `module_pricing_scope` — ERP seed), `userRating`, extra recruiters beyond first, PreEmployment/Joining templates (no MySQL ATS source — company config seed).

### Recruitment / ATS (professional fit)

Legacy ATS maps into ERP **`hrm_recruitment_*`** (not empty pre-employment checklist templates).

| Step | Source (MySQL) | Target (PostgreSQL) |
|------|----------------|---------------------|
| 27a | `vacancy` | `hrm_recruitment_vacancy` (default branch = company first branch) |
| 27b | `vacancyNewspaper` | `hrm_recruitment_vacancy_publication` (`NEWSPAPER`; clipping URL only) |
| 27c | `stages` | `hrm_recruitment_interview_stage` |
| 27d | `screeningQuestion` | `hrm_recruitment_vacancy_screening_question` (MCQ A–D) |
| 27e | `applicant` | `hrm_recruitment_candidate` (mysql_id ≥ 25e12 = employee) + `hrm_recruitment_application` |
| 27f | `screeningAnswer` | `hrm_recruitment_application_screening_answer` |
| 27g | `applicantsTransaction` | `hrm_recruitment_application_status_history` |
| 27h | `recruiters` | patch `vacancy.recruiter_employee_id` (first wins) |
| 27i | `evaluation` | `hrm_recruitment_screening` |

### Leave applications & balances

| Step | Source (MySQL) | Target (PostgreSQL) |
|------|----------------|---------------------|
| 23l | `leaveBalance` (latest per employee+leave) | `hrm_leave_balance` + SET_OPENING row in `hrm_leave_opening_adjustment` |
| 23m | `leaveAdjustment` | `hrm_leave_opening_adjustment` (ADD/DEDUCT) + update balance totals |
| 23n | `leaveApplication` | `hrm_leave` |
| 23o | `leaveCancellation` | update `hrm_leave.leaveStatus` (+ `mysql_cancellation_id`) |

### Leave calculated runs

Historical accrual/credit audit only — does **not** re-apply credits to leave balances (already migrated in 23l).

| Step | Source (MySQL) | Target (PostgreSQL) |
|------|----------------|---------------------|
| 23p | `calculatedAutoLeaveAccumulation` | `hrm_leave_credit` (`LEAVE_ACCUMULATION`, `MIGRATE-AUTO-ACC-{id}`) |
| 23q | `calculatedOTLeaveBalance` | `hrm_ot_leave_accrual_run` + `hrm_ot_leave_accrual_line` + OT `hrm_leave_credit` (`MIGRATE-OT-ACC-{id}`); seeds `hrm_ot_leave_accrual_rule` per branch when missing |

## Adjustments
- Page size: `PAGE_SIZE` in `ImportRouteBuilder` (default 100).
- Step throttle: `MIGRATION_THROTTLE_MS` between steps.
