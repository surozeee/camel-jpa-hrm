"""Poll PG counts during migration. Uses env POSTGRES_* or .env via python-dotenv if present."""
import os
import time

try:
    from dotenv import load_dotenv

    load_dotenv()
except Exception:
    pass

import psycopg2

CONN = dict(
    host=os.environ.get("PGHOST", "165.22.209.54"),
    port=int(os.environ.get("PGPORT", "5432")),
    dbname=os.environ.get("PGDATABASE", "hrm_migration"),
    user=os.environ.get("PGUSER") or os.environ.get("POSTGRES_USERNAME"),
    password=os.environ.get("PGPASSWORD") or os.environ.get("POSTGRES_PASSWORD"),
)

CHECKS = [
    ("users", "select count(*) from users where mysql_id is not null"),
    ("salary", "select count(*) from hrm_employee_salary where mysql_id is not null"),
    ("loan", "select count(*) from hrm_employee_loan where mysql_id is not null"),
    ("roster_slot", "select count(*) from hrm_roster_shift_slot where mysql_id is not null"),
    ("vacancy", "select count(*) from recruitment_vacancy where mysql_id is not null"),
]


def snap():
    c = psycopg2.connect(**CONN)
    cur = c.cursor()
    out = {}
    for name, sql in CHECKS:
        try:
            cur.execute(sql)
            out[name] = cur.fetchone()[0]
        except Exception as e:
            c.rollback()
            out[name] = f"ERR:{e.__class__.__name__}"
    c.close()
    return out


if __name__ == "__main__":
    for _ in range(20):
        print(time.strftime("%H:%M:%S"), snap(), flush=True)
        time.sleep(45)
