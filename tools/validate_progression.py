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
side_path_paths = sorted((PROG / "side-paths").glob("*.json"))
side_paths = [read_json(path) for path in side_path_paths]
graph = read_json(PROG / "progression-graph.json")
pacing = read_json(PROG / "pacing.json")
profiles = read_json(PROG / "optimization-profiles.json")
placeholders = read_json(PROG / "placeholder-registry.json")
objective_detection = read_json(PROG / "objective-detection.json")
telemetry = read_json(PROG / "telemetry-schema.json")
runtime_content = read_json(PROG / "runtime-content.json")
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
for side_path in side_paths:
    for key in schemas.get("side-path.schema.json", {}).get("required", []):
        check(key in side_path, f"side path {side_path.get('id')} schema field {key}")
    for milestone in side_path.get("milestones", []):
        for key in milestone_required:
            check(key in milestone, f"milestone {milestone.get('id')} schema field {key}")
for name, required in (("pacing", ["canonical_profile", "ai_age_target_hours", "milestones"]),
                       ("optimization profiles", ["profiles"]),
                       ("placeholder registry", ["namespace", "config", "entries"]),
                       ("telemetry", ["implemented", "fields"]),
                       ("runtime content", ["namespace", "asset_policy", "blocks", "items"])):
    data = {"pacing": pacing, "optimization profiles": profiles,
            "placeholder registry": placeholders, "telemetry": telemetry,
            "runtime content": runtime_content}[name]
    check(all(key in data for key in required), f"{name} schema required fields")

check(len(chapters) == 16, "exactly 16 canonical chapters")
check([c.get("number") for c in chapters] == list(range(1, 17)), "chapter numbers are contiguous")
check([c.get("id") for c in chapters] == graph.get("canonical_order"), "chapter order matches progression graph")

milestones = ([ms for chapter in chapters for ms in chapter.get("milestones", [])] +
              [ms for path in side_paths for ms in path.get("milestones", [])])
by_id = {ms.get("id"): ms for ms in milestones}
check(len(by_id) == len(milestones), "milestone IDs are globally unique")
for chapter in chapters:
    check(chapter.get("completion_milestone") in by_id, f"chapter {chapter.get('id')} has a real completion milestone")
for ms in milestones:
    check(ms.get("prerequisite_logic", "AND") in {"AND", "OR"}, f"milestone {ms.get('id')} prerequisite logic")
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

alternate_gates = graph.get("alternate_gates", {})
for gate, definition in alternate_gates.items():
    check(gate in by_id, f"alternate gate exists: {gate}")
    check(by_id.get(gate, {}).get("prerequisite_logic") == "OR", f"alternate gate {gate} uses native OR logic")
    check(by_id.get(gate, {}).get("prerequisites") == definition.get("routes"), f"alternate gate routes match: {gate}")
    check(len(definition.get("routes", [])) >= 2, f"alternate gate {gate} has multiple routes")

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
check(not used_placeholders, "no canonical milestone still uses placeholder fulfillment")
check(placeholders.get("status") == "replaced", "placeholder registry is marked replaced")
for pid, entry in placeholder_entries.items():
    check(entry.get("milestone_id") in by_id, f"replacement {pid} retains its stable milestone")
    check(entry.get("replacement_status") == "implemented", f"replacement {pid} is implemented")
    check(bool(entry.get("runtime_item")), f"replacement {pid} has a runtime registry object")
    check(bool(entry.get("implementation")), f"replacement {pid} documents real behavior")
    milestone = by_id[entry["milestone_id"]]
    check(milestone.get("required_item") == entry.get("runtime_item"), f"replacement {pid} fulfills its canonical quest")
check((ROOT / placeholders.get("config", "missing")).is_file(), "runtime progression config exists")

