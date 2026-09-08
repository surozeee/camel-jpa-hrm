@echo off
cd /d D:\Projects\camel-jpa-hrm
for /f "usebackq tokens=1,* delims==" %%a in (".env") do set "%%a=%%b"
set MIGRATION_MODE=from
set MIGRATION_FROM_STEP=attendance-forgot-migration
set MIGRATION_VERIFY_AFTER_EACH=true
set MIGRATION_ONLY_STEP=
"C:\Program Files\Java\jdk-21\bin\java.exe" -Xmx2g -jar build\libs\Camel-Jpa-TN-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev > "D:\Projects\camel-jpa-hrm\logs\migration-PS-JAR-from-attforgot-20260908-155229.log" 2>&1
echo EXIT=%ERRORLEVEL% > logs\migration-PS-JAR-exit.txt
