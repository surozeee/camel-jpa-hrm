from pathlib import Path
import re

candidates = [
    Path(r"D:\Projects\camel-jpa-hrm\logs\migration-app.log"),
    Path(r"D:\Projects\camel-jpa-hrm\logs\migration-from-start-ACTIVE.log"),
]
for p in candidates:
    print("====", p.name, "exists", p.exists(), "size", p.stat().st_size if p.exists() else None)
    if not p.exists() or p.stat().st_size == 0:
        continue
    text = p.read_text(encoding="utf-8", errors="replace")
    for pat in [
        r"Migration mode=",
        r"Step-by-step migration starting",
        r"Started CamelJpaTnApplication",
        r">>> \[",
        r"<<< \[",
        r"ERROR",
        r"FAILED",
        r"APPLICATION FAILED",
        r"Caused by:",
        r"OutOfMemory",
        r"Address already in use",
        r"privilege-migration",
        r"BUILD ",
    ]:
        hits = re.findall(pat + r".*", text)
        if hits:
            print(f"-- {pat} ({len(hits)})")
            for h in hits[-8:]:
                print(h[:300])
    lines = [ln for ln in text.splitlines() if ln.strip()]
    print("-- TAIL --")
    for ln in lines[-30:]:
        print(ln[:300])
