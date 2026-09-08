from pathlib import Path
import re
import subprocess

pid = 27220
try:
    out = subprocess.check_output(
        ["powershell", "-NoProfile", "-Command", f"Get-Process -Id {pid} -ErrorAction Stop | Select-Object Id,CPU,WorkingSet64 | Format-List | Out-String"],
        text=True,
        errors="replace",
    )
    print("proc", out.strip())
except Exception as e:
    print("proc DEAD", e)

p = Path(r"D:\Projects\camel-jpa-hrm\logs\migration-app.log")
text = p.read_text(encoding="utf-8", errors="replace") if p.exists() else ""
print("size", p.stat().st_size if p.exists() else -1)
for pat in ["Migration mode", "Step-by-step", "of 143", "of 141", ">>> [", "<<< [", "ERROR", "FAILED", "Started CamelJpa"]:
    hits = [ln for ln in text.splitlines() if pat in ln]
    if hits:
        print(pat, "->", hits[-1][:220])
print("TAIL:")
for ln in text.splitlines()[-15:]:
    print(ln[:220])
