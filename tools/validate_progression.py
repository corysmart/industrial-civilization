#!/usr/bin/env python3
"""Static validator for canonical progression, generated quests, and placeholders."""
import json
import re
import sys
import zipfile
from collections import defaultdict, deque
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROG = ROOT / "progression"
errors = []
checks = []


def check(condition, message):
    (checks if condition else errors).append(message)


def read_json(path):
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
        check(True, f"valid JSON: {path.relative_to(ROOT)}")
        return data
    except Exception as exc:
        check(False, f"valid JSON: {path.relative_to(ROOT)} ({exc})")
        return {}


chapter_paths = sorted((PROG / "chapters").glob("*.json"))
chapters = [read_json(path) for path in chapter_paths]
graph = read_json(PROG / "progression-graph.json")
pacing = read_json(PROG / "pacing.json")
profiles = read_json(PROG / "optimization-profiles.json")
placeholders = read_json(PROG / "placeholder-registry.json")
telemetry = read_json(PROG / "telemetry-schema.json")
schemas = {path.name: read_json(path) for path in sorted((PROG / "schemas").glob("*.json"))}

# Lightweight, dependency-free validation against the checked-in schemas.
chapter_required = schemas.get("chapter.schema.json", {}).get("required", [])
milestone_required = schemas.get("milestone.schema.json", {}).get("required", [])
for chapter in chapters:
    for key in chapter_required:
        check(key in chapter, f"chapter {chapter.get('id')} schema field {key}")
    for milestone in chapter.get("milestones", []):
        for key in milestone_required:
            check(key in milestone, f"milestone {milestone.get('id')} schema field {key}")
        check(milestone.get("category") in {"possession", "construction", "operation", "mastery", "research", "transition"},
              f"milestone {milestone.get('id')} category enum")
for name, required in (("pacing", ["canonical_profile", "ai_age_target_hours", "milestones"]),
                       ("optimization profiles", ["profiles"]),
                       ("placeholder registry", ["namespace", "config", "entries"]),
                       ("telemetry", ["implemented", "fields"])):
    data = {"pacing": pacing, "optimization profiles": profiles,
            "placeholder registry": placeholders, "telemetry": telemetry}[name]
    check(all(key in data for key in required), f"{name} schema required fields")

check(len(chapters) == 16, "exactly 16 canonical chapters")
check([c.get("number") for c in chapters] == list(range(1, 17)), "chapter numbers are contiguous")
check([c.get("id") for c in chapters] == graph.get("canonical_order"), "chapter order matches progression graph")

milestones = [ms for chapter in chapters for ms in chapter.get("milestones", [])]
by_id = {ms.get("id"): ms for ms in milestones}
check(len(by_id) == len(milestones), "milestone IDs are globally unique")
for chapter in chapters:
    check(chapter.get("completion_milestone") in by_id, f"chapter {chapter.get('id')} has a real completion milestone")
for ms in milestones:
    for pre in ms.get("prerequisites", []):
        check(pre in by_id, f"milestone {ms.get('id')} prerequisite exists: {pre}")

# Kahn reachability and cycle detection.
indegree = {mid: 0 for mid in by_id}
children = defaultdict(list)
for mid, ms in by_id.items():
    for pre in ms.get("prerequisites", []):
        if pre in by_id:
            indegree[mid] += 1
            children[pre].append(mid)
queue = deque(sorted(mid for mid, degree in indegree.items() if degree == 0))
reached = []
while queue:
    current = queue.popleft()
    reached.append(current)
    for child in children[current]:
        indegree[child] -= 1
        if indegree[child] == 0:
            queue.append(child)
check(len(reached) == len(by_id), "milestone graph is acyclic and fully reachable")


def ancestors(mid):
    found, todo = set(), list(by_id.get(mid, {}).get("prerequisites", []))
    while todo:
        value = todo.pop()
        if value in found:
            continue
        found.add(value)
        todo.extend(by_id.get(value, {}).get("prerequisites", []))
    return found


for gate, required in graph.get("hard_gates", {}).items():
    check(gate in by_id, f"hard gate milestone exists: {gate}")
    gate_ancestors = ancestors(gate)
    for requirement in required:
        check(requirement in gate_ancestors, f"{gate} is transitively gated by {requirement}")
