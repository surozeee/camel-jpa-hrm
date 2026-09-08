import json
import re
import subprocess
from pathlib import Path

ps = r"""
Get-CimInstance Win32_Process | Where-Object {
  $_.CommandLine -and (
    $_.CommandLine -match 'CamelJpaTnApplication' -or
    ($_.CommandLine -match 'gradle-wrapper\.jar' -and $_.CommandLine -match 'bootRun')
  )
} | Select-Object ProcessId, Name, CommandLine | ConvertTo-Json -Depth 3
"""
raw = subprocess.check_output(["powershell", "-NoProfile", "-Command", ps], text=True, errors="replace")
data = json.loads(raw) if raw.strip() else []
if isinstance(data, dict):
    data = [data]
print("jvm_count", len(data))
for r in data:
    print("pid", r.get("ProcessId"), (r.get("CommandLine") or "")[:180])

p = Path(r"D:\Projects\camel-jpa-hrm\logs\migration-app.log")
text = p.read_text(encoding="utf-8", errors="replace")
print("log_size", p.stat().st_size)
print("finished", "Step-by-step migration finished" in text)
failed = re.findall(r"<<< \[\d+/\d+\] FAILED .*", text)
print("failed", failed[-5:])
errors = [ln for ln in text.splitlines() if " ERROR " in ln or "APPLICATION FAILED" in ln or "OutOfMemory" in ln]
print("errors", len(errors))
for e in errors[-10:]:
    print(e[:280])
completed = re.findall(r"<<< \[(\d+)/(\d+)\] Completed ([^ ]+)", text)
running = re.findall(r">>> \[(\d+)/(\d+)\] Running ([^\n]+)", text)
print("last_completed", completed[-1] if completed else None)
print("last_started", running[-1] if running else None)
print("tail:")
for ln in text.splitlines()[-20:]:
    print(ln[:280])
