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
    "industrial_agriculture": "A technical civilization cannot depend on fantasy predators for ordinary fiber. IC2 crop engineering turns farmland, genetics, and tapped rubber trees into a repeatable logistics system for string and livestock.",
    "industrial_foregoing_farms": "The first IC2 generator should power more than a macerator. LV agricultural machinery turns trees, crops, livestock, and water into unattended infrastructure while charcoal feeds the young electrical grid.",
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
    "industrial_agriculture": "The farm now supplies renewable fiber, animal handling, and supplemental bone without introducing a hostile-monster economy.",
    "industrial_foregoing_farms": "Peaceful agriculture is now automated from soil to storage, and its charcoal loop can finance the settlement's next generation of electrical machines.",
    "strategic_defense": "A controlled conventional deterrent now exists. Nuclear and exotic payloads remain separate scientific responsibilities, not automatic upgrades.",
    "field_engineering": "The field toolkit is resilient enough for expansion, but it remains an optional advantage rather than a scientific gate.",
    "orbital_power": "Orbital generation can now carry real production loads and support later off-world industry.",
    "burst_power_banking": "The bank now converts stored energy into near-instant industrial work while every individual packet remains within the machine's native voltage limit.",
    "cargo_logistics": "The cargo network makes distance routine; future colonies can be supplied as systems rather than emergencies.",
    "post_ai_parallel": "This program expands the civilization without closing any of its other frontiers.",
}

