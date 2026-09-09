$ErrorActionPreference = 'Continue'
Set-Location 'D:\Projects\camel-jpa-hrm'

# Kill leftover migration JVMs only
Get-CimInstance Win32_Process | Where-Object {
  $_.CommandLine -and (
    $_.CommandLine -match 'CamelJpaTnApplication' -or
    $_.CommandLine -match 'Camel-Jpa-TN-0.0.1-SNAPSHOT\.jar' -or
    $_.CommandLine -match 'migration-runner\.jar' -or
    ($_.CommandLine -match 'gradle-wrapper\.jar' -and $_.CommandLine -match 'bootRun')
  )
} | ForEach-Object {
  Write-Output "killing $($_.ProcessId)"
  Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
}
Start-Sleep 2

# Prefer .env controls; override only if missing
if (-not $env:MIGRATION_MODE) { $env:MIGRATION_MODE = 'from' }
if (-not $env:MIGRATION_FROM_STEP) { $env:MIGRATION_FROM_STEP = 'attendance-forgot-migration' }
if (-not $env:MIGRATION_VERIFY_AFTER_EACH) { $env:MIGRATION_VERIFY_AFTER_EACH = 'true' }
$env:MIGRATION_FAIL_ON_VERIFY = 'false'
$env:JAVA_TOOL_OPTIONS = '-Xmx6g'

$ts = Get-Date -Format 'yyyyMMdd-HHmmss'
$appLog = "logs\migration-app.log"
$console = "logs\migration-step-verify-$ts.log"
if (Test-Path $appLog) {
  Move-Item $appLog ("logs\migration-app.prev-{0}.log" -f (Get-Date -Format 'HHmmss')) -Force -ErrorAction SilentlyContinue
}

Set-Content -Path 'logs\migration-log-path-active.txt' -Value ((Resolve-Path .).Path + '\' + $appLog.Replace('/','\')) -Encoding UTF8

$bootArgs = "--spring.profiles.active=dev --logging.file.name=logs/migration-app.log"
$p = Start-Process -FilePath 'cmd.exe' `
  -ArgumentList '/c',("gradlew.bat bootRun --args=`"{0}`" --no-daemon > {1} 2>&1" -f $bootArgs, $console) `
  -WorkingDirectory 'D:\Projects\camel-jpa-hrm' `
  -WindowStyle Hidden `
  -PassThru

@"
cmd_pid=$($p.Id)
mode=$($env:MIGRATION_MODE)
from=$($env:MIGRATION_FROM_STEP)
verify=$($env:MIGRATION_VERIFY_AFTER_EACH)
appLog=$appLog
console=$console
started=$(Get-Date)
"@ | Set-Content 'logs\migration-start-meta2.txt' -Encoding UTF8

Write-Output "cmd_pid=$($p.Id)"
Write-Output "from=$($env:MIGRATION_FROM_STEP)"
Write-Output "console=$console"
Write-Output "appLog=$appLog"
