@echo off
cd /d D:\Projects\camel-jpa-hrm
set MIGRATION_MODE=from
set MIGRATION_FROM_STEP=attendance-forgot-migration
set MIGRATION_VERIFY_AFTER_EACH=true
set MIGRATION_CHAIN_ENABLED=false
java -Xmx2g -jar build\libs\Camel-Jpa-TN-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev > "D:\Projects\camel-jpa-hrm\logs\migration-from-attforgot-20260908-154714.log" 2>&1
echo EXIT=%ERRORLEVEL%>> "D:\Projects\camel-jpa-hrm\logs\migration-from-attforgot-20260908-154714.log"