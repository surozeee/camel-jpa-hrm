import json
import os
import subprocess
import time
import zipfile
from datetime import datetime
from pathlib import Path

ROOT = Path(r"D:\Projects\camel-jpa-hrm")
JAR = ROOT / "build" / "libs" / "Camel-Jpa-TN-0.0.1-SNAPSHOT.jar"


def kill_app_only():
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
    for r in data:
        pid = r.get("ProcessId")
        print("killing", pid, (r.get("CommandLine") or "")[:140])
        subprocess.run(["taskkill", "/F", "/T", "/PID", str(pid)], capture_output=True)


print("=== kill running apps ===")
kill_app_only()
time.sleep(2)

print("=== bootJar ===")
r = subprocess.run(
    ["cmd", "/c", "gradlew.bat bootJar --rerun-tasks --no-daemon"],
    cwd=str(ROOT),
    capture_output=True,
    text=True,
    errors="replace",
)
print((r.stdout or "")[-800:])
print((r.stderr or "")[-400:])
if r.returncode != 0:
    raise SystemExit(f"bootJar failed: {r.returncode}")

with zipfile.ZipFile(JAR) as z:
    names = [n for n in z.namelist() if n.endswith("MigrationSteps.class") or n.endswith("ImportRouteBuilder.class")]
    print("class_entries", names)
    ms = next(n for n in names if n.endswith("MigrationSteps.class"))
    irb_name = next(n for n in names if n.endswith("ImportRouteBuilder.class"))
    data = z.read(ms)
    irb = z.read(irb_name)
print("jar_has_att_log", b"attendance-log-migration" in data)
print("jar_has_att_tx", b"attendance-transaction-migration" in data)
print("jar_has_sample_limit", b"ATTENDANCE_SAMPLE_LIMIT" in irb)

print("=== kill again before start ===")
kill_app_only()
time.sleep(1)

logs = ROOT / "logs"
app_log = logs / "migration-app.log"
console = logs / "migration-from-start-ACTIVE.log"
if app_log.exists():
    app_log.rename(logs / f"migration-app.prev-{datetime.now().strftime('%H%M%S')}.log")
if console.exists():
    try:
        console.unlink()
    except Exception:
        pass

env = os.environ.copy()
env["MIGRATION_MODE"] = "from"
env["MIGRATION_FROM_STEP"] = "employee-migration"
env["MIGRATION_VERIFY_AFTER_EACH"] = "true"
env["MIGRATION_FAIL_ON_VERIFY"] = "false"

# Use Start-Process via powershell for reliable detached start with env
ps_start = f"""
$env:MIGRATION_MODE='from'
$env:MIGRATION_FROM_STEP='employee-migration'
$env:MIGRATION_VERIFY_AFTER_EACH='true'
$env:MIGRATION_FAIL_ON_VERIFY='false'
$p = Start-Process -FilePath 'C:\\Program Files\\Java\\jdk-21\\bin\\java.exe' `
  -ArgumentList '-Xmx6g','-Dlogging.file.name=logs/migration-app.log','-jar','{JAR}','--spring.profiles.active=dev' `
  -WorkingDirectory '{ROOT}' `
  -RedirectStandardOutput 'logs\\migration-from-start-ACTIVE.log' `
  -RedirectStandardError 'logs\\migration-from-start-ACTIVE.err.log' `
  -WindowStyle Hidden `
  -PassThru
Write-Output $p.Id
"""
out = subprocess.check_output(["powershell", "-NoProfile", "-Command", ps_start], text=True, errors="replace")
print("java_pid", out.strip())
(logs / "migration-start-meta2.txt").write_text(
    f"java_pid={out.strip()}\nfrom=employee-migration\njar={JAR}\nstarted={datetime.now()}\n",
    encoding="utf-8",
)
time.sleep(2)
print("done")
