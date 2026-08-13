#!/usr/bin/env python3
"""Validate the generated single-tab advancement migration."""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets"
IC_ROOT = ASSETS / "industrialcivilizationcore/advancements"
PORT_ROOT = IC_ROOT / "ported"
MANIFEST = json.loads((ROOT / "progression/unified-advancements.json").read_text())
MAPPING = json.loads((ASSETS / "industrialcivilizationcore/ported_advancements.json").read_text())


def advancement_id(path):
    relative = path.relative_to(IC_ROOT).as_posix()[:-5]
    return "industrialcivilizationcore:" + relative


files = sorted(IC_ROOT.rglob("*.json"))
all_ids = {advancement_id(path) for path in files}
ports = sorted(PORT_ROOT.rglob("*.json"))
port_ids = {advancement_id(path) for path in ports}
errors = []

if len(ports) != MANIFEST["ported"]:
    errors.append(f"ported file count {len(ports)} != manifest {MANIFEST['ported']}")
if set(MAPPING.values()) != port_ids:
    errors.append("runtime compatibility mapping does not exactly cover ported files")
if set(MAPPING).intersection(MANIFEST["excluded"]):
    errors.append("excluded advancement leaked into runtime mapping")

parents = {}
for path in files:
    data = json.loads(path.read_text())
    identifier = advancement_id(path)
    parent = data.get("parent")
    if parent and ":" not in parent:
        parent = "industrialcivilizationcore:" + parent
    parents[identifier] = parent
    if identifier in port_ids and "display" not in data:
        errors.append(f"ported advancement lacks display: {identifier}")
    if identifier in port_ids and data.get("criteria") != {
        "ported_source": {"trigger": "minecraft:impossible"}
    }:
        errors.append(f"ported advancement can bypass ordered synchronization: {identifier}")
    if parent and parent.startswith("industrialcivilizationcore:") and parent not in all_ids:
        errors.append(f"missing parent {parent} for {identifier}")

for identifier in parents:
    seen = set()
    current = identifier
    while current in parents and parents[current]:
        if current in seen:
            errors.append(f"parent cycle involving {identifier}")
            break
        seen.add(current)
        current = parents[current]

for source in MAPPING:
    namespace, path = source.split(":", 1)
    hidden_path = ASSETS / namespace / "advancements" / f"{path}.json"
    if not hidden_path.is_file():
        errors.append(f"missing hidden source override: {source}")
    elif "display" in json.loads(hidden_path.read_text()):
        errors.append(f"foreign display remains visible: {source}")

required_exclusions = {
    "minecraft:husbandry/balanced_diet",
    "minecraft:story/enchant_item",
    "minecraft:end/root",
    "galacticraftcore:galacticraft/basic_solar",
    "galacticraftcore:galacticraft/advanced_solar",
    "railcraft:tracks/firestone",
}
if not required_exclusions.issubset(set(MANIFEST["excluded"])):
    errors.append("required impossible, magical, or duplicate exclusions are missing")

if errors:
    print(f"UNIFIED ADVANCEMENT VALIDATION FAILED: {len(errors)} error(s)")
    for error in errors:
        print(" - " + error)
    raise SystemExit(1)
print(f"UNIFIED ADVANCEMENTS: {len(ports)} ported, {len(all_ids)} visible, "
      f"{len(MANIFEST['excluded'])} removed/replaced; one rooted campaign tree")
