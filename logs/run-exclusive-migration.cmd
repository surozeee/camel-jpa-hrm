@echo off
cd /d D:\Projects\camel-jpa-hrm
for /f "usebackq eol=# tokens=1,* delims==" %%a in (".env") do (
  if not "%%a"=="" if not "%%b"=="" set "%%a=%%b"
)
set MIGRATION_MODE=from
set MIGRATION_FROM_STEP=attendance-forgot-migration
set MIGRATION_VERIFY_AFTER_EACH=true
set MIGRATION_ONLY_STEP=
echo STARTED %DATE% %TIME% > logs\migration-start-meta2.txt
"C:\Program Files\Java\jdk-21\bin\java.exe" -Xmx2g -jar build\libs\Camel-Jpa-TN-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev > "logs\migration-exclusive-20260908-155830.log" 2>&1
echo EXIT=%ERRORLEVEL% %DATE% %TIME%>> logs\migration-start-meta2.txt
