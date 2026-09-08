$ErrorActionPreference = 'Continue'
$env:MIGRATION_MODE = 'from'
$env:MIGRATION_FROM_STEP = 'emp-permanent-shift-migration'
$env:MIGRATION_VERIFY_AFTER_EACH = 'true'
Set-Location 'D:\Projects\camel-jpa-hrm'
# Prefer not being killed by string matchers: keep CamelJpa in cmdline unavoidably
& cmd.exe /c "gradlew.bat bootRun --args=--spring.profiles.active=dev --no-daemon > "D:\Projects\camel-jpa-hrm\logs\migration-PS-EXCLUSIVE-20260908-144234.log" 2>&1"
[System.IO.File]::WriteAllText('D:\Projects\camel-jpa-hrm\logs\migration-PS-EXCLUSIVE-exit.txt', "exit=$LASTEXITCODE finished=$(Get-Date -Format o)")
