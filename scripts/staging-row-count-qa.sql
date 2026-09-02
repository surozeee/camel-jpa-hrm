-- Staging row-count QA (manual)
-- Run MySQL queries against legacy DB and PostgreSQL queries against ERP DB.
-- Automated equivalent runs at end of master migration (MigrationRowCountQaService).

-- Core 1:1 mappings
-- MySQL                          | PostgreSQL
SELECT COUNT(*) FROM requestmap;   -- SELECT COUNT(*) FROM permission WHERE mysql_id IS NOT NULL;
SELECT COUNT(*) FROM secRole;      -- SELECT COUNT(*) FROM role WHERE mysql_id IS NOT NULL;
SELECT COUNT(*) FROM company;      -- SELECT COUNT(*) FROM company WHERE mysql_id IS NOT NULL;
SELECT COUNT(*) FROM company;      -- SELECT COUNT(*) FROM organization;
SELECT COUNT(*) FROM branch;       -- SELECT COUNT(*) FROM branch WHERE mysql_id IS NOT NULL;
SELECT COUNT(*) FROM employee;     -- SELECT COUNT(*) FROM employee WHERE mysql_id IS NOT NULL;
SELECT COUNT(*) FROM secUser;      -- SELECT COUNT(*) FROM users WHERE mysql_id IS NOT NULL;

-- Employee profile sub-entities
SELECT COUNT(*) FROM employeeAddress;
-- SELECT COUNT(*) FROM address WHERE employee_id IS NOT NULL AND mysql_id IS NOT NULL AND mysql_id < 9000000000000;

SELECT COUNT(*) FROM employee WHERE NULLIF(TRIM(permanentAdd), '') IS NOT NULL;
SELECT COUNT(*) FROM employee WHERE NULLIF(TRIM(temperoryAdd), '') IS NOT NULL;
-- SELECT COUNT(*) FROM address WHERE mysql_id >= 9000000000000;

SELECT COUNT(*) FROM employeeEducation;
-- SELECT COUNT(*) FROM hrm_employee_education WHERE mysql_id IS NOT NULL;

SELECT COUNT(*) FROM family;
-- SELECT COUNT(*) FROM employee_family_detail WHERE mysql_id IS NOT NULL;

-- Leave balances
SELECT COUNT(*) FROM leaveAccumulation;
-- SELECT COUNT(*) FROM leave_accumulation WHERE mysql_id IS NOT NULL;

-- Idempotency spot-check (PostgreSQL): no duplicate legacy keys
-- SELECT mysql_id, COUNT(*) FROM employee GROUP BY mysql_id HAVING COUNT(*) > 1;
-- SELECT mysql_id, COUNT(*) FROM permission GROUP BY mysql_id HAVING COUNT(*) > 1;
