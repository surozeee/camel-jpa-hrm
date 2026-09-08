from pathlib import Path
import re

p = Path(r"D:\Projects\camel-jpa-hrm\logs\migration-app.log")
text = p.read_text(encoding="utf-8", errors="replace") if p.exists() else ""
print("size", p.stat().st_size if p.exists() else -1)

completed = re.findall(r"<<< \[(\d+)/(\d+)\] Completed ([^ ]+)", text)
failed = re.findall(r"<<< \[(\d+)/(\d+)\] FAILED ([^ ]+).*", text)
running = re.findall(r">>> \[(\d+)/(\d+)\] Running ([^ ]+)", text)
errors = re.findall(r".*(ERROR|APPLICATION FAILED|OutOfMemory|Caused by:).*", text)
qa_fail = re.findall(r".*QA: .*FAIL.*", text)
finished = "Step-by-step migration finished" in text

print("completed_count", len(completed))
if completed:
    print("last_completed", completed[-1])
if running:
    print("last_started", running[-1])
print("failed_count", len(failed))
for f in failed[-10:]:
    print("FAILED", f)
print("qa_fail_count", len(qa_fail))
for q in qa_fail[-10:]:
    print(q[:250])
print("error_lines", len(errors))
for e in errors[-15:]:
    print(e[:300])
print("finished", finished)

# process check
import subprocess
raw = ""
try:
    ps = "Get-Process -Id 17900 -ErrorAction SilentlyContinue | Select-Object Id,CPU,WorkingSet64 | ConvertTo-Json"
    raw = subprocess.check_output(["powershell","-NoProfile","-Command",ps], text=True, errors="replace").strip()
except Exception:
    raw = ""
print("proc17900", raw[:200] if raw else "DEAD/unknown")

# any CamelJpa process?
ps2 = r"""
Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -match 'CamelJpaTnApplication' } |
  Select-Object -ExpandProperty ProcessId
"""
try:
    pids = subprocess.check_output(["powershell","-NoProfile","-Command",ps2], text=True, errors="replace").strip()
    print("camel_pids", pids.replace("\n", ",") if pids else "none")
except Exception as e:
    print("camel_pids_err", e)
