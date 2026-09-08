import psycopg2
pg=psycopg2.connect(host='165.22.209.54',port=5432,dbname='hrm_migration',user='Wl457oGknvD1Dsyk',password='Brvqg7E1e2ISwXKPGGB')
cur=pg.cursor()
cur.execute('SELECT COUNT(*) FROM hrm_attendance_time_request WHERE mysql_id IS NOT NULL')
print('PG_TIME_REQ', cur.fetchone()[0])
pg.close()
