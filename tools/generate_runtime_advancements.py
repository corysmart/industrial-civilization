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


ADVANCEMENT_DESCRIPTION_OVERRIDES = {
    "mobile_quarry_relocation": "Physically recover and redeploy the same exhausted Quarry in the next lane.",
    "first_resources": "Gather the metals, fuel, and rubber that make an electrical workshop possible.",
    "first_ic2_generator": "Generate the first EU for a workshop that can grow beyond hand labor.",
    "voltage_literacy": "Build with IC2 voltage tiers, transformers, and insulation safely.",
    "quarry_extraction": "Turn automated extraction into permanent industrial infrastructure.",
    "early_autocrafting": "Route physical ingredients through a repeatable automatic recipe.",
    "programmable_assembler": "Move from fixed automation to selectable production programs.",
    "nuclear_reactor": "Bring a complete IC2 reactor online with safety designed in from the start.",
    "emergency_shutdown": "Prove that the reactor can be stopped without approaching the core.",
    "orbital_habitat": "Create a breathable foothold beyond Earth's atmosphere.",
    "functional_orbital_station": "Sustain habitation, power, research, and safe arrival together in orbit.",
    "orbital_research_complete": "Turn orbital experiments into the knowledge required for the Moon.",
    "lunar_darkness_mastery": "Keep lunar life support and industry running through prolonged darkness.",
    "extreme_voltage_industry": "Operate EV infrastructure without confusing throughput with safe packet size.",
    "quantumsuit": "Equip and charge a complete QuantumSuit from mature high-tier industry.",
    "tier2_mars_launch": "Commit a Mars-ready civilization package to interplanetary flight.",
    "functional_martian_base": "Sustain a Martian settlement with local industry and a dependable return route.",
    "autonomous_resource_response": "Teach distant production to recognize and answer its own shortages.",
    "martian_autonomy_complete": "Prove that Martian industry can respond without constant human attention.",
    "molecular_analyzer": "Begin matter engineering with precise measurement rather than transmutation.",
    "artificial_industrial_intelligence_core": "Unify the civilization's research into an industrial coordination system.",
    "ai_age_entry": "Scale automation from individual machines to connected industrial systems.",
    "ae2_entry": "Bring storage, pattern knowledge, and manufacturing into one digital network.",
    "ae2_autocrafting": "Coordinate multi-step manufacturing through encoded network patterns.",
    "continuous_civilization": "Build the capacity for technological civilization to keep expanding.",
    "crop_engineering": "Establish parallel IC2 crop-breeding experiments for renewable materials.",
    "breed_hemp": "Discover Hemp and secure a renewable source of industrial fiber.",
    "lv_charcoal_tree_farm": "Close the loop between automated forestry and electrical generation.",
    "mfsu_bank_baseline": "Establish one legal 512-EU packet as the burst-power baseline.",
    "mfsu_bank_quad": "Deliver four legal MFSU packets in one tick for 2,048 EU/t.",
    "mfsu_bank_ten": "Scale parallel MFSU delivery to ten legal packets and 5,120 EU/t.",
    "mfsu_bank_fifty": "Deliver 25,600 EU/t as fifty legal packets, never one illegal voltage spike.",
    "blink_manufacturing": "Turn a physical fifty-MFSU bank into sub-second industrial work.",
}


def advancement_description(milestone):
    if milestone["id"] in ADVANCEMENT_DESCRIPTION_OVERRIDES:
        return ADVANCEMENT_DESCRIPTION_OVERRIDES[milestone["id"]]
    title = milestone["title"]
    templates = {
        "construction": f"Establish {title} as working industrial infrastructure.",
        "possession": f"Secure {title} for the civilization's next stage.",
        "operation": f"Put {title} into reliable operation.",
        "research": f"Complete {title} and turn observation into engineering knowledge.",
        "transition": f"Open the technological path to {title}.",
        "mastery": f"Integrate the systems required for {title}.",
    }
    return templates[milestone["category"]]


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
            "description": {"text": advancement_description(milestone)},
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
