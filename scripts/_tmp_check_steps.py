import re
from pathlib import Path
t = Path(r"src/main/java/com/jojolaptech/camel/migration/MigrationSteps.java").read_text(encoding="utf-8")
steps = re.findall(r'"([a-z0-9-]+)"', t)
print("steps", len(steps))
print("attendance", [s for s in steps if "attendance" in s])
# class file string check
cls = Path(r"build/classes/java/main/com/jojolaptech/camel/migration/MigrationSteps.class")
print("class_exists", cls.exists(), "mtime", cls.stat().st_mtime if cls.exists() else None)
if cls.exists():
    data = cls.read_bytes()
    print("class_has_att_log", b"attendance-log-migration" in data)
    print("class_has_att_tx", b"attendance-transaction-migration" in data)
