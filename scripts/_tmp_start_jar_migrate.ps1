$ErrorActionPreference = 'Continue'
Set-Location 'D:\Projects\camel-jpa-hrm'

# Kill competing app instances (not gradle builds)
Get-CimInstance Win32_Process | Where-Object {
  $_.CommandLine -and (
    $_.CommandLine -match 'Camel-Jpa-TN-0.0.1-SNAPSHOT\.jar' -or
    $_.CommandLine -match 'CamelJpaTnApplication' -or
    ($_.CommandLine -match 'gradle-wrapper\.jar' -and $_.CommandLine -match 'bootRun')
  )
} | ForEach-Object {
  Write-Output "killing $($_.ProcessId)"
  Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
}
Start-Sleep 2

if (Test-Path 'logs\migration-app.log') {
  Move-Item 'logs\migration-app.log' ("logs\migration-app.prev-{0}.log" -f (Get-Date -Format 'HHmmss')) -Force
}
if (Test-Path 'logs\migration-from-start-ACTIVE.log') { Remove-Item 'logs\migration-from-start-ACTIVE.log' -Force -ErrorAction SilentlyContinue }
if (Test-Path 'logs\migration-from-start-ACTIVE.err.log') { Remove-Item 'logs\migration-from-start-ACTIVE.err.log' -Force -ErrorAction SilentlyContinue }

$env:MIGRATION_MODE = 'from'
$env:MIGRATION_FROM_STEP = 'employee-migration'
$env:MIGRATION_VERIFY_AFTER_EACH = 'true'
$env:MIGRATION_FAIL_ON_VERIFY = 'false'

$p = Start-Process -FilePath 'C:\Program Files\Java\jdk-21\bin\java.exe' `
  -ArgumentList @(
    '-Xmx6g',
    '-Dlogging.file.name=logs/migration-app.log',
    '-jar', 'build\libs\Camel-Jpa-TN-0.0.1-SNAPSHOT.jar',
    '--spring.profiles.active=dev',
    '--server.port=9010'
  ) `
  -WorkingDirectory 'D:\Projects\camel-jpa-hrm' `
  -RedirectStandardOutput 'logs\migration-from-start-ACTIVE.log' `
  -RedirectStandardError 'logs\migration-from-start-ACTIVE.err.log' `
  -WindowStyle Hidden `
  -PassThru

"java_pid=$($p.Id)" | Set-Content 'logs\migration-start-meta2.txt'
"from=employee-migration port=9010" | Add-Content 'logs\migration-start-meta2.txt'
"started=$(Get-Date)" | Add-Content 'logs\migration-start-meta2.txt'
Write-Output "java_pid=$($p.Id)"

# Watchdog: every 20s kill foreign -Xmx2g jar copies that are NOT our pid
$wd = @"
`$our = $($p.Id)
while (`$true) {
  Start-Sleep 20
  if (-not (Get-Process -Id `$our -ErrorAction SilentlyContinue)) { break }
  Get-CimInstance Win32_Process | Where-Object {
    `$_.ProcessId -ne `$our -and `$_.CommandLine -and (
      `$_.CommandLine -match 'Camel-Jpa-TN-0.0.1-SNAPSHOT\.jar' -or
      (`$_.CommandLine -match 'gradle-wrapper\.jar' -and `$_.CommandLine -match 'bootRun')
    )
  } | ForEach-Object {
    Stop-Process -Id `$_.ProcessId -Force -ErrorAction SilentlyContinue
    Add-Content -Path 'D:\Projects\camel-jpa-hrm\logs\migration-watchdog.txt' -Value ("{0} killed competitor {1}" -f (Get-Date), `$_.ProcessId)
  }
}
"@
$w = Start-Process -FilePath 'powershell.exe' -ArgumentList '-NoProfile','-Command', $wd -WindowStyle Hidden -PassThru
"watchdog_pid=$($w.Id)" | Add-Content 'logs\migration-start-meta2.txt'
Write-Output "watchdog_pid=$($w.Id)"
