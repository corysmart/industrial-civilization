#!/usr/bin/env python3
"""Generate Better Questing 3 data deterministically from progression/*.json."""
import json
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROGRESSION = ROOT / "progression"
OUT = ROOT / "config" / "betterquesting" / "DefaultQuests.json"
DETECTION = json.loads((PROGRESSION / "objective-detection.json").read_text(encoding="utf-8"))

CANVAS_CENTER = 256
QUEST_NODE_SIZE = 24


def radial_position(index, total, line_seed):
    """Center the opening quest, then distribute progression over two rings."""
    center = CANVAS_CENTER - QUEST_NODE_SIZE // 2
    if index == 0:
        return center, center
    remaining = total - 1
    if remaining <= 6:
        ring, slot, capacity = 1, index - 1, remaining
    elif index <= 6:
        ring, slot, capacity = 1, index - 1, 6
    else:
        ring, slot, capacity = 2, index - 7, remaining - 6
    radius = 78 if ring == 1 else 160
    # Each tab receives a distinct rotation, direction, and ellipse. Connector
    # lines therefore form wheels, arcs, diamonds, and orbital-looking loops
    # without sacrificing the centered start or deterministic generation.
    rotation = math.radians((line_seed * 37) % 360)
    direction = -1 if line_seed % 2 else 1
    angle = rotation + direction * (2 * math.pi * slot / max(1, capacity))
    y_scale = (0.72, 0.80, 0.88)[line_seed % 3]
    x = round(CANVAS_CENTER + math.cos(angle) * radius - QUEST_NODE_SIZE / 2)
    y = round(CANVAS_CENTER + math.sin(angle) * radius * y_scale - QUEST_NODE_SIZE / 2)
    return x, y

STORY_OPENINGS = {
    "survival_workshop": "You arrive with tools, hunger, and a blank map. Before there can be a civilization, there must be one defensible room where useful materials stop being loot and become inventory.",
    "electrification": "Hand labor can keep one survivor alive, but it cannot preserve knowledge or scale production. Copper, rubber, and controlled voltage turn the workshop into the first living system of the new industrial order.",
    "automated_industry": "The machines run, yet every full chest and idle furnace proves that human attention is still the bottleneck. The next victory is not a stronger pickaxe; it is work that continues after you walk away.",
    "heavy_industry": "Automation makes the workshop valuable enough to defend and hungry enough to outgrow its walls. Steel, fuel, freight, force fields, and organized security transform a homestead into strategic infrastructure.",
    "programmable_manufacturing": "A factory large enough to matter is too complex to steer with levers alone. Computers become the nervous system: observing queues, scheduling production, and turning many machines into one coordinated plant.",
    "nuclear_age": "Orbital industry demands an energy source that does not sleep with the weather. Nuclear power is the first technology here that can build the future or erase the workshop, so instrumentation and shutdown discipline matter as much as output.",
    "orbital_age": "Earth has taught the settlement everything available at the bottom of one gravity well. The first launch is not a sightseeing trip; it is the construction convoy for a permanent laboratory above the atmosphere.",
    "orbital_research": "Reaching orbit proves only that the rocket worked. Keeping experiments powered, supplied, and repeatable turns the station into a place that can produce knowledge unavailable on Earth.",
    "lunar_settlement": "The orbital archive points toward the Moon, but flags do not make settlements. A real lunar foothold must breathe, generate power, mine locally, manufacture replacements, and send cargo without depending on a rescue flight.",
    "lunar_research": "The Moon's long darkness and unfamiliar materials expose the limits of terrestrial engineering. Sustained lunar science now becomes the bridge to extreme voltage and quantum machinery.",
    "quantum_technology": "Quantum technology is not a trophy; it is the industrial answer to distance. Extreme-voltage power, lunar components, and protected operators must become reliable enough to support a mission that cannot quickly return home.",
    "mars_settlement": "Mars is too distant for an improvised outpost. The authorization assembled from lunar science commits the civilization to a second world, where every habitat, machine, and cargo route must earn its mass.",
    "martian_autonomy": "The Martian colony survives, but survival still depends on decisions made by a human standing nearby. Autonomy research tests whether the system can recognize shortages, power loss, and production demand before they become disasters.",
    "lite_matter_engineering": "Desh carries a material signature the old machines cannot explain. The Molecular Analyzer begins a careful science of recording matter—knowledge powerful enough to prepare replication, but deliberately unable to perform it yet.",
    "ai_age": "Earth industry, orbital experiments, lunar quantum engineering, and Martian autonomy finally describe one connected civilization. An industrial intelligence can now be built to coordinate that history, but creating it begins the endgame rather than ending it.",
    "post_ai_civilization": "The AI core removes the limit of human scheduling, not the limits of matter, energy, or distance. From here the civilization branches outward: logistics, replication, fusion, megastructures, and colonies become parallel programs with no final horizon.",
    "factions_and_salvage": "Not every industrial route begins at a crafting table. Settlements, criminal networks, and abandoned factories preserve fragments of the old world; diplomacy or force can recover them, but inherited machinery carries inherited risks.",
    "strategic_defense": "Heavy industry has made long-range force possible. Strategic defense asks whether radar, launch control, and missiles can protect civilization without allowing dangerous payloads to bypass the scientific progression that makes them governable.",
    "field_engineering": "Civilization is built between major breakthroughs as much as at them. Field engineering rewards the tools, remote controls, and recovery practices that keep ambitious systems from becoming fragile ones.",
    "orbital_power": "A station that merely survives is still dependent on Earth. Orbital power engineering exploits constant sunlight and active tracking to make the platform an industrial contributor.",
    "burst_power_banking": "Extreme voltage becomes most interesting when storage is treated as parallel infrastructure rather than one large battery. Burst-power banking turns safe individual MFSU packets into manufacturing speed without disguising an illegal voltage shortcut.",
    "cargo_logistics": "Separate worlds become one civilization only when material can move between them predictably. Cargo logistics turns launches into scheduled infrastructure instead of heroic one-off expeditions.",
    "post_ai_parallel": "The AI Age creates several urgent frontiers at once. These programs are intentionally parallel: the player decides whether matter, logistics, fusion, colonies, or megastructures receive the next industrial generation.",
}

