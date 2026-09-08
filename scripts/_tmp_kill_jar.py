import json
import subprocess
import time

def procs():
    ps = r"""
Get-CimInstance Win32_Process | Where-Object {
  $_.CommandLine -and (
    $_.CommandLine -match 'CamelJpaTnApplication' -or
    $_.CommandLine -match 'Camel-Jpa-TN-0.0.1-SNAPSHOT\.jar' -or
    ($_.CommandLine -match 'gradle-wrapper\.jar' -and $_.CommandLine -match 'bootRun')
  )
} | Select-Object ProcessId, CommandLine | ConvertTo-Json -Depth 3
"""
    raw = subprocess.check_output(["powershell", "-NoProfile", "-Command", ps], text=True, errors="replace")
    data = json.loads(raw) if raw.strip() else []
    if isinstance(data, dict):
        data = [data]
    return data

rows = procs()
print("before", len(rows))
for r in rows:
    cmd = r.get("CommandLine") or ""
    pid = r.get("ProcessId")
    kind = "jar" if "SNAPSHOT.jar" in cmd else ("bootRun-wrapper" if "gradle-wrapper" in cmd else "app")
    print(kind, pid, cmd[:160])
    # Kill jar competitor; keep current bootRun app (we'll leave bootRun alone if healthy)
    if "SNAPSHOT.jar" in cmd:
        subprocess.run(["taskkill", "/F", "/T", "/PID", str(pid)], capture_output=True)
        print("killed_jar", pid)

time.sleep(1)
print("after", [(r.get("ProcessId"), (r.get("CommandLine") or "")[:80]) for r in procs()])
