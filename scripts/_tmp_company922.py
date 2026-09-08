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
)
pg = psycopg2.connect(
    host=p.group(1),
    port=int(p.group(2)),
    user=env["POSTGRES_USERNAME"],
    password=env["POSTGRES_PASSWORD"],
    dbname=p.group(3),
)
pg.autocommit = True
mc = my.cursor()
pc = pg.cursor()

mc.execute("SELECT id, HEX(name), LENGTH(name), REPLACE(name, CHAR(0), '') FROM company WHERE id=922")
print("mysql 922", mc.fetchone())
pc.execute("SELECT mysql_id, name FROM company WHERE lower(name) = lower(%s)", ("registerHere",))
print("pg registerHere", pc.fetchall())
pc.execute("SELECT COUNT(*) FROM company WHERE mysql_id = 922")
print("pg has 922", pc.fetchone()[0])
my.close()
pg.close()
