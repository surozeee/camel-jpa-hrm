import os
import signal
import subprocess
import time
from pathlib import Path

ROOT = Path(r"D:\Projects\camel-jpa-hrm")


def list_java():
    ps = r"""
Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='cmd.exe'" |
  Select-Object ProcessId, Name, CommandLine |
  ConvertTo-Json -Depth 3
"""
    raw = subprocess.check_output(
        ["powershell", "-NoProfile", "-Command", ps],
        text=True,
        errors="replace",
    )
    import json

    data = json.loads(raw) if raw.strip() else []
    if isinstance(data, dict):
        data = [data]
    return data


def should_kill(cmd: str) -> bool:
    if not cmd:
        return False
    c = cmd.lower()
    markers = (
        "cameljpatnapplication",
        "gradlew.bat bootrun",
        "gradlew bootrun",
        "gradle-wrapper.jar\" bootrun",
        "gradle-wrapper.jar bootrun",
        "migration-from-start",
    )
    return any(m in c for m in markers) or (
        "camel-jpa-hrm" in c and "bootrun" in c
    )


killed = []
for row in list_java():
    cmd = row.get("CommandLine") or ""
    pid = row.get("ProcessId")
    if should_kill(cmd):
        try:
            subprocess.run(
                ["taskkill", "/F", "/T", "/PID", str(pid)],
                check=False,
                capture_output=True,
                text=True,
            )
            killed.append(pid)
            print(f"killed {pid}")
        except Exception as e:
            print(f"fail kill {pid}: {e}")

print("killed_count", len(killed))
time.sleep(3)

# ensure port 9000 free-ish and no leftover bootRun
left = [r for r in list_java() if should_kill(r.get("CommandLine") or "")]
print("remaining_boot", [(r.get("ProcessId"), (r.get("CommandLine") or "")[:120]) for r in left])
