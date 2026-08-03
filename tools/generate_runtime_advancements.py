#!/usr/bin/env python3
"""Generate hidden advancement criteria used by Better Questing runtime tasks."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROG = ROOT / "progression"
OUT = ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/industrialcivilizationcore/advancements"

milestones = []
for folder in ("chapters", "side-paths"):
    for path in sorted((PROG / folder).glob("*.json")):
        milestones.extend(json.loads(path.read_text())["milestones"])

OUT.mkdir(parents=True, exist_ok=True)
expected = set()
for milestone in milestones:
    value = milestone.get("runtime_advancement")
    if not value:
        continue
    advancement = milestone["id"] if value is True else value
    expected.add(f"{advancement}.json")
    payload = {"criteria": {"runtime": {"trigger": "minecraft:impossible"}}}
    (OUT / f"{advancement}.json").write_text(json.dumps(payload, indent=2) + "\n")

for path in OUT.glob("*.json"):
    if path.name not in expected:
        path.unlink()

print(f"Generated {len(expected)} hidden runtime advancements")
