import os
import subprocess
import time
from datetime import datetime
from pathlib import Path

ROOT = Path(r"D:\Projects\camel-jpa-hrm")
logs = ROOT / "logs"
logs.mkdir(exist_ok=True)

ts = datetime.now().strftime("%Y%m%d-%H%M%S")
log = logs / f"migration-from-start-{ts}.log"
(logs / "migration-log-path-active.txt").write_text(str(log), encoding="utf-8")
(logs / "current-migration-log.txt").write_text(str(log), encoding="utf-8")
(logs / f"migration-env-{ts}.ps1").write_text(
    "\n".join(
        [
            "$env:MIGRATION_MODE='from'",
            "$env:MIGRATION_FROM_STEP='privilege-migration'",
            "$env:MIGRATION_VERIFY_AFTER_EACH='true'",
            "$env:MIGRATION_FAIL_ON_VERIFY='false'",
            f"Set-Location '{ROOT}'",
            "",
        ]
    ),
    encoding="utf-8",
)

env = os.environ.copy()
env["MIGRATION_MODE"] = "from"
env["MIGRATION_FROM_STEP"] = "privilege-migration"
env["MIGRATION_VERIFY_AFTER_EACH"] = "true"
env["MIGRATION_FAIL_ON_VERIFY"] = "false"

# Detached cmd so agent shell teardown won't kill migration
creationflags = subprocess.CREATE_NEW_PROCESS_GROUP | subprocess.DETACHED_PROCESS
cmd = (
    f'cmd.exe /c "gradlew.bat bootRun --args=--spring.profiles.active=dev '
    f'--no-daemon > \"{log}\" 2>&1"'
)
proc = subprocess.Popen(
    cmd,
    cwd=str(ROOT),
    env=env,
    shell=True,
    creationflags=creationflags,
    stdout=subprocess.DEVNULL,
    stderr=subprocess.DEVNULL,
    stdin=subprocess.DEVNULL,
    close_fds=True,
)
print(f"starter_pid={proc.pid}")
print(f"log={log}")
time.sleep(2)
print("started_ok")
