import pymysql

c = pymysql.connect(host="165.22.209.54", user="root", password="Nepal@123", database="hrmsuite")
cur = c.cursor()
cur.execute("SHOW TABLES LIKE %s", ("%shift%",))
print("tables", cur.fetchall())
for t in ("attEmpShift", "attempshift", "att_emp_shift"):
    try:
        cur.execute(f"SELECT COUNT(*) FROM `{t}`")
        print(t, cur.fetchone()[0])
    except Exception as e:
        print(t, type(e).__name__, e)
c.close()
