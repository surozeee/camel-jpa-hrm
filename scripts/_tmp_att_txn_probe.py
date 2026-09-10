import pymysql

c = pymysql.connect(
    host="165.22.209.54",
    port=3306,
    user="root",
    password="Nepal@123",
    database="hrmsuite",
)
cur = c.cursor()
cur.execute("SHOW COLUMNS FROM attendance_transaction")
cols = [r[0] for r in cur.fetchall()]
print("cols", cols)
for q in [
    "SELECT COUNT(*) FROM attendance_transaction WHERE log_date >= '2026-08-01 00:00:00' AND log_date <= NOW()",
    "SELECT COUNT(*) FROM attendance_transaction WHERE logDate >= '2026-08-01 00:00:00' AND logDate <= NOW()",
]:
    try:
        cur.execute(q)
        print("ok", cur.fetchone()[0], q)
    except Exception as e:
        print("fail", str(e).split("\n")[0][:160])
c.close()
