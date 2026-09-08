import re
from pathlib import Path

# Extract only the ALL list contents between ALL = List.of( and );
text = Path("src/main/java/com/jojolaptech/camel/migration/MigrationSteps.java").read_text(encoding="utf-8")
m = re.search(r"ALL = List\.of\((.*?)\);", text, re.S)
body = m.group(1)
steps = re.findall(r'"([a-z0-9-]+)"', body)
print("ALL_count", len(steps))
# find employee index
idx = steps.index("employee-migration")
print("from_employee_count", len(steps) - idx)
print("has_att_log", "attendance-log-migration" in steps)
print("has_att_tx", "attendance-transaction-migration" in steps)
# around attendance
for i, s in enumerate(steps):
    if "attendance" in s or "loan-payment" in s or "employee-loan" in s:
        print(i, s)

# class constant pool strings via strings-like
cls = Path("build/classes/java/main/com/jojolaptech/camel/migration/MigrationSteps.class").read_bytes()
# crude extract utf8 strings
strs = []
i = 0
while i < len(cls) - 2:
    if cls[i] == 1:  # CONSTANT_Utf8
        ln = int.from_bytes(cls[i+1:i+3], "big")
        try:
            s = cls[i+3:i+3+ln].decode("utf-8")
            if s.startswith("attendance") or s.endswith("-migration"):
                strs.append(s)
        except Exception:
            pass
        i += 3 + ln
    else:
        i += 1
att = [s for s in strs if "attendance" in s]
print("class_att_strings", sorted(set(att)))
mig = [s for s in strs if s.endswith("-migration")]
print("class_migration_count_approx", len(set(mig)))