# These paragraphs teach the engineering reason behind milestone-scale systems.
# Short, self-explanatory quests use the line narrative plus their authored
# action text; unfamiliar systems receive a focused field-guide explanation.
QUEST_STORY_OVERRIDES = {
    "first_ic2_generator": "A hand-fed furnace can process material, but it cannot distribute power. The Generator is the first machine that lets one fuel source support an electrical workshop instead of one isolated task.",
    "low_voltage_network": "Electricity becomes useful only when it can move safely. LV cable gives the workshop a shared power network, while transformers let later voltage tiers feed smaller machines without destroying them.",
    "electric_processing": "Electric machines turn fuel into a workshop-wide resource. A dependable processing suite replaces separate furnace fires with controllable EU and lays the foundation for continuous production.",
    "voltage_literacy": "IC2 machines care about the size of each incoming EU packet, not only total energy. Safe expansion depends on matching machine tiers, cable losses, storage output, and transformer direction before power is applied.",
    "automated_ore_processing": "Ore doubling is valuable, but carrying every stack between machines still makes the player the factory's conveyor belt. Routed processing is the first step toward production that continues unattended.",
    "quarry_extraction": "Industrial demand eventually exceeds what even efficient manual mining can supply. A Quarry turns extraction into permanent infrastructure, delivering a steady material stream to the same EU-centered economy that powers the workshop.",
    "physical_logistics": "Automation fails when inputs pile up in one machine and outputs block another. Pipes, hoppers, and buffers give material a route through the factory and make throughput a property of the system rather than the player.",
    "early_autocrafting": "Repeated recipes are another form of manual labor. Early autocrafting is deliberately physical and modest: ingredients must be routed into a fixed recipe, and the output must have somewhere to go.",
    "programmable_assembler": "Fixed automation is efficient until demand changes. The Programmable Assembler turns recipes into selectable production programs, letting one powered line queue different work and report what it still needs.",
    "production_queue": "A programmable factory should keep working after one order finishes. Queues turn production into a schedule, while shortage reporting shows whether the real bottleneck is material, power, or output space.",
    "nuclear_reactor": "Orbital construction requires power that does not disappear with daylight or weather. Nuclear energy provides that scale, but only when heat, cooling, containment, and shutdown systems are designed before the reactor is started.",
    "reactor_telemetry": "A reactor that appears quiet may still be approaching failure. Continuous heat, output, and operating-state telemetry turns invisible risk into information that automation can act upon.",
    "emergency_shutdown": "Monitoring is useful only if the control system can respond. A remote shutdown must remove the reactor's ability to continue producing heat or power even when the operator cannot safely approach it.",
    "nuclear_containment": "Automation reduces risk; containment limits the consequences when prevention fails. Build the reactor as an industrial facility with controlled access and a deliberate barrier between the core and the rest of the settlement.",
    "orbital_habitat": "Orbit has sunlight and research potential, but no breathable environment. A real station begins with a sealed volume where oxygen production can be measured at the place people and equipment must actually survive.",
    "orbital_power": "Orbital sunlight is strong, but every station still crosses darkness. Generation and storage must be designed together so life support, telemetry, and research continue through an eclipse.",
    "research_station": "Crafting another machine can no longer answer every engineering question. The Research Station turns environmental observations and operating records into artifacts that authorize the next technological era.",
    "functional_orbital_station": "A collection of blocks is not yet a station. Habitation, power, communications, storage, experiments, and safe arrival infrastructure must operate together long enough to show that people can remain in orbit.",
    "lunar_mining": "The Moon becomes useful when it can supply its own industry. Automated mining reduces dependence on launch mass and begins converting an expedition into an extraterrestrial settlement.",
    "lunar_darkness_mastery": "Lunar night lasts far longer than an Earth night. Surviving one complete darkness cycle requires stored energy, disciplined loads, and enough local infrastructure to avoid turning every eclipse into an evacuation.",
    "extreme_voltage_industry": "Quantum manufacturing needs energy at a scale ordinary workshops cannot deliver. EV systems demand deliberate transformers, tier-appropriate machines, and packet safety; a single oversized packet can destroy equipment regardless of the average EU/t.",
    "tier2_mars_launch": "Mars is not the next sightseeing destination. Its distance makes rescue and resupply unreliable, so the mission begins only after lunar engineering and Quantum technology can support a colony designed to stand on its own.",
    "autonomous_resource_response": "A distant factory cannot wait for an operator to notice every empty input. Resource response lets production recognize shortages and choose whether to replenish, reroute, or postpone work before the entire line stalls.",
    "autonomous_power_response": "Power failures on Mars threaten every connected system at once. Autonomous load shedding protects life support and essential machinery first, then restores production in a controlled order when generation returns.",
    "molecular_analyzer": "Matter engineering begins with measurement, not replication. The Molecular Analyzer records how materials from Earth, the Moon, and Mars differ before the civilization attempts to reproduce any of them.",
    "comparative_molecular_analysis": "One sample describes a material; samples from three worlds reveal which properties belong to the substance and which come from its environment. Comparative records turn scattered observations into reliable engineering knowledge.",
    "artificial_industrial_intelligence_core": "Until now, automation has coordinated individual machines and queues. The AI Core combines the civilization's accumulated industrial, orbital, lunar, Martian, and matter knowledge so entire production systems can be coordinated together.",
    "ai_age_entry": "The AI Age is not a magical shortcut. It is the point where every earlier discipline—power, logistics, research, programming, and autonomous response—can be applied at civilization scale.",
    "ae2_entry": "AE2 is the culmination of the automation path, not merely a larger chest. The AI Core authorizes a network where storage, pattern knowledge, and manufacturing requests can be coordinated as one digital industrial system.",
    "ae2_autocrafting": "Early autocrafting moved physical ingredients through fixed machinery. AE2 turns that experience into network manufacturing: encoded patterns describe the plan while the network supplies ingredients and coordinates each required step.",
    "uu_matter_research": "Once energy, computation, and material patterns are understood together, matter itself becomes an industrial input. UU-Matter research explores that conversion without pretending energy or authorization constraints have disappeared.",
    "controlled_replication": "Replication is powerful because it can reproduce scarce authorized patterns; it is dangerous when treated as free matter. A controlled system ties every copy to known data, real energy, and bounded throughput.",
    "fusion_and_antimatter": "Fusion and antimatter move the civilization from chemical and nuclear industry into controlled stellar-scale processes. Containment, energy accounting, and remote operation matter more here than raw output alone.",
    "orbital_megastructures": "Orbit makes structures possible that gravity and atmosphere make impractical on Earth. Megastructure construction turns cargo logistics, autonomous assembly, and orbital power into infrastructure measured across entire regions.",
    "autonomous_colony_expansion": "A colony becomes a civilization when it can establish the next settlement without rebuilding every supply chain from Earth. Autonomous expansion packages power, habitat, industry, and logistics into a repeatable frontier system.",
    "continuous_civilization": "There is no final machine that completes civilization. The achievement is a system that can continue exploring, researching, automating, scaling, colonizing, and connecting new industry without a predefined endpoint.",
    "breed_hemp": "Useful biological materials can be engineered as deliberately as metals. IC2 crop breeding turns mature parent plants and parallel experiments into a renewable path toward Hemp and industrial fiber.",
    "renewable_string": "Hemp becomes strategically useful when it replaces a scavenged supply with a cultivated one. Processing the harvest into String closes the gap between crop engineering and ordinary workshop logistics.",
    "controlled_livestock": "Leads connect renewable fiber and Sticky Resin to practical animal management. A controlled herd provides food and materials without depending on dangerous wildlife for basic industrial supplies.",
    "lv_tree_planting": "Coal can power the first electrical workshop, but every piece burned must be replaced. Automated trees offer a fuel supply that grows back and begin the workshop's first closed industrial loop.",
    "lv_charcoal_tree_farm": "Planting trees solves only half the fuel problem. Harvesting, sapling recovery, charcoal production, and return fuel must form one loop before the Generator can help supply its own future energy.",
    "automated_animal_husbandry": "A renewable herd still needs population control. Automated breeding, growth, and separation keep adults productive without overcrowding the enclosure or consuming every food reserve.",
    "mfsu_bank_baseline": "An MFSU stores enormous energy, but its most important property here is a legal 512-EU output packet. Establish one safe packet as the baseline before attempting parallel burst power.",
    "mfsu_bank_quad": "Voltage and throughput are different quantities. Four MFSUs can deliver four legal 512-EU packets in the same tick—2,048 EU/t in aggregate—without ever exposing the machine to a packet above its tier.",
    "mfsu_bank_ten": "Parallel storage turns electrical architecture into manufacturing speed. Ten independently connected MFSUs can deliver 5,120 EU/t as ten legal packets when the receiving machine can convert extra accepted EU into work.",
    "mfsu_bank_fifty": "At fifty parallel MFSUs, the bank can deliver 25,600 EU/t while every packet remains 512 EU. This is fundamentally different from one illegal 25,600-EU packet and is intentional expert IC2 engineering.",
    "blink_manufacturing": "A mature burst-power bank can compress an energy-limited manufacturing cycle into a fraction of a second. The challenge is to achieve that speed through real concurrent packets, not stored progress or a finished item supplied in advance.",
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
    "industrial_agriculture": "earth",
    "industrial_foregoing_farms": "earth",
}

