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
my = pymysql.connect(
    host=m.group(1),
    port=int(m.group(2)),
    user=env["MYSQL_USERNAME"],
    password=env["MYSQL_PASSWORD"],
    database=m.group(3),
    cursorclass=pymysql.cursors.DictCursor,
)
mc = my.cursor()
p = re.match(r"jdbc:postgresql://([^:/]+):(\d+)/([^?]+)", env["POSTGRES_URL"])
pg = psycopg2.connect(
    host=p.group(1),
    port=int(p.group(2)),
    user=env["POSTGRES_USERNAME"],
    password=env["POSTGRES_PASSWORD"],
    dbname=p.group(3),
)
pc = pg.cursor()

print("MYSQL sec_role", end=" ")
mc.execute("SELECT COUNT(*) c FROM sec_role")
print(mc.fetchone())
print("MYSQL sec_user", end=" ")
mc.execute("SELECT COUNT(*) c FROM sec_user")
print(mc.fetchone())
print("MYSQL sec_user_sec_role", end=" ")
mc.execute("SELECT COUNT(*) c FROM sec_user_sec_role")
print(mc.fetchone())

for q in [
    "SELECT COUNT(*) FROM role",
    "SELECT COUNT(*) FROM role WHERE mysql_id IS NOT NULL",
    "SELECT COUNT(*) FROM users",
    "SELECT COUNT(*) FROM users WHERE mysql_id IS NOT NULL",
    "SELECT COUNT(*) FROM user_role",
    "SELECT COUNT(*) FROM role_permission",
]:
    try:
        pc.execute(q)
        print("PG", q, "=>", pc.fetchone()[0])
    except Exception as e:
        pg.rollback()
        print("PG ERR", q, e)

pc.execute(
    """
SELECT COUNT(*) FROM users u
WHERE u.mysql_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM user_role ur WHERE ur.user_id = u.id)
"""
)
print("users_without_roles", pc.fetchone()[0])

pc.execute("SELECT mysql_id, name FROM role ORDER BY mysql_id NULLS LAST LIMIT 10")
print("sample_roles", pc.fetchall())
pc.execute(
    """
SELECT u.mysql_id, u.email_address, COUNT(ur.role_id) roles
FROM users u
LEFT JOIN user_role ur ON ur.user_id = u.id
WHERE u.mysql_id IS NOT NULL
GROUP BY u.mysql_id, u.email_address
ORDER BY u.mysql_id
LIMIT 10
"""
)
print("sample_users", pc.fetchall())

my.close()
pg.close()