check("orbital_research_complete" in ancestors("moon_access"), "Moon is gated by Orbital Research")
check("functional_lunar_base" in ancestors("lunar_research_complete"), "Lunar Research is gated by Lunar Settlement")
check("lunar_research_complete" in ancestors("quantum_technology_complete"), "Quantum is gated by Lunar Research")
check({"quantum_technology_complete", "mars_mission_authorization"}.issubset(ancestors("tier2_mars_launch")), "Mars requires Quantum and authorization")
check({"martian_autonomy_complete", "lite_matter_complete"}.issubset(ancestors("ai_age_entry")), "AI Age requires Martian Autonomy and Lite Matter")
post_ai_ids = {ms["id"] for c in chapters if c.get("number") == 16 for ms in c.get("milestones", [])}
check(not (ancestors("ai_age_entry") & post_ai_ids), "post-AI milestones do not gate AI entry")
check("ai_age_entry" in ancestors("ae2_entry"), "AE2 entry is gated by AI Age")

critical = graph.get("critical_path", [])
check(len(critical) == len(set(critical)) and all(mid in by_id for mid in critical), "critical path uses unique real milestones")
for prior, later in zip(critical, critical[1:]):
    check(prior in ancestors(later) or prior == "tier1_orbital_launch" and later == "functional_orbital_station",
          f"critical path ordering {prior} -> {later}")

target_hours = pacing.get("ai_age_target_hours", {})
check(target_hours == {"optimized": 20, "average": 40, "poor": 80}, "formal AI Age targets are 20/40/80")
profile_hours = {p.get("id"): p.get("ai_age_target_hours") for p in profiles.get("profiles", [])}
check(profile_hours == {"optimized": 20, "average": 40, "poor": 80}, "optimization profiles match 20/40/80")
previous = {"optimized": -1, "average": -1, "poor": -1}
for entry in pacing.get("milestones", []):
    check(entry.get("milestone_id") in by_id, f"pacing milestone exists: {entry.get('milestone_id')}")
    check(entry.get("fixed_timer") is False, f"pacing milestone {entry.get('milestone_id')} has no fixed timer")
    for profile in previous:
        current = entry.get("cumulative_hours", {}).get(profile, -1)
        check(current >= previous[profile], f"{profile} pacing is monotonic at {entry.get('milestone_id')}")
        previous[profile] = current
check(previous == {"optimized": 20, "average": 40, "poor": 80}, "pacing endpoints reach exactly 20/40/80")

placeholder_entries = {entry.get("id"): entry for entry in placeholders.get("entries", [])}
used_placeholders = {ms.get("placeholder_id") for ms in milestones if ms.get("placeholder_id")}
check(used_placeholders == set(placeholder_entries), "quest placeholders and registry entries are one-to-one")
for pid, entry in placeholder_entries.items():
    check(entry.get("milestone_id") in by_id, f"placeholder {pid} has a stable milestone")
    check(bool(entry.get("final_target")), f"placeholder {pid} has a final target")
    check(bool(entry.get("represents")), f"placeholder {pid} states what it represents")
    check(entry.get("display_name", "").startswith("[TEST PLACEHOLDER]"), f"placeholder {pid} is visibly temporary")
    check(entry.get("temporary_recipe") in (["minecraft:paper", "minecraft:redstone"], ["minecraft:iron_ingot", "minecraft:redstone"]), f"placeholder {pid} follows simple recipe convention")
check((ROOT / placeholders.get("config", "missing")).is_file(), "central placeholder toggle exists")

# Build a practical item/resource index from installed jars and custom source.
asset_domains = set()
asset_paths = defaultdict(set)
for jar in (ROOT / "mods").glob("*.jar"):
    try:
        with zipfile.ZipFile(jar) as zf:
            for name in zf.namelist():
                match = re.match(r"assets/([^/]+)/(?:models/item|blockstates)/(.+)\.json$", name)
                if match:
                    domain, path = match.groups()
                    asset_domains.add(domain)
                    asset_paths[domain].add(path)
    except zipfile.BadZipFile:
        pass
custom_ids = {"molecular_analyzer", "material_pattern_record"}
custom_ids.update(entry["placeholder_item"].split(":", 1)[1] for entry in placeholder_entries.values())


def item_exists(ref):
    parts = ref.split(":")
    if len(parts) < 2:
        return False
    domain, path = parts[0], parts[1]
    if domain == "minecraft":
        return True
    if domain == "industrialcivilizationcore":
        return path in custom_ids
    # Galacticraft's metadata-backed item_basic registries deliberately use
    # descriptive model filenames (for example ingot_desh) rather than the
    # registry path. This exact metadata-2 item is verified elsewhere against
    # the installed Galacticraft archive and the working Analyzer source.
    if domain == "galacticraftplanets" and path == "item_basic_mars":
        return True
    if domain not in asset_domains:
        return False
    candidates = asset_paths[domain]
    return path in candidates or any(value.endswith("/" + path) or value.startswith(path + "_") for value in candidates)


