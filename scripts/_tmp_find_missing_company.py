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

mc.execute("SELECT id, name FROM company ORDER BY id")
mysql_rows = mc.fetchall()
pc.execute("SELECT mysql_id, name FROM company WHERE mysql_id IS NOT NULL ORDER BY mysql_id")
pg_rows = {r[0]: r[1] for r in pc.fetchall()}

missing = [(i, n) for i, n in mysql_rows if i not in pg_rows]
print(f"mysql={len(mysql_rows)} pg={len(pg_rows)} missing={len(missing)}")
for i, n in missing:
    print(f"MISSING mysql_id={i} name={n!r}")
    # check if same name exists in pg under another mysql_id
    pc.execute(
        "SELECT mysql_id, name FROM company WHERE lower(name)=lower(%s)",
        (n,),
    )
    dup = pc.fetchall()
    print(f"  PG name matches: {dup}")

# also blank names
mc.execute("SELECT id, name FROM company WHERE name IS NULL OR TRIM(name)=''")
print("blank names:", mc.fetchall())

my.close()
pg.close()
