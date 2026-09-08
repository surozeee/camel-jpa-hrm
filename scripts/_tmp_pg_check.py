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
SELECT pid, state, wait_event_type, wait_event,
       left(query, 200) AS q,
       now() - query_start AS dur,
       now() - state_change AS state_dur
FROM pg_stat_activity
WHERE datname = 'hrm_migration' AND pid <> pg_backend_pid()
ORDER BY query_start NULLS LAST
"""
)
print("=== activity ===")
for r in cur.fetchall():
    print(r)

cur.execute(
    """
SELECT blocked.pid AS blocked_pid,
       blocking.pid AS blocking_pid,
       left(blocked.query, 120) AS blocked_q,
       left(blocking.query, 120) AS blocking_q
FROM pg_stat_activity blocked
JOIN pg_locks bl ON bl.pid = blocked.pid AND NOT bl.granted
JOIN pg_locks gl ON gl.locktype = bl.locktype
  AND gl.database IS NOT DISTINCT FROM bl.database
  AND gl.relation IS NOT DISTINCT FROM bl.relation
  AND gl.page IS NOT DISTINCT FROM bl.page
  AND gl.tuple IS NOT DISTINCT FROM bl.tuple
  AND gl.virtualxid IS NOT DISTINCT FROM bl.virtualxid
  AND gl.transactionid IS NOT DISTINCT FROM bl.transactionid
  AND gl.classid IS NOT DISTINCT FROM bl.classid
  AND gl.objid IS NOT DISTINCT FROM bl.objid
  AND gl.objsubid IS NOT DISTINCT FROM bl.objsubid
  AND gl.pid <> bl.pid
  AND gl.granted
JOIN pg_stat_activity blocking ON blocking.pid = gl.pid
WHERE blocked.datname = 'hrm_migration'
"""
)
print("=== blocks ===")
rows = cur.fetchall()
print("count", len(rows))
for r in rows:
    print(r)

for t in ["users", "employee", "calculated_auto_leave_credit", "employee_leave_balance"]:
    try:
        cur.execute(f"SELECT COUNT(*) FROM {t}")
        print("count", t, cur.fetchone()[0])
    except Exception as e:
        c.rollback()
        print("count", t, "ERR", str(e).splitlines()[0])

c.close()
