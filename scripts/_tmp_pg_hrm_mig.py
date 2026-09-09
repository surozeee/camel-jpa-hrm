import psycopg2
c = psycopg2.connect(host="165.22.209.54", port=5432, dbname="hrm_migration",
                     user="Wl457oGknvD1Dsyk", password="Brvqg7E1e2ISwXKPGGB")
cur = c.cursor()
for t in ["company", "branch", "employee", "users", "hrm_attendance_log", "hrm_attendance", "hrm_attendance_forgot"]:
    try:
        cur.execute(f"SELECT COUNT(*) FROM {t}")
        print(t, cur.fetchone()[0])
    except Exception as e:
        c.rollback()
        print(t, "ERR", str(e).splitlines()[0][:80])
c.close()
