import mysql.connector

c = mysql.connector.connect(
    host="165.22.209.54",
    port=3306,
    user="root",
    password="Nepal@123",
    database="hrmsuite",
)
cur = c.cursor()
cur.execute("SHOW FULL PROCESSLIST")
rows = cur.fetchall()
print("mysql processes", len(rows))
for r in rows:
    # Id, User, Host, db, Command, Time, State, Info
    if r[4] not in ("Sleep",) or (r[7] is not None):
        print(r[0], r[4], r[5], r[6], (str(r[7])[:140] if r[7] else None))
c.close()
