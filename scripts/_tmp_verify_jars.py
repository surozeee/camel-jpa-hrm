import zipfile
from pathlib import Path

for name in ["Camel-Jpa-TN-0.0.1-SNAPSHOT.jar", "migration-runner.jar"]:
    p = Path("build/libs") / name
    if not p.exists():
        print(name, "missing")
        continue
    with zipfile.ZipFile(p) as z:
        ms = next(n for n in z.namelist() if n.endswith("MigrationSteps.class"))
        data = z.read(ms)
        irb = next(n for n in z.namelist() if n.endswith("ImportRouteBuilder.class"))
        irbd = z.read(irb)
    print(name)
    print("  att_log", b"attendance-log-migration" in data)
    print("  att_tx", b"attendance-transaction-migration" in data)
    print("  sample", b"ATTENDANCE_SAMPLE_LIMIT" in irbd)
    # count migration step-like strings
    import re
    # utf8 strings containing -migration
    found = set()
    i = 0
    while i < len(data) - 3:
        if data[i:i+1] == b"\x01":
            ln = int.from_bytes(data[i+1:i+3], "big")
            raw = data[i+3:i+3+ln]
            try:
                s = raw.decode("utf-8")
                if s.endswith("-migration"):
                    found.add(s)
            except Exception:
                pass
            i += 3 + ln
        else:
            i += 1
    print("  utf8_migration_strings", len(found))
    att = sorted(s for s in found if "attendance" in s)
    print("  attendance", att)