branch_members = [mid for branch in graph.get("optional_branches", {}).values() for mid in branch]
optional_ids = {ms["id"] for ms in milestones if ms.get("optional")}
check(len(branch_members) == len(set(branch_members)), "side-path visual membership has no duplicates")
check(set(branch_members) == optional_ids, "every optional objective is assigned to one independent side path")

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
asset_root = ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/industrialcivilizationcore"
custom_ids = {path.stem for path in (asset_root / "models/item").glob("*.json")}
custom_ids.update(path.stem for path in (asset_root / "blockstates").glob("*.json"))
declared_custom_ids = {entry["id"] for kind in ("blocks", "items") for entry in runtime_content[kind]}
special_compatibility_models = {"technical_phase_pearl"}
check(declared_custom_ids == custom_ids - special_compatibility_models
      and special_compatibility_models <= custom_ids,
      "runtime content registry plus declared compatibility models exactly match custom models")
ic2_lang = zipfile.ZipFile(ROOT / "mods/IC2Classic-1.12.2-1.5.11.jar").read(
    "assets/ic2/lang/en_us.lang").decode("utf-8")
ic2_registry_paths = {match.group(1).lower() for match in re.finditer(
    r"^(?:tile|item)\.([A-Za-z0-9_]+)\.name=", ic2_lang, re.MULTILINE)}


def item_exists(ref):
    parts = ref.split(":")
    if len(parts) < 2:
        return False
    domain, path = parts[0], parts[1]
    if domain == "minecraft":
        return True
    if domain == "industrialcivilizationcore":
        return path in custom_ids
    if domain == "ic2":
        return path.lower() in ic2_registry_paths
    # Galacticraft's metadata-backed item_basic registries deliberately use
    # descriptive model filenames (for example ingot_desh) rather than the
    # registry path. This exact metadata-2 item is verified elsewhere against
    # the installed Galacticraft archive and the working Analyzer source.
    if domain == "galacticraftplanets" and path == "item_basic_mars":
        return True
    # Techguns ammunition is metadata on its registered itemshared container;
    # the descriptive names are recipe aliases, not item registry paths.
    if domain == "techguns" and path == "itemshared":
        return True
    # ICBM Classic supplies this missile through a custom item renderer and
    # therefore does not expose an ordinary models/item JSON.
    if domain == "icbmclassic" and path == "explosive_missile":
        with zipfile.ZipFile(ROOT / "mods/ICBM-classic-1.12.2-6.5.5.jar") as zf:
            return "icbm/classic/content/items/ItemMissile.class" in zf.namelist()
    # WR-CBE uses a custom multipart item renderer and therefore ships no
    # ordinary models/item JSON. Verify its concrete registry constant against
    # the installed class instead of treating it as a missing quest picture.
    if domain == "wrcbe" and path == "wireless_logic":
        with zipfile.ZipFile(ROOT / "mods/WR-CBE-1.12.2-2.3.2.33-universal-patched.jar") as zf:
            return b"wireless_logic" in zf.read("codechicken/wirelessredstone/init/ModItems.class")
    if domain not in asset_domains:
        return False
    candidates = asset_paths[domain]
    return path in candidates or any(value.endswith("/" + path) or value.startswith(path + "_") for value in candidates)


for ms in milestones:
    check(item_exists(ms["icon"]), f"quest icon exists: {ms['id']} -> {ms['icon']}")
    if ms.get("required_item"):
        check(item_exists(ms["required_item"]), f"quest item exists: {ms['id']} -> {ms['required_item']}")
    if ms.get("runtime_advancement"):
        advancement = ms["id"] if ms["runtime_advancement"] is True else ms["runtime_advancement"]
        advancement_path = asset_root / "advancements" / f"{advancement}.json"
        check(advancement_path.is_file(), f"runtime advancement exists: {ms['id']} -> {advancement}")

detection_overrides = objective_detection.get("overrides", {})
check(set(detection_overrides) <= set(by_id), "objective detection overrides reference real milestones")
for milestone_id, evidence in detection_overrides.items():
    check(bool(evidence), f"objective evidence is nonempty: {milestone_id}")
    for value in evidence:
        spec = value if isinstance(value, dict) else {"item": value}
        check(item_exists(spec.get("item", "")),
              f"objective evidence item exists: {milestone_id} -> {spec.get('item')}")
        check(0 < spec.get("count", 1) <= 127, f"objective evidence count is valid: {milestone_id}")

