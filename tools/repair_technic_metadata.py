#!/usr/bin/env python3
"""Remove deleted magic payload entries from Technic's extraction ledger."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ledger = ROOT / "bin/extractedFiles.json"
entries = json.loads(ledger.read_text(encoding="utf-8"))
blocked = ("projecte", "viewemc", "ic2cuumatter")
filtered = [entry for entry in entries if not any(token in entry.lower() for token in blocked)]
ledger.write_text(json.dumps(filtered, separators=(",", ":")) + "\n", encoding="utf-8")
print(f"Technic extraction ledger: {len(entries)} -> {len(filtered)} entries")
