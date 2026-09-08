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

print("=== MySQL tables ===")
for like in ("%dept%", "attLogs", "att_logs", "%attendance%"):
    mc.execute(f"SHOW TABLES LIKE '{like}'")
    print(like, mc.fetchall())

print("=== PG tables ===")
pc.execute(
    """
    SELECT tablename FROM pg_tables
    WHERE schemaname='public'
      AND (tablename LIKE '%dept%' OR tablename LIKE '%attendance%' OR tablename LIKE '%leave%')
    ORDER BY 1
    """
)
print(pc.fetchall())

checks = [
    ("permission", "SELECT COUNT(*) FROM requestmap", "SELECT COUNT(*) FROM permission WHERE mysql_id IS NOT NULL"),
    ("company", "SELECT COUNT(*) FROM company", "SELECT COUNT(*) FROM company WHERE mysql_id IS NOT NULL"),
    ("branch", "SELECT COUNT(*) FROM branch", "SELECT COUNT(*) FROM branch WHERE mysql_id IS NOT NULL"),
    ("leave_type", "SELECT COUNT(*) FROM leaves", "SELECT COUNT(*) FROM hrm_leave_type WHERE mysql_id IS NOT NULL"),
    ("employee", "SELECT COUNT(*) FROM employee", "SELECT COUNT(*) FROM employee WHERE mysql_id IS NOT NULL"),
    (
        "branch_department",
        "SELECT COUNT(*) FROM branchDepartment",
        "SELECT COUNT(*) FROM department WHERE mysql_id IS NOT NULL",
    ),
    (
        "attLogs",
        "SELECT COUNT(*) FROM attLogs WHERE COALESCE(isDeleted,'N') <> 'Y'",
        "SELECT COUNT(*) FROM hrm_attendance_log WHERE mysql_id IS NOT NULL AND mysql_id < 12000000000000",
    ),
    (
        "attendanceTransaction",
        "SELECT COUNT(*) FROM attendanceTransaction",
        "SELECT COUNT(*) FROM hrm_attendance WHERE mysql_id IS NOT NULL AND mysql_id < 14000000000000",
    ),
]

print()
print(f"{'check':25} {'mysql':>12} {'pg':>12} {'delta':>8} status")
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
        if label in ("attLogs", "attendanceTransaction"):
            st = f"SAMPLE pg={pv}"
        elif abs(d) <= 1 or pv <= mv:
            st = "OK"
        else:
            st = "CHECK"
        print(f"{label:25} {mv:12} {pv:12} {d:8} {st}")
    else:
        print(f"{label:25} {str(mv):>12} {str(pv):>12}          ERR")

my.close()
pg.close()