STORY_TRANSITIONS = {
    "survival_workshop": "The workshop is secure. The next chapter asks it to produce power instead of merely consuming daylight.",
    "electrification": "Electricity is stable enough to trust. Now the factory must learn to move and process material without constant supervision.",
    "automated_industry": "Automation has created throughput. Heavy industry will decide whether that throughput can survive scale, politics, and attack.",
    "heavy_industry": "The installation now has industrial weight. Its next weakness is coordination, not machinery.",
    "programmable_manufacturing": "The plant can be programmed and observed. It is ready to take responsibility for nuclear-scale power.",
    "nuclear_age": "The reactor can sustain orbital construction. The sky is now a logistics problem rather than a boundary.",
    "orbital_age": "A station exists above Earth. It must now justify every launch by producing operational research.",
    "orbital_research": "The orbital archive is complete. Lunar settlement is now a researched commitment, not a gamble.",
    "lunar_settlement": "The Moon can support industry. Its environment can now be used as an instrument of science.",
    "lunar_research": "Lunar knowledge has opened the quantum route. Mars remains locked until that route becomes dependable industry.",
    "quantum_technology": "Quantum industry closes the distance to Mars. The next launch carries the seed of an autonomous colony.",
    "mars_settlement": "The Martian base can survive and manufacture. Now it must learn to recognize and answer its own needs.",
    "martian_autonomy": "Autonomy exposes the boundary of conventional control. Matter itself becomes the next engineering subject.",
    "lite_matter_engineering": "Matter can be measured and recorded, but not yet copied. The accumulated archives can now teach an industrial intelligence.",
    "ai_age": "The AI core is online and AE2 foundation technology is authorized. The civilization has reached an opening, not an ending.",
    "post_ai_civilization": "There is no victory screen here. Each completed program creates the capacity—and the obligation—to begin another.",
    "factions_and_salvage": "The recovered infrastructure offers alternate access to industrial capacity, but it cannot replace the scientific gates ahead.",
    "strategic_defense": "A controlled conventional deterrent now exists. Nuclear and exotic payloads remain separate scientific responsibilities, not automatic upgrades.",
    "field_engineering": "The field toolkit is resilient enough for expansion, but it remains an optional advantage rather than a scientific gate.",
    "orbital_power": "Orbital generation can now carry real production loads and support later off-world industry.",
    "burst_power_banking": "The bank now converts stored energy into near-instant industrial work while every individual packet remains within the machine's native voltage limit.",
    "cargo_logistics": "The cargo network makes distance routine; future colonies can be supplied as systems rather than emergencies.",
    "post_ai_parallel": "This program expands the civilization without closing any of its other frontiers.",
}

