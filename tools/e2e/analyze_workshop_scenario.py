#!/usr/bin/env python3
"""Analyze the live Technic log after the workshop HeadlessMC scenario."""
from pathlib import Path
import os
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[2]
PACK = Path(os.environ.get("IC_TECHNIC_PACK",
    "/Users/cory/Library/Application Support/technic/modpacks/industrial-civilization-astra"))
LOG = PACK / "logs/latest.log"
SNAPSHOT = ROOT / ".headlessmc/results/workshop-adjacency.json"
command = [sys.executable, str(Path(__file__).with_name("runtime_log_assert.py")), str(LOG),
    "--require", "IC_TEST|PASS|workshop_adjacency", "--snapshot", str(SNAPSHOT)]
raise SystemExit(subprocess.call(command))
