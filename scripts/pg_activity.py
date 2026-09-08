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
SELECT pid, state, wait_event_type, wait_event, left(query, 160), now() - query_start AS dur
FROM pg_stat_activity
WHERE datname = 'hrm_migration' AND pid <> pg_backend_pid()
ORDER BY query_start NULLS LAST
"""
)
rows = cur.fetchall()
print("sessions", len(rows))
for r in rows:
    print(r)
c.close()
