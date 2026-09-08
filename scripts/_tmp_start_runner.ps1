$ErrorActionPreference = 'Continue'
Set-Location 'D:\Projects\camel-jpa-hrm'

# Kill all SNAPSHOT jar / bootRun instances
Get-CimInstance Win32_Process | Where-Object {
  $_.CommandLine -and (
    $_.CommandLine -match 'Camel-Jpa-TN-0.0.1-SNAPSHOT\.jar' -or
    $_.CommandLine -match 'migration-runner\.jar' -or
    $_.CommandLine -match 'CamelJpaTnApplication' -or
    ($_.CommandLine -match 'gradle-wrapper\.jar' -and $_.CommandLine -match 'bootRun')
  )
} | ForEach-Object {
  Write-Output "killing $($_.ProcessId)"
  Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
}
Start-Sleep 2

Copy-Item 'build\libs\Camel-Jpa-TN-0.0.1-SNAPSHOT.jar' 'build\libs\migration-runner.jar' -Force

if (Test-Path 'logs\migration-app.log') {
  Move-Item 'logs\migration-app.log' ("logs\migration-app.prev-{0}.log" -f (Get-Date -Format 'HHmmss')) -Force -ErrorAction SilentlyContinue
}
Remove-Item 'logs\migration-from-start-ACTIVE.log','logs\migration-from-start-ACTIVE.err.log' -Force -ErrorAction SilentlyContinue

$env:MIGRATION_MODE = 'from'
$env:MIGRATION_FROM_STEP = 'employee-migration'
$env:MIGRATION_VERIFY_AFTER_EACH = 'true'
$env:MIGRATION_FAIL_ON_VERIFY = 'false'

$p = Start-Process -FilePath 'C:\Program Files\Java\jdk-21\bin\java.exe' `
  -ArgumentList @(
    '-Xmx6g',
    '-XX:+HeapDumpOnOutOfMemoryError',
    '-XX:HeapDumpPath=logs/heapdump.hprof',
    '-Dlogging.file.name=logs/migration-app.log',
    '-jar', 'build\libs\migration-runner.jar',
    '--spring.profiles.active=dev',
    '--server.port=9010'
  ) `
  -WorkingDirectory 'D:\Projects\camel-jpa-hrm' `
  -RedirectStandardOutput 'logs\migration-from-start-ACTIVE.log' `
  -RedirectStandardError 'logs\migration-from-start-ACTIVE.err.log' `
  -WindowStyle Hidden `
  -PassThru

@"
java_pid=$($p.Id)
from=employee-migration
jar=migration-runner.jar
port=9010
started=$(Get-Date)
"@ | Set-Content 'logs\migration-start-meta2.txt'

# Watchdog: kill SNAPSHOT.jar competitors only (leave migration-runner alone)
$wd = @"
`$our = $($p.Id)
while (`$true) {
  Start-Sleep 15
  if (-not (Get-Process -Id `$our -ErrorAction SilentlyContinue)) {
    Add-Content 'D:\Projects\camel-jpa-hrm\logs\migration-watchdog.txt' ("{0} our pid dead" -f (Get-Date))
    break
  }
  Get-CimInstance Win32_Process | Where-Object {
    `$_.ProcessId -ne `$our -and `$_.CommandLine -and (
      `$_.CommandLine -match 'Camel-Jpa-TN-0.0.1-SNAPSHOT\.jar' -or
      (`$_.CommandLine -match 'gradle-wrapper\.jar' -and `$_.CommandLine -match 'bootRun')
    )
  } | ForEach-Object {
    Stop-Process -Id `$_.ProcessId -Force -ErrorAction SilentlyContinue
    Add-Content 'D:\Projects\camel-jpa-hrm\logs\migration-watchdog.txt' ("{0} killed competitor {1}" -f (Get-Date), `$_.ProcessId)
  }
}
"@
$w = Start-Process powershell.exe -ArgumentList '-NoProfile','-Command',$wd -WindowStyle Hidden -PassThru
"watchdog_pid=$($w.Id)" | Add-Content 'logs\migration-start-meta2.txt'
Write-Output "java_pid=$($p.Id) watchdog=$($w.Id)"
