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
)
c = my.cursor()
for like in ("%sec%role%", "%role%", "address"):
    c.execute(f"SHOW TABLES LIKE '{like}'")
    print(like, c.fetchall()[:20])
for t in ("secRole", "sec_role", "role"):
    try:
        c.execute(f"SELECT COUNT(*) FROM `{t}`")
        print(t, c.fetchone()[0])
    except Exception as e:
        print(t, type(e).__name__, e.args[:1])
my.close()
