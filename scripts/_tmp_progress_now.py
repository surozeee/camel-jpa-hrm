from pathlib import Path
import re
import subprocess
import json
import psycopg2

ps = r"""
Get-Process -Id 24364 -ErrorAction SilentlyContinue | Select-Object Id,CPU,WorkingSet64 | ConvertTo-Json
"""
raw = subprocess.check_output(["powershell", "-NoProfile", "-Command", ps], text=True, errors="replace").strip()
print("proc", raw[:200] if raw else "DEAD")

p = Path(r"D:\Projects\camel-jpa-hrm\logs\migration-app.log")
text = p.read_text(encoding="utf-8", errors="replace") if p.exists() else ""
print("size", len(text))
completed = re.findall(r"<<< \[(\d+)/(\d+)\] Completed ([^ ]+)", text)
failed = re.findall(r"<<< \[(\d+)/(\d+)\] FAILED ([^ ]+)", text)
started = re.findall(r">>> \[(\d+)/(\d+)\] Running ([^\n]+)", text)
qa_fail = [ln for ln in text.splitlines() if "QA:" in ln and "FAIL" in ln]
qa_pass = [ln for ln in text.splitlines() if "QA: PASS" in ln]
errs = [ln for ln in text.splitlines() if " ERROR " in ln or "APPLICATION FAILED" in ln]
print("completed", len(completed), "last", completed[-1] if completed else None)
print("started_last", started[-1] if started else None)
print("failed", failed[-5:])
print("qa_pass", len(qa_pass), "qa_fail", len(qa_fail))
for q in qa_fail[-5:]:
    print(q[:220])
print("errors", len(errs))
for e in errs[-5:]:
    print(e[:220])
print("finished", "migration finished" in text)
# last pages
pages = [ln for ln in text.splitlines() if "attendance-forgot-migration page=" in ln]
if pages:
    print("last_page", pages[-1][:200])

c = psycopg2.connect(host="165.22.209.54", port=5432, dbname="hrm_migration",
                     user="Wl457oGknvD1Dsyk", password="Brvqg7E1e2ISwXKPGGB")
cur = c.cursor()
for t in ["employee", "users", "hrm_attendance_log", "hrm_attendance", "hrm_attendance_time_request"]:
    try:
        cur.execute(f"SELECT COUNT(*) FROM {t}")
        print(t, cur.fetchone()[0])
    except Exception as e:
        c.rollback()
        print(t, "ERR", str(e).splitlines()[0][:60])
c.close()
