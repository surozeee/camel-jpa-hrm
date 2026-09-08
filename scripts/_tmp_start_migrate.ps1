$ErrorActionPreference = 'Continue'
Set-Location 'D:\Projects\camel-jpa-hrm'

Get-CimInstance Win32_Process | Where-Object {
  $_.CommandLine -and (
    $_.CommandLine -match 'CamelJpaTnApplication' -or
    ($_.CommandLine -match 'gradle-wrapper\.jar' -and $_.CommandLine -match 'bootRun') -or
    $_.CommandLine -match 'gradlew.*bootRun'
  )
} | ForEach-Object {
  Write-Output "killing $($_.ProcessId)"
  Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
}
Start-Sleep -Seconds 2

$fromStep = if ($env:RESTART_FROM) { $env:RESTART_FROM } else { 'employee-migration' }
$consoleLog = "D:\Projects\camel-jpa-hrm\logs\migration-from-start-ACTIVE.log"
$appLog = "D:\Projects\camel-jpa-hrm\logs\migration-app.log"
# Keep previous app log; rotate active console
if (Test-Path $consoleLog) {
  Copy-Item $consoleLog "D:\Projects\camel-jpa-hrm\logs\migration-from-start-ACTIVE.prev.log" -Force
  Remove-Item $consoleLog -Force
}
if (Test-Path $appLog) {
  Copy-Item $appLog "D:\Projects\camel-jpa-hrm\logs\migration-app.prev.log" -Force
  Remove-Item $appLog -Force
}

Set-Content -Path 'D:\Projects\camel-jpa-hrm\logs\migration-log-path-active.txt' -Value $appLog -Encoding UTF8

$env:MIGRATION_MODE = 'from'
$env:MIGRATION_FROM_STEP = $fromStep
$env:MIGRATION_VERIFY_AFTER_EACH = 'true'
$env:MIGRATION_FAIL_ON_VERIFY = 'false'
$env:JAVA_TOOL_OPTIONS = '-Xmx6g'

$bootArgs = "--spring.profiles.active=dev --logging.file.name=logs/migration-app.log"
$p = Start-Process -FilePath 'cmd.exe' `
  -ArgumentList '/c',("gradlew.bat bootRun --args=`"{0}`" --no-daemon > logs\migration-from-start-ACTIVE.log 2>&1" -f $bootArgs) `
  -WorkingDirectory 'D:\Projects\camel-jpa-hrm' `
  -WindowStyle Hidden `
  -PassThru

"cmd_pid=$($p.Id)" | Out-File 'D:\Projects\camel-jpa-hrm\logs\migration-start-meta2.txt' -Encoding utf8
"from=$fromStep" | Out-File 'D:\Projects\camel-jpa-hrm\logs\migration-start-meta2.txt' -Append -Encoding utf8
"started=$(Get-Date)" | Out-File 'D:\Projects\camel-jpa-hrm\logs\migration-start-meta2.txt' -Append -Encoding utf8
Write-Output "cmd_pid=$($p.Id) from=$fromStep"

