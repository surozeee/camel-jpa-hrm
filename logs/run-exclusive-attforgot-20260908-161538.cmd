@echo off
cd /d D:\Projects\camel-jpa-hrm
for /f "usebackq eol=# tokens=1,* delims==" %%a in (".env") do (
  if not "%%a"=="" if not "%%b"=="" set "%%a=%%b"
)
set MIGRATION_MODE=from
set MIGRATION_FROM_STEP=attendance-forgot-migration
set MIGRATION_VERIFY_AFTER_EACH=true
set MIGRATION_ONLY_STEP=
echo owner=exclusive-attforgot > logs\MIGRATION_LOCK_exclusive-attforgot.txt
echo from=attendance-forgot-migration>> logs\MIGRATION_LOCK_exclusive-attforgot.txt
echo jar=Camel-Jpa-TN-0.0.1-SNAPSHOT.jar>> logs\MIGRATION_LOCK_exclusive-attforgot.txt
echo log=logs\migration-exclusive-20260908-161538.log>> logs\MIGRATION_LOCK_exclusive-attforgot.txt
echo applog=logs\migration-app-exclusive-20260908-161538.log>> logs\MIGRATION_LOCK_exclusive-attforgot.txt
echo STARTED %DATE% %TIME% > logs\exclusive-attforgot-meta.txt
"C:\Program Files\Java\jdk-21\bin\java.exe" -Xmx2g -Dlogging.file.name=logs\migration-app-exclusive-20260908-161538.log -jar build\libs\Camel-Jpa-TN-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev > "logs\migration-exclusive-20260908-161538.log" 2>&1
echo EXIT=%ERRORLEVEL% %DATE% %TIME%>> logs\exclusive-attforgot-meta.txt
