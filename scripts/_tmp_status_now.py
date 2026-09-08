from pathlib import Path
import re
import subprocess

pid = 33680
try:
    out = subprocess.check_output(
        ["powershell", "-NoProfile", "-Command",
         f"(Get-Process -Id {pid} -ErrorAction Stop | Select-Object Id,CPU,WorkingSet64 | Format-List | Out-String)"],
        text=True, errors="replace")
    print("ALIVE")
    print(out.strip())
except Exception:
    print("DEAD")

p = Path(r"D:\Projects\camel-jpa-hrm\logs\migration-app.log")
text = p.read_text(encoding="utf-8", errors="replace") if p.exists() else ""
print("size", p.stat().st_size if p.exists() else -1)
completed = re.findall(r"<<< \[(\d+)/(\d+)\] Completed ([^ ]+)", text)
failed = re.findall(r"<<< \[(\d+)/(\d+)\] FAILED ([^ ]+)", text)
running = re.findall(r">>> \[(\d+)/(\d+)\] Running ([^\n]+)", text)
print("completed", len(completed), "last", completed[-1] if completed else None)
print("started_last", running[-1] if running else None)
print("failed", failed)
errs = [ln for ln in text.splitlines() if " ERROR " in ln]
print("errors", len(errs))
for e in errs[-5:]:
    print(e[:220])
print("finished", "migration finished" in text)
wd = Path(r"D:\Projects\camel-jpa-hrm\logs\migration-watchdog.txt")
if wd.exists():
    print("watchdog:\n", wd.read_text(encoding="utf-8", errors="replace")[-400:])

import psycopg2
c = psycopg2.connect(host="165.22.209.54", port=5432, dbname="hrmsuite", user="Wl457oGknvD1Dsyk", password="Brvqg7E1e2ISwXKPGGB")
cur = c.cursor()
for t in ["employee", "company", "branch"]:
    cur.execute(f"SELECT COUNT(*) FROM {t}")
    print(t, cur.fetchone()[0])
c.close()