# Better Questing quest pictures are ItemStacks. These exceptions replace old
# symbolic storyboard icons with the machine, artifact, vehicle, or output that
# actually proves the objective. Most other icons are derived automatically
# from required-item or objective-evidence data below.
ICON_OVERRIDES = {
    "basic_storage": "ironchest:iron_chest:3",
    "first_ic2_generator": "ic2:blockgenerator:0",
    "low_voltage_network": "ic2:itemcable:0",
    "electric_processing": "ic2:blockmachinelv:0",
    "ore_doubling": "ic2:blockmachinelv",
    "electric_tools": "ic2:itemdrills:0",
    "voltage_literacy": "ic2:itemcable",
    "quarry_extraction": "buildcraftbuilders:quarry",
    "loaded_industry": "railcraft:worldspike",
    "wireless_control": "wrcbe:wireless_logic",
    "automation_throughput": "minecraft:hopper",
    "railcraft_steel": "railcraft:tool_pickaxe_steel",
    "refined_fuel": "buildcraftfactory:distiller",
    "advanced_ic2": "ic2:blockmachinemv:0",
    "freight_infrastructure": "railcraft:locomotive_steam_solid",
    "faction_contacts": "industrialcivilizationcore:industrial_credit",
    "mffs_installation": "modularforcefieldsystem:projector",
    "production_queue": "industrialcivilizationcore:programmable_assembler",
    "multi_step_manufacturing": "industrialcivilizationcore:programmable_assembler",
    "programmable_manufacturing": "industrialcivilizationcore:control_processor",
    "nuclear_reactor": "ic2:blocknuclearreactor",
    "reactor_output": "ic2:blocknuclearreactor",
    "orbital_power": "industrialcivilizationcore:environmental_solar_array",
    "orbital_experiments": "industrialcivilizationcore:orbital_experiment_module",
    "orbital_operational_data": "industrialcivilizationcore:research_data",
    "moon_access": "industrialcivilizationcore:orbital_research_archive",
    "lunar_landing": "galacticraftcore:rocket_t1",
    "lunar_cargo": "galacticraftplanets:rocket_t2",
    "lunar_science_program": "industrialcivilizationcore:lunar_engineering_archive",
    "lunar_darkness_mastery": "industrialcivilizationcore:environmental_solar_array",
    "lunar_precision_manufacturing": "industrialcivilizationcore:precision_frame",
    "extreme_voltage_industry": "ic2:blockmachinehv:0",
    "nanosuit_and_tools": "ic2:itemarmornanohelmet:0",
    "quantumsuit": "ic2:itemarmorquantumhelmet:0",
    "mars_sample": "galacticraftplanets:item_basic_mars:2",
    "martian_cargo": "industrialcivilizationcore:interplanetary_cargo_controller",
    "martian_science_program": "industrialcivilizationcore:martian_autonomy_archive",
    "autonomous_resource_response": "industrialcivilizationcore:programmable_assembler",
    "autonomous_power_response": "industrialcivilizationcore:environmental_solar_array",
    "unattended_martian_production": "industrialcivilizationcore:robotic_manufacturing_cell",
    "analyzer_power": "industrialcivilizationcore:molecular_analyzer",
    "comparative_molecular_analysis": "industrialcivilizationcore:material_pattern_record",
    "lite_matter_complete": "industrialcivilizationcore:material_pattern_record",
    "abandoned_factory_discovered": "industrialcivilizationcore:factory_control_terminal",
}