lang_path = ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/industrialcivilizationcore/lang/en_us.lang"
lang = lang_path.read_text(encoding="utf-8")
java_sources = "\n".join(path.read_text(encoding="utf-8") for path in
    (ROOT / "development/IndustrialCivilizationCore/src/main/java").rglob("*.java"))
for entry in placeholder_entries.values():
    item_id = entry["runtime_item"].split(":", 1)[1]
    check(item_id in java_sources, f"runtime replacement registered: {item_id}")
    localized = (f"item.industrialcivilizationcore.{item_id}.name=" in lang
        or f"tile.industrialcivilizationcore.{item_id}.name=" in lang)
    check(localized, f"runtime replacement localized: {item_id}")
    check((asset_root / "models/item" / f"{item_id}.json").is_file(), f"runtime replacement model exists: {item_id}")

content_script = (ROOT / "groovy/postInit/industrial_civilization_content.groovy").read_text(encoding="utf-8")
main_script = (ROOT / "groovy/postInit/industrial_civilization.groovy").read_text(encoding="utf-8")
check("placeholder_" not in java_sources and "[TEST PLACEHOLDER]" not in lang, "placeholder registrations and labels are removed")
for machine_id in ("research_station", "orbital_experiment_module", "electric_fabricator", "programmable_assembler", "robotic_manufacturing_cell"):
    check(machine_id in content_script, f"real machine has a construction recipe: {machine_id}")
check("startsWith('appliedenergistics2:')" in content_script and ".removeAll()" in content_script,
      "all original AE2 crafting recipes are replaced after catalog capture")
check("aiCore" in content_script and "ai_gated_ae2_" in content_script
      and "ai_catalog_" in content_script and "ae2Catalog" in content_script,
      "the complete captured AE2 catalog requires the durable AI Core")
pre_ai_text = "\n".join(json.dumps(chapter) for chapter in chapters if chapter["number"] < 15)
check("ender_pearl" not in pre_ai_text and "ender_eye" not in pre_ai_text,
      "Technical Phase Pearls and Ender Eyes are absent from every pre-AI quest")
phase_pearl = by_id["technical_phase_pearl"]
check("ai_age_entry" in ancestors("technical_phase_pearl")
      and phase_pearl.get("required_item") == "minecraft:ender_pearl",
      "Technical Phase Pearl is an AI-only compatibility item")

# Generated quest database must be a lossless projection of milestone IDs/order.
quests = read_json(ROOT / "config/betterquesting/DefaultQuests.json")
quest_db = quests.get("questDatabase:9", {})
quest_lines = quests.get("questLines:9", {})
check(len(quest_db) == len(milestones), "generated quest count matches canonical milestones")
check(len(quest_lines) == len(chapters) + len(graph.get("optional_branches", {})), "generated quest lines include chapters and independent side paths")
id_order = {ms["id"]: index for index, ms in enumerate(milestones)}
for index, ms in enumerate(milestones):
    quest = quest_db.get(f"{index}:10", {})
    props = quest.get("properties:10", {}).get("betterquesting:10", {})
    check(props.get("name:8") == ms["title"], f"generated quest title matches {ms['id']}")
    generated_icon = props.get("icon:10", {}).get("id:8", "")
    check(item_exists(generated_icon), f"generated quest picture exists: {ms['id']} -> {generated_icon}")
    description = props.get("desc:8", "")
    check(all(section in description for section in ("STORY\n", "MISSION\n", "PROOF OF COMPLETION\n", "CONTROLS AND OPERATION\n")),
          f"generated quest teaches story, proof, and controls: {ms['id']}")
    check(quest.get("preRequisites:11") == [id_order[p] for p in ms["prerequisites"]], f"generated prerequisites match {ms['id']}")
    task = quest.get("tasks:9", {}).get("0:10", {})
    expected_task = ("bq_standard:advancement" if ms.get("runtime_advancement")
                     else "bq_standard:retrieval")
    check(task.get("taskID:8") == expected_task, f"generated task type matches {ms['id']}")
    if ms.get("runtime_advancement"):
        advancement = ms["id"] if ms["runtime_advancement"] is True else ms["runtime_advancement"]
        check(task.get("advancement_id:8") == f"industrialcivilizationcore:{advancement}",
              f"generated runtime advancement matches {ms['id']}")
    check(props.get("visibility:8") == "ALWAYS", f"quest is aspirationally visible from the start: {ms['id']}")
    check(props.get("questlogic:8") == ms.get("prerequisite_logic", "AND"), f"generated prerequisite logic matches {ms['id']}")
