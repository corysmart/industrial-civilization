#!/usr/bin/env python3
"""Assert parseable Industrial Civilization runtime markers in a Minecraft log."""
from __future__ import annotations
import argparse
import json
import re
import sys
from pathlib import Path

FATAL_PATTERNS = {
    "crash": re.compile(r"---- Minecraft Crash Report ----|The game crashed whilst", re.I),
    "script_errors": re.compile(r"Found [1-9][0-9]* errors? while running scripts", re.I),
    "industrial_exception": re.compile(r"(?:ERROR|Exception).*industrialcivilization", re.I),
    "missing_registry": re.compile(r"Missing registry data for.*industrialcivilization", re.I),
}

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("log", type=Path)
    parser.add_argument("--require", action="append", default=[])
    parser.add_argument("--snapshot", type=Path)
    args = parser.parse_args()
    if not args.log.is_file():
        print(f"E2E LOG: missing {args.log}")
        return 2
    text = args.log.read_text(errors="replace")
    failures = [name for name, pattern in FATAL_PATTERNS.items() if pattern.search(text)]
    missing = [marker for marker in args.require if marker not in text]
    snapshots = []
    for line in text.splitlines():
        marker = "IC_TEST|SNAPSHOT|"
        if marker in line:
            try:
                snapshots.append(json.loads(line.split(marker, 1)[1]))
            except json.JSONDecodeError:
                failures.append("malformed_snapshot")
    if args.snapshot and snapshots:
        args.snapshot.parent.mkdir(parents=True, exist_ok=True)
        args.snapshot.write_text(json.dumps(snapshots[-1], indent=2, sort_keys=True) + "\n")
    if failures or missing:
        print("E2E LOG: FAIL")
        for failure in sorted(set(failures)): print(f"- fatal marker: {failure}")
        for marker in missing: print(f"- missing marker: {marker}")
        return 1
    print(f"E2E LOG: PASS ({len(snapshots)} state snapshot(s))")
    return 0

if __name__ == "__main__":
    sys.exit(main())
