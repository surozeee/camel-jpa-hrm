import re
from pathlib import Path

import pymysql

env = {}
for line in Path(".env").read_text(encoding="utf-8").splitlines():
    line = line.strip()
    if not line or line.startswith("#") or "=" not in line:
        continue
    k, v = line.split("=", 1)
    env[k] = v

m = re.match(r"jdbc:mysql://([^:/]+):(\d+)/([^?]+)", env["MYSQL_URL"])
my = pymysql.connect(
    host=m.group(1),
    port=int(m.group(2)),
    user=env["MYSQL_USERNAME"],
    password=env["MYSQL_PASSWORD"],
    database=m.group(3),
    connect_timeout=60,
)
c = my.cursor()
since = "2026-01-01"
print(f"MySQL {m.group(1)}/{m.group(3)} | since {since}")

c.execute("SELECT COUNT(*) FROM att_logs WHERE check_time >= %s", (since,))
print(f"att_logs from_{since}:", c.fetchone()[0])

c.execute(
    """
    SELECT COUNT(*) FROM att_logs
    WHERE (is_deleted IS NULL OR is_deleted = 'N')
      AND check_time >= %s
    """,
    (since,),
)
print(f"att_logs migratable (not deleted) from_{since}:", c.fetchone()[0])

c.execute("SELECT COUNT(*) FROM att_logs")
print("att_logs total:", c.fetchone()[0])

c.execute(
    "SELECT COUNT(*) FROM attendance_transaction WHERE log_date >= %s",
    (since,),
)
print(f"attendance_transaction from_{since}:", c.fetchone()[0])

c.execute("SELECT COUNT(*) FROM attendance_transaction")
print("attendance_transaction total:", c.fetchone()[0])

my.close()
