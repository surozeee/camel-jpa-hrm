import psycopg2

c = psycopg2.connect(
    host="165.22.209.54",
    port=5432,
    dbname="hrm_migration",
    user="Wl457oGknvD1Dsyk",
    password="Brvqg7E1e2ISwXKPGGB",
)
cur = c.cursor()
cur.execute(
    """
SELECT relname FROM pg_class
WHERE relkind='r' AND (
  relname ILIKE '%attendance%' OR relname ILIKE '%forgot%'
  OR relname ILIKE '%device_log%' OR relname ILIKE '%time_request%'
  OR relname = 'users'
)
ORDER BY 1
"""
)
tables = [r[0] for r in cur.fetchall()]
print("tables", tables)
for t in tables + ["hrm_leave_credit", "users"]:
    try:
        cur.execute(f"SELECT COUNT(*) FROM {t}")
        print(t, cur.fetchone()[0])
    except Exception as e:
        c.rollback()
        print(t, "err", str(e).splitlines()[0])
c.close()