ERA_BACKGROUNDS = {
    "earth": "industrialcivilizationcore:textures/gui/quest_bg_earth_ui.png",
    "orbit": "industrialcivilizationcore:textures/gui/quest_bg_orbit_ui.png",
    "moon": "industrialcivilizationcore:textures/gui/quest_bg_moon_ui.png",
    "mars": "industrialcivilizationcore:textures/gui/quest_bg_mars_ui.png",
    "post_ai": "industrialcivilizationcore:textures/gui/quest_bg_post_ai_ui.png",
}

CHAPTER_ERAS = {
    **{name: "earth" for name in ("survival_workshop", "electrification", "automated_industry", "heavy_industry", "programmable_manufacturing", "nuclear_age")},
    **{name: "orbit" for name in ("orbital_age", "orbital_research")},
    **{name: "moon" for name in ("lunar_settlement", "lunar_research", "quantum_technology")},
    **{name: "mars" for name in ("mars_settlement", "martian_autonomy", "lite_matter_engineering")},
    **{name: "post_ai" for name in ("ai_age", "post_ai_civilization")},
    "factions_and_salvage": "earth",
    "strategic_defense": "earth",
}

# Better Questing quest pictures are ItemStacks. These exceptions replace old
# symbolic storyboard icons with the machine, artifact, vehicle, or output that
# actually proves the objective. Most other icons are derived automatically
# from required-item or objective-evidence data below.
ICON_OVERRIDES = {
    "ore_doubling": "ic2:blockmachinelv",
    "voltage_literacy": "ic2:itemcable",
    "loaded_industry": "railcraft:worldspike",
    "wireless_control": "wrcbe:wireless_logic",
    "automation_throughput": "minecraft:hopper",
    "freight_infrastructure": "railcraft:locomotive",
    "faction_contacts": "industrialcivilizationcore:industrial_credit",
    "production_queue": "industrialcivilizationcore:programmable_assembler",
    "multi_step_manufacturing": "industrialcivilizationcore:programmable_assembler",
    "programmable_manufacturing": "industrialcivilizationcore:control_processor",
    "reactor_output": "ic2:blocknuclearreactor",
    "orbital_experiments": "industrialcivilizationcore:orbital_experiment_module",
    "orbital_operational_data": "industrialcivilizationcore:research_data",
    "moon_access": "industrialcivilizationcore:orbital_research_archive",
    "lunar_landing": "galacticraftcore:rocket_t1",
    "lunar_cargo": "galacticraftplanets:rocket_t2",
    "lunar_science_program": "industrialcivilizationcore:lunar_engineering_archive",
    "lunar_darkness_mastery": "industrialcivilizationcore:environmental_solar_array",
    "lunar_precision_manufacturing": "industrialcivilizationcore:precision_frame",
    "martian_cargo": "industrialcivilizationcore:interplanetary_cargo_controller",
    "martian_science_program": "industrialcivilizationcore:martian_autonomy_archive",
    "autonomous_resource_response": "industrialcivilizationcore:programmable_assembler",
    "autonomous_power_response": "industrialcivilizationcore:environmental_solar_array",
    "unattended_martian_production": "industrialcivilizationcore:robotic_manufacturing_cell",
    "analyzer_power": "industrialcivilizationcore:molecular_analyzer",
    "comparative_molecular_analysis": "industrialcivilizationcore:material_pattern_record",
    "lite_matter_complete": "industrialcivilizationcore:material_pattern_record",
}


def load_chapters():
    return [json.loads(path.read_text(encoding="utf-8"))
            for path in sorted((PROGRESSION / "chapters").glob("*.json"))]


def load_side_paths():
    return [json.loads(path.read_text(encoding="utf-8"))
            for path in sorted((PROGRESSION / "side-paths").glob("*.json"))]


def split_item(ref):
    parts = ref.split(":")
    if len(parts) == 3 and parts[-1] == "*":
        return ":".join(parts[:2]), 32767
    if len(parts) == 3 and parts[-1].isdigit():
        return ":".join(parts[:2]), int(parts[-1])
    return ref, 0


