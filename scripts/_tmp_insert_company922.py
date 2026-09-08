"""Insert missing MySQL company 922 into live PG as 'registerHere (C922)'."""
import re
import uuid
from datetime import datetime, timezone
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
pc = pg.cursor()
mc = my.cursor()

pc.execute("SELECT COUNT(*) FROM company WHERE mysql_id = 922")
if pc.fetchone()[0] > 0:
    print("already present mysql_id=922")
    raise SystemExit(0)

mc.execute(
    "SELECT id, REPLACE(name, CHAR(0), ''), phone, url, logo, is_archive FROM company WHERE id=922"
)
row = mc.fetchone()
print("source", row)
_, raw_name, phone, url, logo, is_archive = row
name = (raw_name or "").strip() or "Company-922"
pc.execute("SELECT 1 FROM company WHERE lower(name)=lower(%s)", (name,))
if pc.fetchone():
    name = f"{name} (C922)"

pc.execute(
    """
    SELECT column_name, is_nullable, column_default
    FROM information_schema.columns
    WHERE table_name='company' AND table_schema='public'
    ORDER BY ordinal_position
    """
)
print("pg columns:")
for r in pc.fetchall():
    print(" ", r)

pc.execute("SELECT company_type_id FROM company WHERE mysql_id IS NOT NULL LIMIT 1")
company_type_id = pc.fetchone()[0]

now = datetime.now(timezone.utc).replace(tzinfo=None)
new_id = str(uuid.uuid4())
status = "INACTIVE" if str(is_archive).upper() in ("1", "Y", "TRUE") else "ACTIVE"

pc.execute(
    """
    INSERT INTO company (
        id, version, created_at, last_modified_at, status,
        mysql_id, name, contact_no, website, logo_url, company_type_id
    ) VALUES (
        %s, 0, %s, %s, %s,
        922, %s, %s, %s, %s, %s
    )
    """,
    (new_id, now, now, status, name, phone, url, logo, company_type_id),
)
pg.commit()

mc.execute("SELECT COUNT(*) FROM company")
pc.execute("SELECT COUNT(*) FROM company WHERE mysql_id IS NOT NULL")
print("inserted", name, "id=", new_id)
print("counts mysql=", mc.fetchone()[0], "pg mysql_id=", pc.fetchone()[0])

my.close()
pg.close()
