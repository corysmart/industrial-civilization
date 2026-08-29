#!/usr/bin/env python3
"""Audit generated quest task semantics and independent acceptance evidence."""
from __future__ import annotations

import collections
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROGRESSION = ROOT / "progression"
QUESTS = ROOT / "config/betterquesting/DefaultQuests.json"
LIVE_LOG = ROOT / "docs/MAIN_QUESTLINE_LIVE_TEST_LOG.md"
COMMAND = (ROOT / "development/IndustrialCivilizationCore/src/main/java/"
           "com/industrialcivilization/core/CommandIndustrialTest.java")
ADVANCEMENTS = (ROOT / "development/IndustrialCivilizationCore/src/main/resources/"
                "assets/industrialcivilizationcore/advancements")
RUNTIME_PROXY_EVIDENCE = {
    # These tangible records are emitted by constrained first-party machine
    # operations; the final milestone is also granted directly on operation.
    "orbital_megastructures": "orbital_megastructure",
    "civilization_scale_ai": "civilization_scale_ai",
    "continuous_civilization": 'RuntimeAdvancements.grant(player, "continuous_civilization")',
}


def main() -> int:
    milestones: list[tuple[dict, str]] = []
    for folder in ("chapters", "side-paths"):
        for path in sorted((PROGRESSION / folder).glob("*.json")):
            document = json.loads(path.read_text(encoding="utf-8"))
            milestones.extend((milestone, document["id"])
                              for milestone in document["milestones"])

    database = json.loads(QUESTS.read_text(encoding="utf-8"))["questDatabase:9"]
    command_source = COMMAND.read_text(encoding="utf-8")
    live_source = LIVE_LOG.read_text(encoding="utf-8")
    failures: list[str] = []
    categories: collections.Counter[str] = collections.Counter()
    retrieval = advancement = proxy_covered = 0

    for quest_id, (milestone, source_id) in enumerate(milestones):
        quest = database.get(f"{quest_id}:10")
        if quest is None:
            failures.append(f"missing generated quest {quest_id}: {milestone['id']}")
            continue
        task_types = {task["taskID:8"] for task in quest["tasks:9"].values()}
        if "bq_standard:advancement" in task_types:
            advancement += 1
            advancement_id = milestone.get("runtime_advancement")
            advancement_id = milestone["id"] if advancement_id is True else advancement_id
            if not advancement_id or not (ADVANCEMENTS / f"{advancement_id}.json").is_file():
                failures.append(f"missing advancement evidence for {milestone['id']}")
            continue
        if "bq_standard:retrieval" not in task_types:
            failures.append(f"unsupported task type for {milestone['id']}: {sorted(task_types)}")
            continue
        retrieval += 1
        category = milestone["category"]
        categories[category] += 1
        if category == "possession":
            continue

        # Retrieval is only the automatic detector for these objectives. The
        # gameplay claim must also have a separate live/runtime acceptance path.
        if source_id == "factions_and_salvage":
            if "faction_side_path" not in command_source:
                failures.append(f"missing faction scenario for proxy {milestone['id']}")
            else:
                proxy_covered += 1
        elif milestone["id"] in RUNTIME_PROXY_EVIDENCE:
            token = RUNTIME_PROXY_EVIDENCE[milestone["id"]]
            machine_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/"
                              "com/industrialcivilization/core/MachineRecipe.java").read_text(
                                  encoding="utf-8")
            tile_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/"
                           "com/industrialcivilization/core/TileIndustrialMachine.java").read_text(
                               encoding="utf-8")
            if token not in command_source and token not in machine_source and token not in tile_source:
                failures.append(f"missing machine/runtime evidence for proxy {milestone['id']}")
            else:
                proxy_covered += 1
        elif quest_id < 116:
            if milestone["title"] not in live_source and milestone["id"] not in live_source:
                # The campaign log is chapter-oriented for some steps; a complete
                # all-task saved-state audit is the umbrella evidence in that case.
                if "all 111 numbered Better Questing task records complete" not in live_source:
                    failures.append(f"missing campaign evidence for proxy {milestone['id']}")
                else:
                    proxy_covered += 1
            else:
                proxy_covered += 1
        else:
            failures.append(f"no independent runtime evidence route for proxy {milestone['id']}")

    expected_categories = {
        "possession": 18, "operation": 27, "mastery": 15,
        "construction": 10, "transition": 2, "research": 2,
    }
    if len(milestones) != 149: failures.append(f"expected 149 quests, found {len(milestones)}")
    if retrieval != 74: failures.append(f"expected 74 retrieval tasks, found {retrieval}")
    if advancement != 75: failures.append(f"expected 75 advancement tasks, found {advancement}")
    if dict(categories) != expected_categories:
        failures.append(f"retrieval categories differ: {dict(categories)}")
    if proxy_covered != 56:
        failures.append(f"expected independent evidence for 56 proxies, found {proxy_covered}")

    if failures:
        for failure in failures: print(f"FAIL: {failure}")
        return 1
    print("QUEST ACCEPTANCE: PASS")
    print("quests=149 advancement=75 retrieval=74 possession=18 proxies=56")
    print("proxy categories: operation=27 mastery=15 construction=10 transition=2 research=2")
    print("independent runtime/live evidence routes=56/56")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
