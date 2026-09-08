import subprocess
import json

ps = r"""
Get-CimInstance Win32_Process -Filter "Name='java.exe'" |
  Select-Object ProcessId, CommandLine |
  ConvertTo-Json -Depth 3
"""
raw = subprocess.check_output(
    ["powershell", "-NoProfile", "-Command", ps],
    text=True,
    errors="replace",
)
data = json.loads(raw) if raw.strip() else []
if isinstance(data, dict):
    data = [data]
for row in data:
    cmd = row.get("CommandLine") or ""
    short = cmd.replace("\r", " ").replace("\n", " ")
    if len(short) > 220:
        short = short[:220] + "..."
    print(f"{row.get('ProcessId')}\t{short}")
