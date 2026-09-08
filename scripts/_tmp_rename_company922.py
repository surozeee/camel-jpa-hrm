import re
from pathlib import Path

import psycopg2

env = {}
for line in Path(".env").read_text(encoding="utf-8").splitlines():
    line = line.strip()
    if not line or line.startswith("#") or "=" not in line:
        continue
    k, v = line.split("=", 1)
    env[k] = v

p = re.match(r"jdbc:postgresql://([^:/]+):(\d+)/([^?]+)", env["POSTGRES_URL"])
pg = psycopg2.connect(
    host=p.group(1),
    port=int(p.group(2)),
    user=env["POSTGRES_USERNAME"],
    password=env["POSTGRES_PASSWORD"],
    dbname=p.group(3),
)
c = pg.cursor()
c.execute("SELECT mysql_id, name FROM company WHERE mysql_id=922")
print("before", c.fetchone())

base = "registerHere"
suffix = 1
while True:
    candidate = f"{base}{suffix}"
    c.execute("SELECT 1 FROM company WHERE lower(name)=lower(%s) AND mysql_id <> 922", (candidate,))
    if not c.fetchone():
        break
    suffix += 1

c.execute("UPDATE company SET name=%s WHERE mysql_id=922", (candidate,))
pg.commit()
c.execute("SELECT mysql_id, name FROM company WHERE mysql_id=922")
print("after", c.fetchone())
pg.close()
