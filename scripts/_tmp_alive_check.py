import json
import subprocess
from pathlib import Path

# process alive?
ps = """
$p = Get-Process -Id 22008 -ErrorAction SilentlyContinue
if ($p) { $p | Select-Object Id,CPU,WorkingSet64,StartTime | ConvertTo-Json } else { 'DEAD' }
"""
print("proc", subprocess.check_output(["powershell","-NoProfile","-Command",ps], text=True, errors="replace").strip())

# stack snippet
jstack = Path(r"C:\Program Files\Java\jdk-21\bin\jstack.exe")
if jstack.exists():
    try:
        out = subprocess.check_output([str(jstack), "22008"], text=True, errors="replace", timeout=30)
        interesting = []
        for line in out.splitlines():
            if any(x in line for x in ("jojolaptech", "hibernate", "hikari", "Migration", "Camel", "main\"", "RUNNABLE", "BLOCKED", "WAITING")):
                interesting.append(line)
        print("jstack_hits", len(interesting))
        for line in interesting[:40]:
            print(line[:200])
    except Exception as e:
        print("jstack_err", e)

# pg counts
try:
    import psycopg2
    c = psycopg2.connect(host="165.22.209.54", port=5432, dbname="hrmsuite", user="Wl457oGknvD1Dsyk", password="Brvqg7E1e2ISwXKPGGB")
    cur = c.cursor()
    for t in ["privilege", "role", "company", "employee"]:
        try:
            cur.execute(f'SELECT COUNT(*) FROM "{t}"')
            print(t, cur.fetchone()[0])
        except Exception as e:
            c.rollback()
            try:
                cur.execute(f"SELECT COUNT(*) FROM {t}")
                print(t, cur.fetchone()[0])
            except Exception as e2:
                c.rollback()
                print(t, "ERR", str(e2)[:80])
    c.close()
except Exception as e:
    print("pg_err", e)

log = Path(r"D:\Projects\camel-jpa-hrm\logs\migration-from-start-ACTIVE.log")
print("log_size", log.stat().st_size)