# Control guidance is based on the gameplay object that originally supplied
# useful operating context, not on later visual-only icon curation.
CONTROL_HINT_OVERRIDES = {
    key: ICON_OVERRIDES[key] for key in (
        "ore_doubling", "voltage_literacy", "loaded_industry", "wireless_control",
        "automation_throughput", "freight_infrastructure", "faction_contacts",
        "production_queue", "multi_step_manufacturing", "programmable_manufacturing",
        "reactor_output", "orbital_experiments", "orbital_operational_data", "moon_access",
        "lunar_landing", "lunar_cargo", "lunar_science_program", "lunar_darkness_mastery",
        "lunar_precision_manufacturing", "martian_cargo", "martian_science_program",
        "autonomous_resource_response", "autonomous_power_response",
        "unattended_martian_production", "analyzer_power",
        "comparative_molecular_analysis", "lite_matter_complete",
    )
}


def load_chapters():
    return [json.loads(path.read_text(encoding="utf-8"))
            for path in sorted((PROGRESSION / "chapters").glob("*.json"))]


def load_side_paths():
    return [json.loads(path.read_text(encoding="utf-8"))
            for path in sorted((PROGRESSION / "side-paths").glob("*.json"))]


def split_item(ref):
    legacy_aliases = {
        "minecraft:firework_rocket": ("minecraft:fireworks", 0),
        "minecraft:red_sand": ("minecraft:sand", 1),
    }
    if ref in legacy_aliases:
        return legacy_aliases[ref]
    parts = ref.split(":")
    if len(parts) == 3 and parts[-1] == "*":
        return ":".join(parts[:2]), 32767
    if len(parts) == 3 and parts[-1].isdigit():
        return ":".join(parts[:2]), int(parts[-1])
    return ref, 0


def stack(ref, count=1, ore_dict=""):
    item_id, damage = split_item(ref)
    return {"id:8": item_id, "Count:3": count, "Damage:2": damage, "OreDict:8": ore_dict}


def retrieval_task(items, task_index=0):
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
        "index:3": task_index,
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


def tasks_for(ms):
    if ms["id"] == "basic_storage":
        alternatives = DETECTION["overrides"]["basic_storage"]
        return {f"{index}:10": retrieval_task([item], index)
                for index, item in enumerate(alternatives)}
    return {"0:10": task_for(ms)}


def quest_icon(ms):
    if ms["id"] in ICON_OVERRIDES:
        return ICON_OVERRIDES[ms["id"]]
    if ms.get("required_item"):
        return ms["required_item"]
    # Detection evidence answers "what proves completion?" and is often a
    # generic hopper, computer, machine casing, or the first item in a larger
    # set. The authored icon answers "what visually identifies this quest?"
    # Keep those concerns separate and override only when a concrete objective
    # artifact is clearer than the authored symbol.
    return ms["icon"]


def quest_icon_stack(ms):
    # Wildcard metadata is valid for retrieval matching but invalid for block
    # rendering. Better Questing attempts to render Damage 32767 directly and
    # IC2 block state containers reject it, flooding the client log.
    icon = quest_icon(ms)
    item_id, damage = split_item(icon)
    return stack(item_id if damage == 32767 else icon)


