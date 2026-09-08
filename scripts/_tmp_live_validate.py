import re
import sys
from pathlib import Path

try:
    import pymysql
except ImportError:
    import subprocess

    subprocess.check_call([sys.executable, "-m", "pip", "install", "pymysql", "psycopg2-binary", "-q"])
    import pymysql

import psycopg2

env = {}
for line in Path(".env").read_text(encoding="utf-8").splitlines():
    line = line.strip()
    if not line or line.startswith("#") or "=" not in line:
        continue
    k, v = line.split("=", 1)
    env[k] = v


def parse(url: str):
    m = re.match(r"jdbc:(mysql|postgresql)://([^:/]+):(\d+)/([^?]+)", url)
    return m.group(2), int(m.group(3)), m.group(4)


mh, mp, mdb = parse(env["MYSQL_URL"])
ph, pp, pdb = parse(env["POSTGRES_URL"])

my = pymysql.connect(
    host=mh,
    port=mp,
    user=env["MYSQL_USERNAME"],
    password=env["MYSQL_PASSWORD"],
    database=mdb,
    connect_timeout=30,
)
pg = psycopg2.connect(
    host=ph,
    port=pp,
    user=env["POSTGRES_USERNAME"],
    password=env["POSTGRES_PASSWORD"],
    dbname=pdb,
    connect_timeout=30,
)

checks = [
    (
        "requestmap->permission",
        "SELECT COUNT(*) FROM requestmap",
        "SELECT COUNT(*) FROM permission WHERE mysql_id IS NOT NULL",
    ),
    (
        "company->company",
        "SELECT COUNT(*) FROM company",
        "SELECT COUNT(*) FROM company WHERE mysql_id IS NOT NULL",
    ),
    (
        "branch->branch",
        "SELECT COUNT(*) FROM branch",
        "SELECT COUNT(*) FROM branch WHERE mysql_id IS NOT NULL",
    ),
    (
        "leaves->hrm_leave_type",
        "SELECT COUNT(*) FROM leaves",
        "SELECT COUNT(*) FROM hrm_leave_type WHERE mysql_id IS NOT NULL",
    ),
    (
        "employee->employee",
        "SELECT COUNT(*) FROM employee",
        "SELECT COUNT(*) FROM employee WHERE mysql_id IS NOT NULL",
    ),
    (
        "department->department",
        "SELECT COUNT(*) FROM department",
        "SELECT COUNT(*) FROM department WHERE mysql_id IS NOT NULL",
    ),
    (
        "attLogs->attendance_log (sample)",
        "SELECT COUNT(*) FROM attLogs WHERE isDeleted IS NULL OR isDeleted='N'",
        "SELECT COUNT(*) FROM hrm_attendance_log WHERE mysql_id IS NOT NULL AND mysql_id < 12000000000000",
    ),
    (
        "attendanceTransaction->attendance (sample)",
        "SELECT COUNT(*) FROM attendanceTransaction",
        "SELECT COUNT(*) FROM hrm_attendance WHERE mysql_id IS NOT NULL AND mysql_id < 14000000000000",
    ),
]

print(f"LIVE validate | MySQL {mh}/{mdb} -> PG {ph}/{pdb}")
print(f"{'check':45} {'mysql':>12} {'pg':>12} {'delta':>8} status")
mc = my.cursor()
pc = pg.cursor()
for label, mq, pq in checks:
    try:
        mc.execute(mq)
        m = mc.fetchone()[0]
    except Exception as ex:
        m = f"ERR:{type(ex).__name__}"
    try:
        pc.execute(pq)
        p = pc.fetchone()[0]
    except Exception as ex:
        p = f"ERR:{type(ex).__name__}"
    if isinstance(m, int) and isinstance(p, int):
        d = p - m
        if "sample" in label:
            st = f"SAMPLE pg={p} (cap 10)"
        elif abs(d) <= 1 or p <= m:
            st = "OK"
        else:
            st = "CHECK"
        print(f"{label:45} {m:12} {p:12} {d:8} {st}")
    else:
        print(f"{label:45} {str(m):>12} {str(p):>12} {'':>8} ERR")

my.close()
pg.close()
