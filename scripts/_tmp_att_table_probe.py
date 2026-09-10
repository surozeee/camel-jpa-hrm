import pymysql

c = pymysql.connect(
    host="165.22.209.54",
    port=3306,
    user="root",
    password="Nepal@123",
    database="hrmsuite",
)
cur = c.cursor()
cur.execute("SHOW TABLES LIKE '%att%Log%'")
print("tables_attLog", cur.fetchall())
cur.execute("SHOW TABLES LIKE '%att%log%'")
print("tables_attlog", cur.fetchall())
cur.execute("SHOW TABLES LIKE 'attendance%'")
print("tables_attendance", cur.fetchall())

for t in ["attLogs", "att_logs", "attlogs", "AttLogs"]:
    try:
        cur.execute(f"SELECT COUNT(*) FROM `{t}`")
        print("count_ok", t, cur.fetchone()[0])
        cur.execute(f"SHOW COLUMNS FROM `{t}`")
        cols = [r[0] for r in cur.fetchall()]
        print("cols", t, cols[:20])
    except Exception as e:
        print("fail", t, str(e).split("\n")[0][:160])

# try queries like QA
for q in [
    "SELECT COUNT(*) FROM att_logs WHERE (is_deleted IS NULL OR is_deleted = 'N') AND check_time >= '2026-08-01 00:00:00' AND check_time <= NOW()",
    "SELECT COUNT(*) FROM `attLogs` WHERE (`isDeleted` IS NULL OR `isDeleted` = 'N') AND `checkTime` >= '2026-08-01 00:00:00' AND `checkTime` <= NOW()",
    "SELECT COUNT(*) FROM attLogs WHERE (isDeleted IS NULL OR isDeleted = 'N') AND checkTime >= '2026-08-01 00:00:00' AND checkTime <= NOW()",
]:
    try:
        cur.execute(q)
        print("QOK", cur.fetchone()[0], q[:80])
    except Exception as e:
        print("QFAIL", str(e).split("\n")[0][:180])

c.close()
