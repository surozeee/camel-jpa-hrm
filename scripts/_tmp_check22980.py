from pathlib import Path
import re
import subprocess

pid = 22980
try:
    subprocess.check_output(["powershell", "-NoProfile", "-Command", f"Get-Process -Id {pid} | Out-Null"], text=True)
    print("ALIVE", pid)
except Exception:
    print("DEAD", pid)

p = Path(r"D:\Projects\camel-jpa-hrm\logs\migration-app.log")
text = p.read_text(encoding="utf-8", errors="replace") if p.exists() else ""
print("size", len(text))
for pat in [r"of \d+", r">>> \[", r"<<< \[", r"ERROR", r"FAILED", r"Step-by-step"]:
    hits = re.findall(pat + r".*", text)
    if hits:
        print(pat, "=>", hits[-1][:200])
print("TAIL")
for ln in text.splitlines()[-8:]:
    print(ln[:220])
wd = Path(r"D:\Projects\camel-jpa-hrm\logs\migration-watchdog.txt")
if wd.exists():
    print("WD", wd.read_text(encoding="utf-8", errors="replace")[-300:])
