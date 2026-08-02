#!/usr/bin/env python3
"""Generate the deterministic Better Questing 3 objective database."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "config" / "betterquesting" / "DefaultQuests.json"

OBJECTIVES = [
    ("Electrification", "Build a working IC2 generation, storage, and ore-processing line.", "minecraft:redstone"),
    ("Automation", "Build an automated ore-processing line using a Quarry and routed transport.", "minecraft:hopper"),
    ("Heavy Industry", "Produce Railcraft steel, refined fuel, and advanced electrical components at factory scale.", "minecraft:anvil"),
    ("Armed Survival", "Manufacture an industrial firearm and its ammunition.", "techguns:m4"),
    ("Human Factions", "Contact a settlement, survive a raider encounter, and resolve an ambiguous restricted-area contact.", "minecraft:skull"),
    ("Nuclear Power", "Establish a monitored IC2 nuclear reactor with remote emergency shutdown.", "minecraft:cauldron"),
    ("Moon Program", "Launch a Tier 1 rocket, land on the Moon, establish life support, and recover the Tier 2 path.", "galacticraftcore:rocket_t1"),
    ("Mars Program", "Launch a Tier 2 rocket, establish a Mars outpost, and return with Desh.", "galacticraftplanets:rocket_t2"),
    ("Lite Matter Engineering", "Power the Molecular Analyzer and record a Martian Desh material pattern.", "industrialcivilizationcore:material_pattern_record"),
    ("AI Age — Locked", "Observe the AE2 lock. Replication, UU-Matter, exotic materials, and AI-directed storage are not available in this slice.", "appliedenergistics2:controller"),
]


def quest(index, name, desc, icon):
    props = {
        "issilent:1": 0,
        "snd_complete:8": "minecraft:entity.player.levelup",
        "lockedprogress:1": 0,
        "tasklogic:8": "AND",
        "repeattime:3": -1,
        "visibility:8": "NORMAL",
        "simultaneous:1": 0,
        "icon:10": {"id:8": icon, "Count:3": 1, "Damage:2": 0, "OreDict:8": ""},
        "globalshare:1": 0,
        "questlogic:8": "AND",
        "partysinglereward:1": 0,
        "snd_update:8": "minecraft:entity.player.levelup",
        "autoclaim:1": 0,
        "ismain:1": 1 if index in (0, 6, 7, 8) else 0,
        "name:8": name,
        "desc:8": desc,
    }
    return {
        "questID:3": index,
        "preRequisites:11": [] if index == 0 else [index - 1],
        "properties:10": {"betterquesting:10": props},
        "tasks:9": {"0:10": {"index:3": 0, "taskID:8": "bq_standard:checkbox"}},
        "rewards:9": {},
    }


data = {
    "format:8": "2.0.0",
    "questDatabase:9": {f"{i}:10": quest(i, *obj) for i, obj in enumerate(OBJECTIVES)},
    "questLines:9": {
        "0:10": {
            "quests:9": {
                f"{i}:10": {"sizeX:3": 24, "x:3": i * 48, "y:3": 0, "id:3": i, "sizeY:3": 24}
                for i in range(len(OBJECTIVES))
            },
            "lineID:3": 0,
            "properties:10": {
                "betterquesting:10": {
                    "name:8": "Industrial Civilization",
                    "bg_image:8": "",
                    "bg_size:3": 1024,
                    "desc:8": "Capability objectives from Earth industry through Mars and Lite Matter Engineering.",
                }
            },
            "order:3": 0,
        }
    },
    "questSettings:10": {
        "betterquesting:10": {
            "livesdef:3": 3,
            "pack_name:8": "Industrial Civilization",
            "home_anchor_y:5": 0.0,
            "livesmax:3": 10,
            "home_anchor_x:5": 0.5,
            "editmode:1": 0,
            "hardcore:1": 0,
            "home_image:8": "betterquesting:textures/gui/image_v10.png",
            "party_enable:1": 1,
            "pack_version:3": 1,
            "home_offset_x:3": 0,
            "home_offset_y:3": 0,
        }
    },
}

OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
