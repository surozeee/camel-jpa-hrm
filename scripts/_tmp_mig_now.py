from pathlib import Path
import re
import subprocess
import json

ps = r"""
Get-CimInstance Win32_Process | Where-Object {
  $_.CommandLine -and (
    $_.CommandLine -match 'CamelJpaTnApplication' -or
    ($_.CommandLine -match 'gradle-wrapper\.jar' -and $_.CommandLine -match 'bootRun') -or
    $_.CommandLine -match 'Camel-Jpa-TN-0.0.1-SNAPSHOT\.jar'
  )
} | Select-Object ProcessId, CommandLine | ConvertTo-Json -Depth 2
"""
raw = subprocess.check_output(["powershell", "-NoProfile", "-Command", ps], text=True, errors="replace")
data = json.loads(raw) if raw.strip() else []
if isinstance(data, dict):
    data = [data]
print("jvm_count", len(data))
for r in data:
    print("pid", r.get("ProcessId"), (r.get("CommandLine") or "")[:160])

for path in [
    Path(r"D:\Projects\camel-jpa-hrm\logs\migration-app.log"),
    Path(r"D:\Projects\camel-jpa-hrm\logs\migration-step-verify-20260909-105253.log"),
]:
    if not path.exists():
        print(path.name, "missing")
        continue
    text = path.read_text(encoding="utf-8", errors="replace")
    print("====", path.name, "size", len(text))
    for key in ["Migration mode", "Step-by-step", ">>> [", "<<< [", "QA:", "ERROR", "FAILED", "finished", "hrm_migration", "Started CamelJpa"]:
        hits = [ln for ln in text.splitlines() if key in ln]
        if hits:
            print(key, "=>", hits[-1][:220])
    print("TAIL:")
    for ln in text.splitlines()[-8:]:
        print(ln[:220])
