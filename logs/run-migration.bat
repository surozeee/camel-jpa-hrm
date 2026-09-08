cd /d D:\Projects\camel-jpa-hrm
set MIGRATION_MODE=from
set MIGRATION_FROM_STEP=emp-permanent-shift-migration
set MIGRATION_VERIFY_AFTER_EACH=true
set JAVA_TOOL_OPTIONS=-Xmx2g
gradlew.bat bootRun --args=--spring.profiles.active=dev --no-daemon > "D:\Projects\camel-jpa-hrm\logs\migration-from-emp-permanent-shift-20260908-144122.log" 2>&1