def story(ms, line, index, total):
    if ms["id"] in QUEST_STORY_OVERRIDES:
        return QUEST_STORY_OVERRIDES[ms["id"]]
    line_id = line["id"]
    opening = STORY_OPENINGS[line_id]
    transition = STORY_TRANSITIONS[line_id]
    if index == 0:
        moment = opening
    elif index == total - 1:
        moment = (f"The {line['title'].lower()} program is ready for its defining achievement. "
                  f"{transition}")
    else:
        category_stories = {
            "construction": (
                "A plan begins to matter when it becomes infrastructure that other work can depend on.",
                "Industrial growth needs permanent systems, not a collection of machines placed for one recipe.",
                "The settlement can now invest materials in equipment meant to support every project that follows.",
            ),
            "possession": (
                "The right component can carry the knowledge and capacity of an entire production chain.",
                "Expansion now depends on securing a tool or material that ordinary workshop methods cannot replace.",
                "This stage turns the output of earlier industry into a durable capability for the next frontier.",
            ),
            "operation": (
                "A machine earns its place when it performs useful work as part of the larger factory.",
                "Installed equipment is only potential; reliable operation is what expands the civilization's capacity.",
                "The next bottleneck is practical throughput rather than access to another recipe.",
            ),
            "research": (
                "New environments become useful only after observation is converted into engineering knowledge.",
                "Industry has reached a question that cannot be answered by crafting another familiar machine.",
                "A controlled experiment can turn an unfamiliar constraint into a repeatable design rule.",
            ),
            "transition": (
                "The workshop has outgrown the assumptions that carried it through the previous era.",
                "Earlier systems now provide enough confidence to open a more demanding technological path.",
                "This is a change in industrial scale: the old foundation remains useful, but it is no longer sufficient.",
            ),
            "mastery": (
                "Separate machines must now behave as one dependable system rather than isolated successes.",
                "The real test of mature industry is whether power, materials, logistics, and control remain stable together.",
                "Scale exposes every weak connection, so this stage brings the chapter's systems into one working whole.",
            ),
        }
        lead = category_stories[ms["category"]][index % 3]
        moment = (f"{lead} {ms['title']} advances the {line['title'].lower()} program "
                  "and prepares the systems that follow.")
    return moment


