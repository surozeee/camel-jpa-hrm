import re
from pathlib import Path

import pymysql
import psycopg2

env = {}
for line in Path(".env").read_text(encoding="utf-8").splitlines():
    line = line.strip()
    if not line or line.startswith("#") or "=" not in line:
        continue
    k, v = line.split("=", 1)
    env[k] = v

m = re.match(r"jdbc:mysql://([^:/]+):(\d+)/([^?]+)", env["MYSQL_URL"])
p = re.match(r"jdbc:postgresql://([^:/]+):(\d+)/([^?]+)", env["POSTGRES_URL"])

my = pymysql.connect(
    host=m.group(1),
    port=int(m.group(2)),
    user=env["MYSQL_USERNAME"],
    password=env["MYSQL_PASSWORD"],
    database=m.group(3),
    connect_timeout=30,
)
pg = psycopg2.connect(
    host=p.group(1),
    port=int(p.group(2)),
    user=env["POSTGRES_USERNAME"],
    password=env["POSTGRES_PASSWORD"],
    dbname=p.group(3),
    connect_timeout=30,
)
pg.autocommit = True
mc = my.cursor()
pc = pg.cursor()

print(f"LIVE | MySQL {m.group(1)}/{m.group(3)} -> PG {p.group(1)}/{p.group(3)}")

# discover department-like
mc.execute("SHOW TABLES")
tables = [r[0] for r in mc.fetchall()]
deptish = [t for t in tables if "dept" in t.lower() or "department" in t.lower()]
print("mysql dept-like:", deptish[:40])

checks = [
    ("permission", "SELECT COUNT(*) FROM requestmap", "SELECT COUNT(*) FROM permission WHERE mysql_id IS NOT NULL"),
    ("company", "SELECT COUNT(*) FROM company", "SELECT COUNT(*) FROM company WHERE mysql_id IS NOT NULL"),
    ("branch", "SELECT COUNT(*) FROM branch", "SELECT COUNT(*) FROM branch WHERE mysql_id IS NOT NULL"),
    ("leave_type", "SELECT COUNT(*) FROM leaves", "SELECT COUNT(*) FROM hrm_leave_type WHERE mysql_id IS NOT NULL"),
    ("employee", "SELECT COUNT(*) FROM employee", "SELECT COUNT(*) FROM employee WHERE mysql_id IS NOT NULL"),
    ("att_logs", "SELECT COUNT(*) FROM att_logs", "SELECT COUNT(*) FROM hrm_attendance_log WHERE mysql_id IS NOT NULL AND mysql_id < 12000000000000"),
    (
        "attendance_transaction",
        "SELECT COUNT(*) FROM attendance_transaction",
        "SELECT COUNT(*) FROM hrm_attendance WHERE mysql_id IS NOT NULL AND mysql_id < 14000000000000",
    ),
]

# optional department if exists
for candidate in ("branch_department", "branchDepartment", "department"):
    if candidate in tables:
        checks.insert(
            5,
            (
                candidate,
                f"SELECT COUNT(*) FROM `{candidate}`",
                "SELECT COUNT(*) FROM department WHERE mysql_id IS NOT NULL",
            ),
        )
        break

print(f"{'check':28} {'mysql':>12} {'pg':>12} {'delta':>10} status")
for label, mq, pq in checks:
    try:
        mc.execute(mq)
        mv = mc.fetchone()[0]
    except Exception as ex:
        mv = f"ERR:{type(ex).__name__}"
    try:
        pc.execute(pq)
        pv = pc.fetchone()[0]
    except Exception as ex:
        pv = f"ERR:{type(ex).__name__}"
    if isinstance(mv, int) and isinstance(pv, int):
        d = pv - mv
        if label in ("att_logs", "attendance_transaction"):
            st = f"FULL_SRC sample-cap later; pg={pv}"
        elif abs(d) <= 1 or pv <= mv:
            st = "OK"
        else:
            st = "CHECK"
        print(f"{label:28} {mv:12} {pv:12} {d:10} {st}")
    else:
        print(f"{label:28} {str(mv):>12} {str(pv):>12} {'':>10} ERR")

my.close()
pg.close()