check(not any(ms.get("placeholder_id") for ms in milestones), "generated projection has no placeholder milestones")
check(not any(quest.get("tasks:9", {}).get("0:10", {}).get("taskID:8") == "bq_standard:checkbox"
              for quest in quest_db.values()), "generated quest projection contains no manual checkbox tasks")
check(quests.get("questSettings:10", {}).get("betterquesting:10", {}).get("pack_version:3") == 6, "Better Questing pack version includes story, controls, and accurate pictures")

expected_backgrounds = {
    "industrialcivilizationcore:textures/gui/quest_bg_earth_ui.png",
    "industrialcivilizationcore:textures/gui/quest_bg_orbit_ui.png",
    "industrialcivilizationcore:textures/gui/quest_bg_moon_ui.png",
    "industrialcivilizationcore:textures/gui/quest_bg_mars_ui.png",
    "industrialcivilizationcore:textures/gui/quest_bg_post_ai_ui.png",
}
actual_backgrounds = {line.get("properties:10", {}).get("betterquesting:10", {}).get("bg_image:8", "")
                      for line in quest_lines.values()}
check(actual_backgrounds == expected_backgrounds, "quest lines use all five era-specific backgrounds")
for resource in expected_backgrounds:
    filename = resource.rsplit("/", 1)[-1]
    check((ROOT / "resources/industrialcivilizationcore/textures/gui" / filename).is_file(),
          f"quest background exists: {filename}")

placements = []
for line in quest_lines.values():
    placements.extend(entry.get("id:3") for entry in line.get("quests:9", {}).values())
check(len(placements) == len(milestones) and len(set(placements)) == len(milestones), "every quest appears in exactly one chapter or side-path tab")
for chapter in chapters:
    line = quest_lines.get(f"{chapter['number'] - 1}:10", {})
    placed = {entry.get("id:3") for entry in line.get("quests:9", {}).values()}
    check(all(id_order[ms["id"]] not in placed for ms in chapter["milestones"] if ms.get("optional")), f"chapter {chapter['id']} contains no embedded optional objectives")

telemetry_ids = {field.get("id") for field in telemetry.get("fields", [])}
required_telemetry = {"milestone_completion_time", "chapter_time", "manual_crafts", "autocrafting_operations",
 "blocks_mined_manually", "blocks_mined_automatically", "machine_utilization", "machine_idle_time",
 "eu_generated", "eu_consumed", "reactor_efficiency", "launches", "cargo_transported",
 "moon_resources_imported", "moon_resources_produced", "mars_resources_imported", "mars_resources_produced",
 "computercraft_programs_used", "ae_unlock_time"}
check(required_telemetry == telemetry_ids
      and telemetry.get("implemented") == "partial"
      and telemetry.get("privacy") == "local_only_no_transmission",
      "telemetry schema is complete and partial runtime counters remain local-only")

if errors:
    print(f"FAILED: {len(errors)} of {len(errors) + len(checks)} checks")
    for message in errors:
        print(f"  - {message}")
    sys.exit(1)
print(f"PASS: {len(checks)} progression checks; {len(chapters)} chapters; {len(graph.get('optional_branches', {}))} side paths; {len(milestones)} milestones; {len(placeholder_entries)} runtime replacements; 0 placeholders")
