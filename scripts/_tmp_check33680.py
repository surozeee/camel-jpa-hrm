from pathlib import Path
import subprocess

pid = 33680
try:
    out = subprocess.check_output(
        ["powershell", "-NoProfile", "-Command",
         f"(Get-Process -Id {pid}).Id; (Get-Process -Id {pid}).CPU; (Get-Process -Id {pid}).WorkingSet64"],
        text=True, errors="replace")
    print("alive", out.strip().replace("\n", " "))
except Exception:
    print("DEAD")

p = Path(r"D:\Projects\camel-jpa-hrm\logs\migration-app.log")
text = p.read_text(encoding="utf-8", errors="replace") if p.exists() else ""
print("size", len(text))
for key in ["of 143", "of 141", "Step-by-step", ">>> [", "<<< [", "ERROR", "Started CamelJpa", "employee-migration"]:
    for ln in text.splitlines():
        if key in ln:
            last = ln
    try:
        print(key, "=>", last[:200])
    except NameError:
        pass
    last = None
print("TAIL")
for ln in text.splitlines()[-12:]:
    print(ln[:220])

wd = Path(r"D:\Projects\camel-jpa-hrm\logs\migration-watchdog.txt")
if wd.exists():
    print("WATCHDOG")
    print(wd.read_text(encoding="utf-8", errors="replace")[-500:])
