import json
import subprocess
import time

ps = r"""
Get-CimInstance Win32_Process | Where-Object {
  $_.CommandLine -and (
    $_.CommandLine -match 'CamelJpaTnApplication' -or
    $_.CommandLine -match 'Camel-Jpa-TN-0.0.1-SNAPSHOT\.jar' -or
    $_.CommandLine -match 'gradle-wrapper\.jar' -or
    ($_.Name -eq 'java.exe' -and $_.CommandLine -match 'camel-jpa-hrm')
  )
} | Select-Object ProcessId, CommandLine | ConvertTo-Json -Depth 3
"""
raw = subprocess.check_output(["powershell", "-NoProfile", "-Command", ps], text=True, errors="replace")
data = json.loads(raw) if raw.strip() else []
if isinstance(data, dict):
    data = [data]
for r in data:
    cmd = r.get("CommandLine") or ""
    pid = r.get("ProcessId")
    print("kill", pid, cmd[:140])
    subprocess.run(["taskkill", "/F", "/T", "/PID", str(pid)], capture_output=True)
time.sleep(2)
print("done")
