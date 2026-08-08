#!/usr/bin/env python3
"""Fail-fast preflight for the private HeadlessMC real-client smoke route."""
from pathlib import Path
import json, os, sys, zipfile

ROOT = Path(__file__).resolve().parents[2]
scenario = json.loads((Path(__file__).with_name("headlessmc-smoke.json")).read_text())
pack = Path(os.environ.get("IC_TECHNIC_PACK", scenario["pack_source"]))
hmc = os.environ.get("HEADLESSMC_JAR")
local_hmc = ROOT / ".headlessmc/headlessmc-launcher-wrapper.jar"
errors = []
if not pack.is_dir(): errors.append(f"Technic pack not found: {pack}")
required = [
    pack / "mods/IndustrialCivilizationCore-0.2.0.jar",
    pack / "config/betterquesting/DefaultQuests.json",
    pack / "groovy/postInit/industrial_civilization.groovy",
]
for path in required:
    if not path.is_file(): errors.append(f"missing staged input: {path}")
if not hmc and local_hmc.is_file(): hmc = str(local_hmc)
if hmc:
    path = Path(hmc)
    if not path.is_file(): errors.append(f"HEADLESSMC_JAR not found: {path}")
    elif not zipfile.is_zipfile(path): errors.append(f"HEADLESSMC_JAR is not a readable jar: {path}")
else:
    errors.append("HEADLESSMC_JAR is unset and the verified local HeadlessMC wrapper is missing")

if errors:
    print("HEADLESSMC PREFLIGHT: BLOCKED")
    for error in errors: print(f"- {error}")
    sys.exit(1)
print(f"HEADLESSMC PREFLIGHT: READY ({len(scenario['checks'])} smoke checks)")
print("Uses a verified local HeadlessMC install and an offline disposable identity; no account credentials required.")