def controls_for(ms):
    evidence = DETECTION.get("overrides", {}).get(ms["id"], [])
    if ms["id"] in CONTROL_HINT_OVERRIDES:
        evidence_ref = CONTROL_HINT_OVERRIDES[ms["id"]]
    elif ms.get("required_item"):
        evidence_ref = ms["required_item"]
    elif evidence:
        value = evidence[0]
        evidence_ref = value.get("item", "") if isinstance(value, dict) else value
    else:
        evidence_ref = ms["icon"]
    haystack = " ".join([ms["id"], ms["title"], ms["capability"], ms["icon"],
                         ms.get("required_item", ""), evidence_ref]).lower()
    controls = []
    if ms["id"] in {"low_voltage_network", "voltage_literacy", "advanced_ic2", "extreme_voltage_industry"}:
        controls.append("IC2 voltage is determined by the largest EU packet a machine receives. Use transformer output faces deliberately, match cables and storage to the intended tier, and step voltage down before a higher-tier source feeds lower-tier equipment; insulation does not make an oversized packet safe.")
    if ms["id"] in {"nuclear_reactor", "reactor_output", "reactor_telemetry", "emergency_shutdown"}:
        controls.append("Design the reactor from a tested component layout before inserting fuel. Observe heat and EU output during the first cycle, and wire the reactor chamber to a remote redstone control that can stop the core without requiring a close approach.")
    if ms["id"] in {"orbital_habitat", "lunar_habitat", "martian_habitat"}:
        controls.append("A sealed room becomes a proven habitat when its Oxygen Sealer supplies breathable air and a nearby Galacticraft Oxygen Detector reaches its active redstone state. Keep storage and the environment-specific support systems inside the maintained base area.")
    if ms["id"] in {"mffs_installation", "nuclear_containment"}:
        controls.append("An MFFS installation needs power, a configured Projector, and a field shape that encloses the area it protects. Test access and shutdown from outside the field before trusting it around valuable machinery.")
    if ms["id"] in {"electric_fabricator", "programmable_assembler", "robotic_manufacturing_cell",
                     "research_station", "orbital_experiment_module", "uu_matter_production",
                     "controlled_replication", "fusion_and_antimatter", "orbital_megastructures",
                     "autonomous_colony_expansion"}:
        controls.append("Industrial Civilization machines: right-click to open the IC2-styled EU/inventory screen. Put recipe inputs in the three left slots and take output from the right slot. Matching inputs run automatically; ComputerCraft is required only for explicit recipe selection, queues, telemetry, or cargo channels.")
    if ms["id"] in {"lv_tree_planting", "lv_charcoal_tree_farm", "automated_field_agriculture",
                     "automated_animal_husbandry", "automated_animal_resources", "automated_water_resources"}:
        controls.append("Industrial Foregoing agriculture: right-click the machine to configure its working area, item or fluid inputs, and output handling. Supply it from IC2 generation through the installed adapter; one LV line has ample capacity for these farm machines.")
    if ms["id"] in {"orbital_research_complete", "moon_access", "lunar_research_complete",
                     "quantum_research_access", "mars_mission_authorization", "material_pattern_record",
                     "martian_autonomy_complete", "ai_prerequisite_audit", "ai_age_entry"}:
        controls.append("When a finished research artifact is meant to open new work, hold it and right-click once to register the knowledge it contains.")
    if ms["id"] in {"computer_online", "factory_telemetry", "programmable_assembler", "production_queue",
                     "reactor_telemetry", "orbital_communications", "autonomous_resource_response",
                     "autonomous_power_response", "ai_factory_coordination"}:
        controls.append("ComputerCraft: right-click a computer to open it; type help for built-in help; hold Control+T to terminate a running program. Use peripheral.getNames() and peripheral.wrap(side) to find attached machines.")
    if ms["id"] in {"programmable_assembler", "production_queue", "multi_step_manufacturing",
                     "programmable_manufacturing"}:
        controls.append("Assembler peripheral: after local m = peripheral.wrap(\"right\"), use m.listRecipes(), m.selectRecipe(\"control_processor\"), and m.queue(4). Replace right with the side where the assembler is attached.")
    if ms["id"] in {"martian_cargo", "cross_planetary_logistics", "nation_trade_network"}:
        controls.append("Cargo peripheral: connect a computer to each controller and call setCargoChannel with the same channel name. Keep both controllers loaded in different dimensions; input enters slot 1 and arrives in the remote output slot.")
    if ms["id"] in {"molecular_analyzer", "analyzer_power", "material_pattern_record"}:
        controls.append("Molecular Analyzer: supply tier-3-compatible IC2 EU, hold Galacticraft Martian Desh metadata 2, and right-click the placed Analyzer. One run consumes one Desh and 6,250 EU.")
    if ms["id"] in {"orbital_power", "orbital_solar_industry", "orbital_tracking_array",
                     "lunar_power", "martian_power"}:
        controls.append("Environmental solar: place with an unobstructed sky view and connect an IC2 cable to any face. Right-click once so sustained generation is credited to you.")
    if ms["id"] in {"tier1_orbital_launch", "lunar_landing", "lunar_cargo",
                     "tier2_mars_launch", "martian_cargo"}:
        controls.append("Galacticraft: right-click a fueled rocket to mount it, Space begins launch, and W/A/S/D steer supported vehicles. Command+I opens the spaceship inventory. Right-click machines/cargo loaders to open them; use the standard Galacticraft wrench on configurable connections.")
    if ms["id"] in {"defensive_readiness", "industrial_armament", "advanced_armament_factory"}:
        controls.append("Techguns: Option+Y forces a reload; right-click aims/uses the weapon and left-click fires.")
    if ms["id"] in {"icbm_launch_control", "icbm_radar_defense", "icbm_conventional_missile"}:
        controls.append("ICBM: right-click the Launcher Screen and Radar Station to configure targets, ranges, and firing groups. Supply their converted IC2 EU network before arming; test conventional payloads only in a remote range.")
    if ms["id"] in {"car_workshop_deployed", "regional_mobility", "industrial_service_carrier"}:
        controls.append("Vehicles: use W/A/S/D to drive, Option+K for the horn, and Option+L to cycle seats. Sneak-right-click the parked Industrial Service Carrier for its 54-slot cargo hold; hold a Crafting Table while doing so for its mobile crafting grid. Fluid containers interact by right-clicking the parked carrier. Park within four blocks of a Vehicle Service Dock to expose cargo and its 64,000 mB tank to BuildCraft pipes.")
    if ms["id"] in {"car_workshop_deployed", "advanced_armament_factory"}:
        controls.append("Large workshops: place the controller to deploy the equipment set piece, then provide a solid roof. Rain reaching the controller rusts it; right-click a Repair Bench while holding one IC2 Machine Block to restore the nearest rusted workshop within 12 blocks.")
    if ms["id"] in {"railcraft_steel", "freight_infrastructure"}:
        controls.append("Railcraft locomotive: Option+[ reverse, Option+period faster, Option+] slower, Option+; mode, and Option+' whistle. Right-click the locomotive for its configuration screen.")
    if ms["id"] in {"faction_contacts", "underworld_lead", "faction_membership"}:
        controls.append("Factions: Pause > Factions & Settlements shows reputation, attitude, membership rules, known settlement types, and trades. Normal right-click opens IC Credit trades; sneak-right-click requests membership. Trusted members at 60 reputation can recruit a companion by holding 8 IC Credits while sneak-right-clicking.")
    if ms["id"] == "underworld_lead":
        controls.append("Custom NPCs: F8 opens the separate NPC quest log when an authored encounter uses it.")
    if ms["id"] in {"electric_tools", "nanosuit_and_tools", "quantumsuit"}:
        controls.append("F10 changes supported IC2 tool modes. Backslash is the armor modifier, Option+H toggles boost, and Option+O expands the armor HUD. Sneak while mining or chopping when you need precise one-block behavior.")
    if ms["id"] == "electric_tools":
        controls.append("Pack tool behavior: stone-tier axes and better, plus the IC2 Chainsaw, fell one connected leaf-bearing tree and charge durability/EU per log. The IC2 Drill mines a 3×3 plane and the Diamond Drill a 9×9 plane; Sneak restores one-block precision.")
    if ms["id"] in {"mfsu_bank_baseline", "mfsu_bank_quad", "mfsu_bank_ten",
                     "mfsu_bank_fifty", "blink_manufacturing"}:
        controls.append("MFSU bank: orient each MFSU output toward an independent EnergyNet connection to the same tier-3-compatible machine. Start the real recipe before discharge. The machine GUI reports aggregate Input EU/t and speed; an adjacent ComputerCraft computer can also call getMfsuPacketsThisTick() and getOperationPeakMfsuPackets().")
    if ms["id"] in {"ae2_entry", "ae2_autocrafting"}:
        controls.append("AE2: right-click terminals and network blocks to configure them; Command+P toggles search-field focus.")
    return controls


