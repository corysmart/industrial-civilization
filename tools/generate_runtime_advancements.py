#!/usr/bin/env python3
"""Generate the visible, progression-ordered advancement tree for every quest."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROG = ROOT / "progression"
OUT = ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/industrialcivilizationcore/advancements"
DETECTION = json.loads((PROG / "objective-detection.json").read_text())


def load(folder):
    return [json.loads(path.read_text()) for path in sorted((PROG / folder).glob("*.json"))]


def advancement_id(milestone):
    value = milestone.get("runtime_advancement")
    return milestone["id"] if value is True or not value else value


def split_item(reference):
    legacy_aliases = {
        "minecraft:firework_rocket": ("minecraft:fireworks", None),
        "minecraft:red_sand": ("minecraft:sand", 1),
    }
    if reference in legacy_aliases:
        return legacy_aliases[reference]
    parts = reference.split(":")
    if len(parts) == 3 and (parts[-1].isdigit() or parts[-1] == "*"):
        return ":".join(parts[:2]), None if parts[-1] == "*" else int(parts[-1])
    return reference, None


def evidence(milestone):
    if milestone.get("required_item"):
        return [milestone["required_item"]]
    return DETECTION.get("overrides", {}).get(milestone["id"], [milestone["icon"]])


def item_reference(value):
    return value["item"] if isinstance(value, dict) else value


def icon(milestone):
    item, data = split_item(milestone["icon"])
    result = {"item": item}
    if data is not None:
        result["data"] = data
    return result


chapters = load("chapters")
side_paths = load("side-paths")
all_milestones = [m for line in chapters + side_paths for m in line["milestones"]]
by_id = {m["id"]: m for m in all_milestones}
adv_by_milestone = {m["id"]: advancement_id(m) for m in all_milestones}

OUT.mkdir(parents=True, exist_ok=True)
expected = {"root.json"}
root = {
    "display": {
        "icon": {"item": "industrialcivilizationcore:molecular_analyzer"},
        "title": {"text": "Industrial Civilization"},
        "description": {"text": "Earth industry to an autonomous interplanetary civilization"},
        "background": "minecraft:textures/gui/advancements/backgrounds/stone.png",
        "frame": "task",
        "show_toast": False,
        "announce_to_chat": False,
        "hidden": False,
    },
    "criteria": {"open": {"trigger": "minecraft:tick"}},
}
(OUT / "root.json").write_text(json.dumps(root, indent=2) + "\n")

previous_critical = "root"
entries = []
for chapter_index, line in enumerate(chapters, 1):
    for milestone_index, milestone in enumerate(line["milestones"], 1):
        if milestone.get("critical"):
            parent = previous_critical
            previous_critical = advancement_id(milestone)
        elif milestone.get("prerequisites"):
            parent = adv_by_milestone[milestone["prerequisites"][0]]
        else:
            parent = previous_critical
        entries.append((milestone, parent, f"{chapter_index:02d}.{milestone_index:02d}"))

for path_index, line in enumerate(side_paths, 1):
    previous = "root"
    for milestone_index, milestone in enumerate(line["milestones"], 1):
        prerequisites = milestone.get("prerequisites", [])
        parent = adv_by_milestone[prerequisites[0]] if prerequisites else previous
        entries.append((milestone, parent, f"S{path_index}.{milestone_index:02d}"))
        previous = advancement_id(milestone)

written = {}
for milestone, parent, order in entries:
    aid = advancement_id(milestone)
    if aid in written and written[aid] != milestone["id"]:
        raise SystemExit(f"advancement id collision: {aid}: {written[aid]} vs {milestone['id']}")
    written[aid] = milestone["id"]
    expected.add(f"{aid}.json")
    if milestone.get("runtime_advancement"):
        criteria = {"runtime": {"trigger": "minecraft:impossible"}}
    else:
        criteria = {}
        for index, value in enumerate(evidence(milestone)):
            item, data = split_item(item_reference(value))
            item_condition = {"item": item}
            if data is not None:
                item_condition["data"] = data
            criteria[f"evidence_{index + 1}"] = {
                "trigger": "minecraft:inventory_changed",
                "conditions": {"items": [item_condition]},
            }
    payload = {
        "parent": f"industrialcivilizationcore:{parent}",
        "display": {
            "icon": icon(milestone),
            "title": {"text": f"{order} — {milestone['title']}"},
            "description": {"text": milestone["capability"]},
            "frame": "challenge" if milestone["category"] == "mastery" else
                "goal" if milestone["category"] in ("construction", "operation") else "task",
            "show_toast": True,
            "announce_to_chat": False,
            "hidden": False,
        },
        "criteria": criteria,
    }
    (OUT / f"{aid}.json").write_text(json.dumps(payload, indent=2) + "\n")

for path in OUT.glob("*.json"):
    if path.name not in expected:
        path.unlink()

print(f"Generated {len(entries)} visible quest advancements plus the ordered root")
