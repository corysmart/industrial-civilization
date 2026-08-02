#!/usr/bin/env python3
"""Generate Better Questing 3 data deterministically from progression/*.json."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROGRESSION = ROOT / "progression"
OUT = ROOT / "config" / "betterquesting" / "DefaultQuests.json"


def load_chapters():
    return [json.loads(path.read_text(encoding="utf-8"))
            for path in sorted((PROGRESSION / "chapters").glob("*.json"))]


def load_side_paths():
    return [json.loads(path.read_text(encoding="utf-8"))
            for path in sorted((PROGRESSION / "side-paths").glob("*.json"))]


def split_item(ref):
    parts = ref.split(":")
    if len(parts) == 3 and parts[-1].isdigit():
        return ":".join(parts[:2]), int(parts[-1])
    return ref, 0


def stack(ref):
    item_id, damage = split_item(ref)
    return {"id:8": item_id, "Count:3": 1, "Damage:2": damage, "OreDict:8": ""}


def task_for(ms):
    if "required_item" not in ms:
        return {"index:3": 0, "taskID:8": "bq_standard:checkbox"}
    return {
        "partialMatch:1": 1,
        "ignoreNBT:1": 1,
        "consume:1": 0,
        "groupDetect:1": 1,
        "autoConsume:1": 0,
        "requiredItems:9": {"0:10": stack(ms["required_item"])},
        "index:3": 0,
        "taskID:8": "bq_standard:retrieval",
    }


def description(ms):
    lines = [
        f"Category: {ms['category'].title()}",
        "",
        ms["capability"],
        "",
        "Final validation will require:",
    ]
    lines.extend(f"- {value}" for value in ms["final_validation"])
    if ms.get("temporary_validation"):
        lines.extend(["", "TEMPORARY VALIDATION:", ms["temporary_validation"]])
    if ms.get("optional"):
        lines.extend(["", "Optional: this quest does not gate the canonical critical path."])
    return "\n".join(lines)


def quest(qid, ms, ids):
    props = {
        "issilent:1": 0,
        "snd_complete:8": "minecraft:entity.player.levelup",
        "lockedprogress:1": 1,
        "tasklogic:8": "AND",
        "repeattime:3": -1,
        "visibility:8": "ALWAYS",
        "simultaneous:1": 0,
        "icon:10": stack(ms["icon"]),
        "globalshare:1": 0,
        "questlogic:8": ms.get("prerequisite_logic", "AND"),
        "partysinglereward:1": 0,
        "snd_update:8": "minecraft:entity.player.levelup",
        "autoclaim:1": 0,
        "ismain:1": 1 if ms["critical"] else 0,
        "name:8": ms["title"],
        "desc:8": description(ms),
    }
    return {
        "questID:3": qid,
        "preRequisites:11": [ids[p] for p in ms["prerequisites"]],
        "properties:10": {"betterquesting:10": props},
        "tasks:9": {"0:10": task_for(ms)},
        "rewards:9": {},
    }


def main():
    chapters = load_chapters()
    side_paths = load_side_paths()
    graph = json.loads((PROGRESSION / "progression-graph.json").read_text(encoding="utf-8"))
    milestones = ([ms for chapter in chapters for ms in chapter["milestones"]] +
                  [ms for path in side_paths for ms in path["milestones"]])
    ids = {ms["id"]: index for index, ms in enumerate(milestones)}

    quest_database = {}
    quest_lines = {}
    for chapter in chapters:
        line_quests = {}
        critical_column = 0
        for ms in chapter["milestones"]:
            qid = ids[ms["id"]]
            quest_database[f"{qid}:10"] = quest(qid, ms, ids)
            if ms["optional"]:
                continue
            x, y = critical_column * 48, 0
            critical_column += 1
            line_quests[f"{qid}:10"] = {
                "sizeX:3": 24, "x:3": x, "y:3": y,
                "id:3": qid, "sizeY:3": 24,
            }
        line_id = chapter["number"] - 1
        quest_lines[f"{line_id}:10"] = {
            "quests:9": line_quests,
            "lineID:3": line_id,
            "properties:10": {"betterquesting:10": {
                "name:8": f"{chapter['number']:02d} — {chapter['title']}",
                "bg_image:8": "",
                "bg_size:3": 512,
                "desc:8": chapter["purpose"],
            }},
            "order:3": line_id,
        }

    for side_path in side_paths:
        for ms in side_path["milestones"]:
            qid = ids[ms["id"]]
            quest_database[f"{qid}:10"] = quest(qid, ms, ids)

    # Side paths are first-class quest lines, not hidden nodes inside the
    # numbered chapters. A quest appears in exactly one visual line.
    branch_titles = {
        "field_engineering": ("Side Path — Field Engineering", "Optional tools, resilience, remote control, and recovery capabilities."),
        "factions_and_salvage": (side_paths[0]["title"], side_paths[0]["purpose"]),
        "orbital_power": ("Side Path — Orbital Power", "Optional orbital generation and tracking-array development."),
        "cargo_logistics": ("Side Path — Cargo Logistics", "Optional freight and interplanetary cargo mastery."),
        "post_ai_parallel": ("Side Path — Post-AI Horizons", "Parallel civilization-scale endgame projects."),
    }
    for branch_index, (branch_id, branch_milestones) in enumerate(graph["optional_branches"].items()):
        line_id = len(chapters) + branch_index
        title, desc = branch_titles[branch_id]
        line_quests = {}
        for index, mid in enumerate(branch_milestones):
            qid = ids[mid]
            line_quests[f"{qid}:10"] = {
                "sizeX:3": 24, "x:3": (index % 6) * 56, "y:3": (index // 6) * 64,
                "id:3": qid, "sizeY:3": 24,
            }
        quest_lines[f"{line_id}:10"] = {
            "quests:9": line_quests,
            "lineID:3": line_id,
            "properties:10": {"betterquesting:10": {
                "name:8": title,
                "bg_image:8": "",
                "bg_size:3": 512,
                "desc:8": desc + " Side paths are visible from the start and may create alternate progression routes.",
            }},
            "order:3": line_id,
        }

    data = {
        "format:8": "2.0.0",
        "questDatabase:9": quest_database,
        "questLines:9": quest_lines,
        "questSettings:10": {"betterquesting:10": {
            "livesdef:3": 3,
            "pack_name:8": "Industrial Civilization — Phase 2",
            "home_anchor_y:5": 0.0,
            "livesmax:3": 10,
            "home_anchor_x:5": 0.5,
            "editmode:1": 0,
            "hardcore:1": 0,
            "home_image:8": "industrialcivilizationcore:textures/gui/quest_home_v2.png",
            "party_enable:1": 1,
            "pack_version:3": 3,
            "home_offset_x:3": -128,
            "home_offset_y:3": 0,
        }},
    }
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
    print(f"Generated {len(quest_database)} quests across {len(chapters)} chapters and {len(quest_lines) - len(chapters)} side paths")


if __name__ == "__main__":
    main()