def stack(ref, count=1, ore_dict=""):
    item_id, damage = split_item(ref)
    return {"id:8": item_id, "Count:3": count, "Damage:2": damage, "OreDict:8": ore_dict}


def retrieval_task(items):
    required = {}
    for index, value in enumerate(items):
        spec = value if isinstance(value, dict) else {"item": value}
        required[f"{index}:10"] = stack(spec["item"], spec.get("count", 1), spec.get("ore_dict", ""))
    return {
        "partialMatch:1": 1,
        "ignoreNBT:1": 1,
        "consume:1": 0,
        "groupDetect:1": 1,
        "autoConsume:1": 0,
        "requiredItems:9": required,
        "index:3": 0,
        "taskID:8": "bq_standard:retrieval",
    }


def task_for(ms):
    if ms.get("runtime_advancement"):
        advancement = ms["id"] if ms["runtime_advancement"] is True else ms["runtime_advancement"]
        return {
            "advancement_id:8": f"industrialcivilizationcore:{advancement}",
            "index:3": 0,
            "taskID:8": "bq_standard:advancement",
        }
    if "required_item" in ms:
        return retrieval_task([ms["required_item"]])
    # Cross-mod capabilities without a reliable Forge event use tangible
    # inventory evidence. Every formerly manual quest is therefore detected
    # by Better Questing itself and never asks the player to self-certify.
    evidence = DETECTION.get("overrides", {}).get(ms["id"], [ms["icon"]])
    return retrieval_task(evidence)


def evidence_item(value):
    return value["item"] if isinstance(value, dict) else value


def quest_icon(ms):
    if ms["id"] in ICON_OVERRIDES:
        return ICON_OVERRIDES[ms["id"]]
    if ms.get("required_item"):
        return ms["required_item"]
    evidence = DETECTION.get("overrides", {}).get(ms["id"])
    return evidence_item(evidence[0]) if evidence else ms["icon"]


def quest_icon_stack(ms):
    # Wildcard metadata is valid for retrieval matching but invalid for block
    # rendering. Better Questing attempts to render Damage 32767 directly and
    # IC2 block state containers reject it, flooding the client log.
    icon = quest_icon(ms)
    item_id, damage = split_item(icon)
    return stack(item_id if damage == 32767 else icon)


def story(ms, line, index, total):
    line_id = line["id"]
    opening = STORY_OPENINGS[line_id]
    transition = STORY_TRANSITIONS[line_id]
    if index == 0:
        moment = opening
    elif index == total - 1:
        moment = (f"The {line['title'].lower()} program reaches its decisive proof. "
                  f"{transition}")
    else:
        moment = (f"The {line['title'].lower()} program advances one dependable system at a time. "
                  f"This step turns {ms['title'].lower()} from an idea into infrastructure.")
    return moment