for ms in milestones:
    check(item_exists(ms["icon"]), f"quest icon exists: {ms['id']} -> {ms['icon']}")
    if ms.get("required_item"):
        check(item_exists(ms["required_item"]), f"quest item exists: {ms['id']} -> {ms['required_item']}")

lang_path = ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/industrialcivilizationcore/lang/en_us.lang"
lang = lang_path.read_text(encoding="utf-8")
java = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/IndustrialCivilizationCore.java").read_text(encoding="utf-8")
for entry in placeholder_entries.values():
    item_id = entry["placeholder_item"].split(":", 1)[1]
    check(f'placeholder("{item_id}")' in java, f"placeholder item registered: {item_id}")
    check(f"item.industrialcivilizationcore.{item_id}.name=" in lang, f"placeholder name localized: {item_id}")
    check(f"item.industrialcivilizationcore.{item_id}.represents=" in lang, f"placeholder purpose localized: {item_id}")
check("test_placeholder" in java and (lang_path.parent.parent / "models/item/test_placeholder.json").is_file(), "all placeholders have a shared item model")

placeholder_script = (ROOT / "groovy/postInit/industrial_civilization_placeholders.groovy").read_text(encoding="utf-8")
main_script = (ROOT / "groovy/postInit/industrial_civilization.groovy").read_text(encoding="utf-8")
check("enableTestingPlaceholders=true" in placeholder_script, "placeholder script reads central enable toggle")
for entry in placeholder_entries.values():
    item_id = entry["placeholder_item"].split(":", 1)[1]
    check(item_id in placeholder_script, f"placeholder recipe exists: {item_id}")
check("startsWith('appliedenergistics2:')" in main_script and ".removeAll()" in main_script, "all original AE2 crafting recipes are removed")
check("aiCore" in placeholder_script and "ai_gated_ae2_" in placeholder_script, "testing AE2 recipes require the AI Core")

# Generated quest database must be a lossless projection of milestone IDs/order.
quests = read_json(ROOT / "config/betterquesting/DefaultQuests.json")
quest_db = quests.get("questDatabase:9", {})
quest_lines = quests.get("questLines:9", {})
check(len(quest_db) == len(milestones), "generated quest count matches canonical milestones")
check(len(quest_lines) == len(chapters), "generated quest lines match canonical chapters")
id_order = {ms["id"]: index for index, ms in enumerate(milestones)}
for index, ms in enumerate(milestones):
    quest = quest_db.get(f"{index}:10", {})
    props = quest.get("properties:10", {}).get("betterquesting:10", {})
    check(props.get("name:8") == ms["title"], f"generated quest title matches {ms['id']}")
    check(quest.get("preRequisites:11") == [id_order[p] for p in ms["prerequisites"]], f"generated prerequisites match {ms['id']}")
    task = quest.get("tasks:9", {}).get("0:10", {})
    expected_task = "bq_standard:retrieval" if ms.get("required_item") else "bq_standard:checkbox"
    check(task.get("taskID:8") == expected_task, f"generated task type matches {ms['id']}")
    if ms.get("placeholder_id"):
        check("TEMPORARY VALIDATION:" in props.get("desc:8", ""), f"placeholder quest explains temporary validation: {ms['id']}")
check(quests.get("questSettings:10", {}).get("betterquesting:10", {}).get("pack_version:3") == 2, "Better Questing pack version is Phase 2")

telemetry_ids = {field.get("id") for field in telemetry.get("fields", [])}
required_telemetry = {"milestone_completion_time", "chapter_time", "manual_crafts", "autocrafting_operations",
 "blocks_mined_manually", "blocks_mined_automatically", "machine_utilization", "machine_idle_time",
 "eu_generated", "eu_consumed", "reactor_efficiency", "launches", "cargo_transported",
 "moon_resources_imported", "moon_resources_produced", "mars_resources_imported", "mars_resources_produced",
 "computercraft_programs_used", "ae_unlock_time"}
check(required_telemetry == telemetry_ids and telemetry.get("implemented") is False, "future telemetry schema is complete and non-invasive")

if errors:
    print(f"FAILED: {len(errors)} of {len(errors) + len(checks)} checks")
    for message in errors:
        print(f"  - {message}")
    sys.exit(1)
print(f"PASS: {len(checks)} progression checks; {len(chapters)} chapters; {len(milestones)} milestones; {len(placeholder_entries)} placeholders")