def description(ms, line, index, total):
    paragraphs = [story(ms, line, index, total)]
    capability = ms["capability"].strip()
    if capability.rstrip(".") != ms["title"].rstrip("."):
        paragraphs.append(capability if capability.endswith(".") else capability + ".")
    paragraphs.extend(controls_for(ms))
    actions = ms["final_validation"]
    if len(actions) == 1:
        action = actions[0]
        paragraphs.append(action if action.endswith(".") else action + ".")
    else:
        paragraphs.append("Bring the project online in these stages:\n" +
                          "\n".join(f"- {value}" for value in actions))
    return "\n\n".join(paragraphs)


def quest(qid, ms, ids, line, index, total):
    props = {
        "issilent:1": 0,
        "snd_complete:8": "minecraft:entity.player.levelup",
        "lockedprogress:1": 1,
        "tasklogic:8": "OR" if ms["id"] == "basic_storage" else "AND",
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
        "tasks:9": tasks_for(ms),
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
        "industrial_agriculture": (side_path_by_id["industrial_agriculture"]["title"], side_path_by_id["industrial_agriculture"]["purpose"]),
        "industrial_foregoing_farms": (side_path_by_id["industrial_foregoing_farms"]["title"], side_path_by_id["industrial_foregoing_farms"]["purpose"]),
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
        "industrial_agriculture": "earth",
        "industrial_foregoing_farms": "earth",
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
            "pack_version:3": 20,
            "home_offset_x:3": -128,
            "home_offset_y:3": -64,
        }},
    }
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
    print(f"Generated {len(quest_database)} quests across {len(chapters)} chapters and {len(quest_lines) - len(chapters)} side paths")


if __name__ == "__main__":
    main()
