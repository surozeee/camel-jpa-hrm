from pathlib import Path
p = Path(r"D:\Projects\camel-jpa-hrm\logs\migration-app.log")
lines = p.read_text(encoding="utf-8", errors="replace").splitlines()
print("total_lines", len(lines))
for ln in lines[-40:]:
    print(ln[:300])