def controls_for(ms):
    haystack = " ".join([ms["id"], ms["title"], ms["capability"], ms["icon"], quest_icon(ms)]).lower()
    controls = ["F6 — reopen the Industrial Civilization quest guide."]
    modded = (":" in ms["icon"] and not ms["icon"].startswith("minecraft:")) or any(
        word in haystack for word in ("ic2", "electric", "voltage", "reactor", "computer", "factory", "orbit", "lunar", "moon", "mars", "quantum", "matter", "cargo", "faction", "wireless"))
    if modded:
        controls.append("HEI: hover an item and press R for its recipe or U for its uses; Option+I bookmarks it.")
    if any(word in haystack for word in ("industrialcivilizationcore", "fabricator", "assembler", "robotic", "research station", "experiment module", "replicator", "fusion", "cargo controller", "megastructure", "colony beacon")):
        controls.append("Industrial Civilization machines: right-click to open the IC2-styled EU/inventory screen. Put recipe inputs in the three left slots and take output from the right slot. Matching inputs run automatically; ComputerCraft is required only for explicit recipe selection, queues, telemetry, or cargo channels.")
    if any(word in haystack for word in ("archive", "record", "authorization", "dossier", "ledger", "certificate", "charter", "network key")):
        controls.append("Research artifacts: hold the finished record and right-click once to register its knowledge with the progression system.")
    if any(word in haystack for word in ("computercraft", "computer", "programmable", "telemetry", "queue", "autonomous", "unattended")):
        controls.append("ComputerCraft: right-click a computer to open it; type help for built-in help; hold Control+T to terminate a running program. Use peripheral.getNames() and peripheral.wrap(side) to find attached machines.")
    if any(word in haystack for word in ("programmable assembler", "production_queue", "multi_step_manufacturing", "programmable_manufacturing")):
        controls.append("Assembler peripheral: after local m = peripheral.wrap(\"right\"), use m.listRecipes(), m.selectRecipe(\"control_processor\"), and m.queue(4). Replace right with the side where the assembler is attached.")
    if "cargo" in haystack and "industrialcivilizationcore:interplanetary_cargo_controller" in haystack:
        controls.append("Cargo peripheral: connect a computer to each controller and call setCargoChannel with the same channel name. Keep both controllers loaded in different dimensions; input enters slot 1 and arrives in the remote output slot.")
    if "molecular_analyzer" in haystack or "analyzer" in haystack:
        controls.append("Molecular Analyzer: supply tier-3-compatible IC2 EU, hold Galacticraft Martian Desh metadata 2, and right-click the placed Analyzer. One run consumes one Desh and 6,250 EU.")
    if "solar" in haystack:
        controls.append("Environmental solar: place with an unobstructed sky view and connect an IC2 cable to any face. Right-click once so sustained generation is credited to you.")
    if any(word in haystack for word in ("galacticraft", "rocket", "orbit", "orbital", "moon", "lunar", "mars", "martian", "cargo")):
        controls.append("Galacticraft: right-click a fueled rocket to mount it, Space begins launch, and W/A/S/D steer supported vehicles. Command+I opens the spaceship inventory. Right-click machines/cargo loaders to open them; use the standard Galacticraft wrench on configurable connections.")
    if any(word in haystack for word in ("techguns", "firearm", "armament", "pistol", "shotgun", "rifle", "defensive")):
        controls.append("Techguns: Option+Y forces a reload; right-click aims/uses the weapon and left-click fires.")
    if any(word in haystack for word in ("icbm", "missile", "launcher", "radar")):
        controls.append("ICBM: right-click the Launcher Screen and Radar Station to configure targets, ranges, and firing groups. Supply their converted IC2 EU network before arming; test conventional payloads only in a remote range.")
    if any(word in haystack for word in ("vehicle", "car workshop", "mobility", "service carrier")):
        controls.append("Vehicles: use W/A/S/D to drive, Option+K for the horn, and Option+L to cycle seats. Sneak-right-click the parked Industrial Service Carrier for its 54-slot cargo hold; hold a Crafting Table while doing so for its mobile crafting grid. Fluid containers interact by right-clicking the parked carrier. Park within four blocks of a Vehicle Service Dock to expose cargo and its 64,000 mB tank to BuildCraft pipes.")
    if any(word in haystack for word in ("car workshop", "gun factory", "rust", "repair bench")):
        controls.append("Large workshops: place the controller to deploy the equipment set piece, then provide a solid roof. Rain reaching the controller rusts it; right-click a Repair Bench while holding one IC2 Machine Block to restore the nearest rusted workshop within 12 blocks.")
    if any(word in haystack for word in ("railcraft", "freight", "locomotive", "rail logistics")):
        controls.append("Railcraft locomotive: Option+[ reverse, Option+period faster, Option+] slower, Option+; mode, and Option+' whistle. Right-click the locomotive for its configuration screen.")
    if any(word in haystack for word in ("faction", "criminal", "settlement", "npc")):
        controls.append("Factions: Pause > Factions & Settlements shows reputation, attitude, membership rules, known settlement types, and trades. Normal right-click opens IC Credit trades; sneak-right-click requests membership. Trusted members at 60 reputation can recruit a companion by holding 8 IC Credits while sneak-right-clicking.")
        controls.append("Custom NPCs: F8 opens the separate NPC quest log when an authored encounter uses it.")
    if any(word in haystack for word in ("ic2", "electric tool", "drill", "chainsaw", "nanosuit", "quantumsuit", "quantum suit")):
        controls.append("IC2 equipment: F10 changes supported modes. Backslash is the armor modifier, Option+H toggles boost, and Option+O expands the armor HUD. Sneak while mining/chopping to suppress pack-added area or whole-tree behavior.")
    if ms["id"] == "electric_tools":
        controls.append("Pack tool behavior: stone-tier axes and better, plus the IC2 Chainsaw, fell one connected leaf-bearing tree and charge durability/EU per log. The IC2 Drill mines a 3×3 plane and the Diamond Drill a 9×9 plane; Sneak restores one-block precision.")
    if "mfsu" in haystack or "burst bank" in haystack or "blink manufacturing" in haystack:
        controls.append("MFSU bank: orient each MFSU output toward an independent EnergyNet connection to the same tier-3-compatible machine. Start the real recipe before discharge. The machine GUI reports aggregate Input EU/t and speed; an adjacent ComputerCraft computer can also call getMfsuPacketsThisTick() and getOperationPeakMfsuPackets().")
    if any(word in haystack for word in ("applied energistics", "ae2", "storage network", "crafting unit")):
        controls.append("AE2: right-click terminals and network blocks to configure them; Command+P toggles search-field focus.")
    if modded:
        controls.append("Waila: Command+[ toggles the overlay, Command+] liquid details, Command+; recipe, Command+0 configuration, and Command+' uses.")
    return controls


