import json
import subprocess
from pathlib import Path

ps = r"""
$procs = Get-CimInstance Win32_Process | Where-Object {
  $_.CommandLine -and (
    $_.CommandLine -match 'bootRun|gradle-wrapper|CamelJpaTnApplication|camel-jpa-hrm'
  )
}
$procs | Select-Object ProcessId, Name, CreationDate, CommandLine | ConvertTo-Json -Depth 3
"""
raw = subprocess.check_output(["powershell", "-NoProfile", "-Command", ps], text=True, errors="replace")
data = json.loads(raw) if raw.strip() else []
if isinstance(data, dict):
    data = [data]
print("count", len(data))
for r in data:
    cmd = (r.get("CommandLine") or "").replace("\n", " ")
    print("---")
    print("pid", r.get("ProcessId"), "name", r.get("Name"), "created", r.get("CreationDate"))
    print(cmd[:300])

lock_dir = Path(r"D:\Projects\camel-jpa-hrm\.gradle")
for p in lock_dir.rglob("*.lock"):
    print("lock", p, "size", p.stat().st_size)

log = Path(r"D:\Projects\camel-jpa-hrm\logs\migration-log-path-active.txt").read_text().strip()
lp = Path(log)
print("log_size", lp.stat().st_size if lp.exists() else None)