def description(ms, line, index, total):
    lines = [
        "STORY",
        story(ms, line, index, total),
        "",
        "MISSION",
        f"{ms['capability']}",
        "",
        "Completion is detected automatically from real gameplay evidence; no checkbox or command is required.",
        "",
        "PROOF OF COMPLETION",
    ]
    lines.extend(f"- {value}" for value in ms["final_validation"])
    lines.extend(["", "CONTROLS AND OPERATION"])
    lines.extend(f"- {value}" for value in controls_for(ms))
    if ms.get("temporary_validation"):
        lines.extend(["", "TEMPORARY VALIDATION:", ms["temporary_validation"]])
    if ms.get("optional"):
        lines.extend(["", "Optional: this quest does not gate the canonical critical path."])
    return "\n".join(lines)


def quest(qid, ms, ids, line, index, total):
    props = {
        "issilent:1": 0,
        "snd_complete:8": "minecraft:entity.player.levelup",
        "lockedprogress:1": 1,
        "tasklogic:8": "AND",
        "repeattime:3": -1,
        "visibility:8": "ALWAYS",
        "simultaneous:1": 0,
        "icon:10": quest_icon_stack(ms),
        "globalshare:1": 0,
        "questlogic:8": ms.get("prerequisite_logic", "AND"),
        "partysinglereward:1": 0,
        "snd_update:8": "minecraft:entity.player.levelup",
        "autoclaim:1": 0,
        "ismain:1": 1 if ms["critical"] else 0,
        "name:8": ms["title"],
        "desc:8": description(ms, line, index, total),
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
    branch_story_lines = {
        "field_engineering": {"id": "field_engineering", "title": "Field Engineering"},
        "orbital_power": {"id": "orbital_power", "title": "Orbital Power"},
        "cargo_logistics": {"id": "cargo_logistics", "title": "Cargo Logistics"},
        "post_ai_parallel": {"id": "post_ai_parallel", "title": "Post-AI Horizons"},
    }
    branch_by_mid = {mid: branch_id for branch_id, mids in graph["optional_branches"].items()
                     for mid in mids if branch_id in branch_story_lines}
    branch_positions = {branch_id: {mid: index for index, mid in enumerate(mids)}
                        for branch_id, mids in graph["optional_branches"].items()}
    milestones = ([ms for chapter in chapters for ms in chapter["milestones"]] +
                  [ms for path in side_paths for ms in path["milestones"]])
    ids = {ms["id"]: index for index, ms in enumerate(milestones)}

    quest_database = {}
    quest_lines = {}
    for chapter in chapters:
        line_quests = {}
        visible_milestones = [ms for ms in chapter["milestones"] if not ms["optional"]]
        visible_index = 0
        for index, ms in enumerate(chapter["milestones"]):
            qid = ids[ms["id"]]
            branch_id = branch_by_mid.get(ms["id"])
            if branch_id:
                line = branch_story_lines[branch_id]
                story_index = branch_positions[branch_id][ms["id"]]
                story_total = len(graph["optional_branches"][branch_id])
            else:
                line, story_index, story_total = chapter, index, len(chapter["milestones"])
            quest_database[f"{qid}:10"] = quest(qid, ms, ids, line, story_index, story_total)
            if ms["optional"]:
                continue
            x, y = radial_position(visible_index, len(visible_milestones), chapter["number"] - 1)
            visible_index += 1
            line_quests[f"{qid}:10"] = {
                "sizeX:3": 24, "x:3": x, "y:3": y,
                "id:3": qid, "sizeY:3": 24,
            }
        line_id = chapter["number"] - 1
        quest_lines[f"{line_id}:10"] = {
            "quests:9": line_quests,
            "lineID:3": line_id,
            "properties:10": {"betterquesting:10": {
                "visibility:8": "ALWAYS",
                "name:8": f"{chapter['number']:02d} — {chapter['title']}",
                "bg_image:8": ERA_BACKGROUNDS[CHAPTER_ERAS[chapter["id"]]],
                "bg_size:3": 512,
                "desc:8": chapter["purpose"],
            }},
            "order:3": line_id,
        }

    for side_path in side_paths:
        for index, ms in enumerate(side_path["milestones"]):
            qid = ids[ms["id"]]
            quest_database[f"{qid}:10"] = quest(qid, ms, ids, side_path, index, len(side_path["milestones"]))

    # Side paths are first-class quest lines, not hidden nodes inside the
    # numbered chapters. A quest appears in exactly one visual line.
    side_path_by_id = {path["id"]: path for path in side_paths}
    branch_titles = {
        "field_engineering": ("Side Path — Field Engineering", "Optional tools, resilience, remote control, and recovery capabilities."),
        "factions_and_salvage": (side_path_by_id["factions_and_salvage"]["title"], side_path_by_id["factions_and_salvage"]["purpose"]),
        "orbital_power": ("Side Path — Orbital Power", "Optional orbital generation and tracking-array development."),
        "burst_power_banking": ("Side Path — MFSU Burst Power", "Optional parallel-storage challenges that turn legal tier-3 packets into extreme manufacturing throughput."),
        "cargo_logistics": ("Side Path — Cargo Logistics", "Optional freight and interplanetary cargo mastery."),
        "mobility_and_nations": ("Side Path — Mobility and Nations", "Optional road travel, industrial vehicles, city exchange, and advanced workshop infrastructure."),
        "strategic_defense": (side_path_by_id["strategic_defense"]["title"], side_path_by_id["strategic_defense"]["purpose"]),
        "post_ai_parallel": ("Side Path — Post-AI Horizons", "Parallel civilization-scale endgame projects."),
    }
    branch_eras = {
        "field_engineering": "earth",
        "factions_and_salvage": "earth",
        "orbital_power": "orbit",
        "burst_power_banking": "moon",
        "cargo_logistics": "post_ai",
        "mobility_and_nations": "earth",
        "strategic_defense": "earth",
        "post_ai_parallel": "post_ai",
    }
    for branch_index, (branch_id, branch_milestones) in enumerate(graph["optional_branches"].items()):
        line_id = len(chapters) + branch_index
        title, desc = branch_titles[branch_id]
        line_quests = {}
        for index, mid in enumerate(branch_milestones):
            qid = ids[mid]
            x, y = radial_position(index, len(branch_milestones), 100 + branch_index)
            line_quests[f"{qid}:10"] = {
                "sizeX:3": 24, "x:3": x, "y:3": y,
                "id:3": qid, "sizeY:3": 24,
            }
        quest_lines[f"{line_id}:10"] = {
            "quests:9": line_quests,
            "lineID:3": line_id,
            "properties:10": {"betterquesting:10": {
                "visibility:8": "ALWAYS",
                "name:8": title,
                "bg_image:8": ERA_BACKGROUNDS[branch_eras[branch_id]],
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
            "pack_name:8": "Industrial Civilization — Astra",
            "home_anchor_y:5": 0.5,
            "livesmax:3": 10,
            "home_anchor_x:5": 0.5,
            "editmode:1": 0,
            "hardcore:1": 0,
            "home_image:8": "industrialcivilizationcore:textures/gui/quest_home_v2.png",
            "party_enable:1": 1,
            "pack_version:3": 14,
            "home_offset_x:3": -128,
            "home_offset_y:3": -64,
        }},
    }
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
    print(f"Generated {len(quest_database)} quests across {len(chapters)} chapters and {len(quest_lines) - len(chapters)} side paths")


if __name__ == "__main__":
    main()
