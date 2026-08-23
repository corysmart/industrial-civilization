#!/usr/bin/env python3
"""Generate human-readable campaign design documents from canonical JSON."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROG = ROOT / "progression"
DOCS = ROOT / "docs"
chapters = [json.loads(p.read_text()) for p in sorted((PROG / "chapters").glob("*.json"))]
side_paths = [json.loads(p.read_text()) for p in sorted((PROG / "side-paths").glob("*.json"))]
graph = json.loads((PROG / "progression-graph.json").read_text())
pacing = json.loads((PROG / "pacing.json").read_text())
profiles = json.loads((PROG / "optimization-profiles.json").read_text())
placeholders = json.loads((PROG / "placeholder-registry.json").read_text())
telemetry = json.loads((PROG / "telemetry-schema.json").read_text())
detection = json.loads((PROG / "objective-detection.json").read_text())
milestones = ([m for c in chapters for m in c["milestones"]] +
              [m for p in side_paths for m in p["milestones"]])
by_id = {m["id"]: m for m in milestones}

NOTICE = "<!-- Generated from progression/*.json by tools/generate_progression_docs.py. -->\n\n"

RUNTIME_TEST_ACTIONS = {
    "faction_contacts": "Find and approach members of the Frontier Cooperative, Survey Detachment 7, and Ashline Raiders so all three factions are discovered. Complete at least one real IC Credit trade; merely opening a merchant is insufficient.",
    "industrial_capacity_access": "Complete Heavy Industrial Complex on the main construction route. Remain in the world for at least five seconds so the route-transition telemetry can grant the advancement. Do not use the abandoned-factory side-route during this main-line run.",
    "production_queue": "Place and power a Programmable Assembler, attach a ComputerCraft computer, wrap the assembler peripheral, select `control_processor`, and queue five operations. Supply the required Precision Frames, Blank Data Cartridges, and Redstone. The first completed operation grants this quest.",
    "multi_step_manufacturing": "Leave the same five-control-processor queue running with routed inputs and output space. The third completed operation grants this quest.",
    "programmable_manufacturing": "Keep telemetry attached and let the fifth queued Control Processor finish. Confirm the assembler reports completed operations and can recover after one deliberately removed ingredient is restored.",
    "programmable_capacity_access": "After Programmable Manufacturing completes, remain in the world for at least five seconds so the validated main-route transition is recorded. Do not use the recovered-control-system side-route.",
    "tier1_orbital_launch": "Build and fuel a Galacticraft Tier 1 rocket, launch it, and arrive in an Earth-orbit space-station dimension. Wait up to five seconds after arrival for telemetry sampling.",
    "orbital_habitat": "In orbit, build a sealed room with a working Galacticraft breathable-air system and place an Oxygen Detector inside it. Wait for the detector to emit redstone, then remain within 10 blocks until the next five-second telemetry sample. Merely carrying oxygen equipment or running a sealer without an active detector must not complete this quest.",
    "orbital_communications": "While within 10 blocks of the active orbital Oxygen Detector, place a ComputerCraft computer or recognized telemetry block within 32 blocks. Wait for the next five-second sample.",
    "functional_orbital_station": "Within 10 blocks of an active Oxygen Detector and 32 blocks of the other infrastructure, maintain recognized computer/telemetry, a Research Station, an Orbital Experiment Module, and a powered block or charged Environmental Solar Array. Keep the habitat stable for 24 five-second samples—two continuous minutes.",
    "orbital_experiments": "In orbit, power an Orbital Experiment Module, insert one Blank Data Cartridge, and complete the `record_orbital_data` operation. Take or leave the resulting orbit-tagged Research Data in the output slot.",
    "orbital_operational_data": "The same completed `record_orbital_data` operation grants this evidence. Verify both quests complete from the machine operation, not from a spawned Research Data item.",
    "orbital_solar_industry": "In orbit, place an Environmental Solar Array with clear sky, connect an IC2 load or storage block, and right-click the array once to attribute operation to the tester. Let it generate at least 10,000 EU.",
    "orbital_tracking_array": "In orbit, repeat the previous test with the Advanced Tracking Solar Array. Right-click it once and let lifetime generation reach at least 10,000 EU.",
    "moon_access": "Produce or spawn the Orbital Research Archive only after its quest unlocks, keep it in inventory for at least one second so authorization records, then use Galacticraft travel to enter the Moon. Confirm travel is denied before the archive and accepted afterward.",
    "lunar_landing": "Complete an authorized Moon transfer after Moon Access. Confirm this arrives through Galacticraft travel rather than a dimension command; wait for the advancement task to update.",
    "lunar_habitat": "On the Moon, build a sealed room, place an Oxygen Detector inside it, and wait for the detector to emit redstone. Remain within 10 blocks until the next five-second sample.",
    "lunar_power": "On the Moon, place an Environmental Solar Array with clear sky, connect storage/load, right-click it for attribution, and accumulate at least 10,000 EU generation.",
    "lunar_mining": "While inside the breathable lunar habitat, keep a recognized quarry or automated miner within 32 blocks until the next five-second sample.",
    "lunar_manufacturing": "Inside the lunar habitat, run at least one operation in a nearby Electric Fabricator, Programmable Assembler, or Robotic Manufacturing Cell. The machine must have a completed-operation count above zero.",
    "functional_lunar_base": "Maintain an active Oxygen Detector within 10 blocks plus recognized communications, automated mining, a locally operated manufacturing machine, and power within 32 blocks. Keep all conditions stable for 24 five-second samples—two continuous minutes.",
    "lunar_science_program": "On the Moon, power an Orbital Experiment Module, insert a Blank Data Cartridge, and complete `record_lunar_data` to create Moon-tagged Research Data.",
    "lunar_darkness_mastery": "On the Moon, right-click an unobstructed Environmental Solar Array to attribute it to the tester, then leave it loaded through 12,000 nighttime ticks while the habitat remains usable. Do not use `/time set day` during the interval.",
    "lunar_precision_manufacturing": "After Lunar Science, use a lunar Research Station to complete `lunar_archive` with Moon-tagged Research Data plus the Orbital Research Archive. The resulting Lunar Engineering Archive is intentionally manufactured before its retrieval quest unlocks. Then use a powered lunar Robotic Manufacturing Cell to complete `lunar_quantum_component` with one Control Processor, that Lunar Engineering Archive, and one raw Meteoric Iron.",
    "tier2_mars_launch": "Hold both the Lunar Quantum Component and Mars Mission Authorization long enough for authorization to record, unlock the Tier 2 schematic page, build/fuel a Tier 2 rocket, and travel to Mars. Confirm Mars rejects entry before either authorization item is recorded.",
    "martian_habitat": "On Mars, build a sealed room, place an Oxygen Detector inside it, and wait for the detector to emit redstone. Remain within 10 blocks until the next five-second sample.",
    "martian_power": "On Mars, place an Environmental Solar Array with clear sky, connect storage/load, right-click it for attribution, and accumulate at least 10,000 EU despite the deterministic dust derating.",
    "martian_mining": "While inside the breathable Martian habitat, keep a recognized quarry or automated miner within 32 blocks until the next five-second sample.",
    "martian_manufacturing": "Inside the Martian habitat, run at least one operation in a nearby Electric Fabricator, Programmable Assembler, or Robotic Manufacturing Cell.",
    "functional_martian_base": "Keep Galacticraft Desh metadata 2 in inventory and maintain an active Oxygen Detector within 10 blocks plus communications, automated mining, local manufacturing, and power within 32 blocks for 24 five-second samples—two continuous minutes.",
    "martian_science_program": "On Mars, power an Orbital Experiment Module, insert a Blank Data Cartridge, and complete `record_martian_data` to create Mars-tagged Research Data.",
    "autonomous_resource_response": "On Mars, power a Research Station and complete `martian_autonomy` using Mars-tagged Research Data and one Control Processor. This implemented operation grants the resource-response evidence.",
    "autonomous_power_response": "The same completed `martian_autonomy` operation currently grants the power-response evidence. During the mechanics check, interrupt and restore power and confirm the installation recovers safely.",
    "unattended_martian_production": "The same completed `martian_autonomy` operation grants unattended-production evidence after both response quests are satisfied. Confirm the output is a Martian Autonomy Archive.",
    "analyzer_power": "Place and supply the Molecular Analyzer with at least 6,250 EU. Insert one supported sample and complete an analysis; the first successful analysis grants this quest.",
    "comparative_molecular_analysis": "Complete three successful Analyzer runs using an Earth Iron Ingot, lunar raw Meteoric Iron, and Galacticraft Desh metadata 2. Verify each resulting Material Pattern Record carries the correct origin tag.",
    "lite_matter_complete": "The third distinct Earth/Moon/Mars Analyzer origin grants this completion together with comparative analysis. Confirm spawned pattern records alone do not grant it.",
    "ai_age_entry": "After the prerequisite audit, obtain the AI Core and keep it in inventory for at least one second. The player must already have the Lite Matter completion record and the canonical Martian Autonomy Archive record. Confirm the scrolling credits appear once and return to the playable world.",
    "technical_phase_pearl": "After AI Age Entry, craft the Technical Phase Pearl through its real recipe with the durable AI Core catalyst. Spawning an Ender Pearl must not complete this quest; a real crafting event must.",
    "mfsu_bank_baseline": "Connect one real MFSU to a tier-3-compatible first-party manufacturing machine, begin a genuine recipe, and complete it while the machine records at least one 512-EU packet in an operation tick.",
    "mfsu_bank_quad": "Repeat genuine manufacturing with four independently oriented MFSUs. Verify at least four MFSU-class packets and approximately 2,048 EU/t are accepted in one tick without false overvoltage.",
    "mfsu_bank_ten": "Scale the physical bank to ten independently connected MFSUs and complete a genuine recipe. Verify a peak of ten packets and approximately 5,120 EU/t.",
    "mfsu_bank_fifty": "Scale the physical bank to fifty independently connected MFSUs and complete a genuine recipe. Verify a peak of fifty packets and approximately 25,600 EU/t.",
    "blink_manufacturing": "Use the fifty-MFSU bank on a Robotic Manufacturing Cell or another energy-limited first-party machine. Complete one genuine recipe in eight active ticks or fewer while the same operation records a peak of at least fifty MFSU-class packets.",
}


def write(name, title, body):
    DOCS.mkdir(parents=True, exist_ok=True)
    (DOCS / name).write_text(NOTICE + f"# {title}\n\n" + body.strip() + "\n", encoding="utf-8")


chapter_table = ["| # | Chapter | Purpose | Completion milestone |", "|---:|---|---|---|"]
for c in chapters:
    chapter_table.append(f"| {c['number']} | {c['title']} | {c['purpose']} | `{c['completion_milestone']}` |")

write("PROGRESSION_OVERVIEW.md", "Progression Overview", f"""
The machine-readable files in `progression/` are authoritative. Better Questing data and these documents are generated projections, never independent definitions. Stable milestone IDs survive replacement of temporary fulfillment mechanisms.

Canonical order:

{' → '.join(c['title'] for c in chapters)}

{chr(10).join(chapter_table)}

The model contains **{len(chapters)} numbered chapters**, **{len(graph['optional_branches'])} independent side paths**, **{len(milestones)} milestones**, and **zero test placeholders**. The original {len(placeholders['entries'])} placeholder contracts retain their stable milestone IDs and now point to real runtime blocks, artifacts, machine processes, and gates. Every objective uses `ALWAYS` visibility: future goals are aspirationally visible even while locked. The AI Age begins the endgame; it is not a victory screen.
""")
(PROG / "progression-overview.md").write_text((DOCS / "PROGRESSION_OVERVIEW.md").read_text(), encoding="utf-8")

edges = []
for c in chapters:
    for m in c["milestones"]:
        for pre in m["prerequisites"]:
            if m["critical"] and by_id[pre]["critical"]:
                edges.append(f'    {pre}["{by_id[pre]["title"]}"] --> {m["id"]}["{m["title"]}"]')
write("PROGRESSION_GRAPH.md", "Progression Graph", f"""
The full graph is `progression/progression-graph.json`; every individual edge is stored on its milestone. The condensed critical-gate graph is:

```mermaid
flowchart LR
    workshop["Secure Workshop"] --> electrical["Stable Electrical Workshop"]
    electrical --> automation["Automated Industry"]
    automation --> heavy["Heavy Industry"]
    heavy --> programmable["Programmable Manufacturing"]
    programmable --> nuclear["Monitored Nuclear Power"]
    nuclear --> orbit["Functional Orbital Station"]
    orbit --> orbitalArchive["Orbital Research Archive"]
    orbitalArchive --> moon["Moon Access"]
    moon --> lunarBase["Functional Lunar Base"]
    lunarBase --> lunarArchive["Lunar Engineering Archive"]
    lunarArchive --> quantum["Quantum Technology"]
    quantum --> authorization["Mars Mission Authorization"]
    authorization --> mars["Functional Martian Base"]
    mars --> autonomy["Martian Autonomy Archive"]
    autonomy --> lite["Lite Matter Engineering"]
    lite --> ai["AI Age"]
    ai --> ae2["Applied Energistics"]
    ae2 --> replication["UU-Matter and Replication"]
    replication --> continuous["Continuous Civilization"]
```

Static validation rejects missing references, duplicate IDs, cycles, unreachable milestones, and violated hard gates.
""")

critical_lines = [f"{i}. `{mid}` — {by_id[mid]['title']}" for i, mid in enumerate(graph["critical_path"], 1)]
write("CRITICAL_PATH.md", "Critical Path", "The authoritative critical path is:\n\n" + "\n".join(critical_lines) + "\n\nMoon, Quantum, Mars, AI, and AE2 are separately asserted as hard gates by `tools/validate_progression.py`.")

branch_sections = []
for name, mids in graph["optional_branches"].items():
    branch_sections.append(f"## {name.replace('_', ' ').title()}\n\n" + "\n".join(f"- `{mid}` — {by_id[mid]['title']}" for mid in mids))
write("OPTIONAL_PATHS.md", "Optional Paths", "Optional objectives live in independent Better Questing tabs, never inside numbered chapter tabs. They remain visible from the start. Most teach useful capabilities without gating the critical path; explicitly documented salvage routes can substitute for selected construction chapters.\n\n" + "\n\n".join(branch_sections) + "\n\n## Alternate construction bypasses\n\nDefeating or otherwise resolving the criminal network can reveal an abandoned factory. Restoring its power and production satisfies `industrial_capacity_access` through native OR prerequisite logic. Recovering and auditing its control system similarly satisfies `programmable_capacity_access`. These routes bypass building Chapters 4 and 5, but do not bypass Nuclear safety, Orbital Research, Lunar Research, Quantum Technology, Mars Authorization, Martian Autonomy, Lite Matter, or the AI gate.")

pacing_rows = ["| Major milestone | Optimized | Average | Poor | Primary bottleneck |", "|---|---:|---:|---:|---|"]
for p in pacing["milestones"]:
    h = p["cumulative_hours"]
    pacing_rows.append(f"| {by_id[p['milestone_id']]['title']} | {h['optimized']}h | {h['average']}h | {h['poor']}h | {', '.join(p['likely_bottlenecks'])} |")
write("PACING_TARGETS.md", "Pacing Targets", "These are balancing expectations, never timers. The average profile is canonical.\n\n" + "\n".join(pacing_rows) + "\n\nEvery entry records resources, throughput, power, manual work, automation, bottlenecks, catch-up, and infrastructure reuse in `progression/pacing.json`.")

profile_text = []
for p in profiles["profiles"]:
    profile_text.append(f"## {p['id'].title()} — {p['ai_age_target_hours']} hours\n\n{p['description']}\n\nObserved behaviors: " + ", ".join(p["behaviors"]) + ".")
write("PACING_PROFILES.md", "Pacing Profiles", "These profiles are inferred outcomes, not player-selected difficulty modes.\n\n" + "\n\n".join(profile_text))

opt_sections = []
for c in chapters:
    opt_sections.append(f"## {c['number']:02d} — {c['title']}\n\n" + "\n".join(f"- {v}" for v in c["optimization_opportunities"]) + f"\n\nReuse: {c['infrastructure_reuse']}")
write("OPTIMIZATION_OPPORTUNITIES.md", "Optimization Opportunities", """Time differences emerge from engineering quality: parallelism, power stability, routing, preparation, local production, and reduced downtime.

First-party manufacturing turns legal aggregate IC2 delivery directly into work throughput. Parallel MFSU banks are an intentional high-end optimization: each packet remains subject to native voltage safety while accepted EU/t aggregates for the active operation. Orbital generation therefore improves industry naturally through storage and distribution engineering, without a dimension-specific crafting multiplier.

""" + "\n\n".join(opt_sections))

write("ANTI_GRIND_RULES.md", "Anti-Grind Rules", """
1. Every repeated task receives automation before large-scale demand.
2. Optimization is multiplicative through parallelism, routing, power quality, replenishment, and local production.
3. Mandatory quests represent capabilities—not cables, batteries, plates, or arbitrary micro-components.
4. No fixed waiting exists to manufacture the 20/40/80-hour spread.
5. Poor play remains successful; it is slower only because of manual work, downtime, shortages, travel, and rebuilding.
6. Old factories are expanded, integrated, repurposed, and automated rather than discarded each era.
7. Rewards are information, access, research artifacts, or small test items—not free high-tier infrastructure.
""")

placeholder_rows = ["| Former placeholder | Stable milestone | Runtime object | Implemented behavior |", "|---|---|---|---|"]
for p in placeholders["entries"]:
    placeholder_rows.append(f"| `{p['id']}` | `{p['milestone_id']}` | `{p['runtime_item']}` | {'; '.join(p['implementation'])} |")
write("PLACEHOLDER_SYSTEM.md", "Placeholder Replacement Status", f"""
All eleven early test placeholders have been removed. Their stable milestone IDs were preserved while their fulfillment mechanisms were replaced with real runtime content. No `[TEST PLACEHOLDER]` item, two-item shortcut recipe, shared placeholder model, or placeholder enable toggle remains.

{chr(10).join(placeholder_rows)}

The historical contract remains machine-readable in `progression/placeholder-registry.json` with status `replaced`, which prevents future work from accidentally reintroducing temporary fulfillment.
""")

write("QUEST_IMPLEMENTATION.md", "Quest Implementation", f"""
Better Questing 3 reads `config/betterquesting/DefaultQuests.json`. Run `python3 tools/generate_objectives.py` after canonical edits; never hand-edit generated quests. The current projection has {len(chapters) + len(graph['optional_branches'])} quest lines ({len(chapters)} numbered chapters and {len(graph['optional_branches'])} independent side paths) and {len(milestones)} quests.

Every quest completes automatically. Native machine operations and dimension events use advancement-backed tasks; item, construction, and cross-mod capability objectives use non-consuming Standard Expansion retrieval tasks, including multi-item evidence sets from `progression/objective-detection.json`. There are no manual checkbox tasks. All quests use `ALWAYS` visibility and locked progress, so players can browse the whole civilization plan without completing locked objectives early.

The generator also emits one visible vanilla advancement per quest plus an Industrial Civilization root. Critical milestones form the intended chapter-order spine, while optional milestones branch from their real prerequisites. Pause > Advancements opens this vanilla tree; F6 remains the detailed Better Questing story, tutorial, and controls interface.

Quest IDs are deterministic from chapter/side-path order and milestone order. Cross-line prerequisites use the same global numeric map. `pack_version` is 20. Every generated description contains player-facing story, an objective, practical steps, and contextual Mac/no-numpad operating notes. Internal evidence, detector, and validation terminology remains in the development model and test plan rather than appearing in the quest book. Quest pictures use real required/evidence objects, and each line uses an era-specific pack-owned background. Each tab opens at the artwork center and expands through rotated circular or elliptical geometry. New worlds receive the current defaults directly. Existing single-player worlds detect a newer matching pack version and show Better Questing's update-notice button on the quest home; clicking it imports the new structure while preserving progress. `/bq_admin default load` remains an administrator fallback for dedicated servers and testing.

The integration mod persists research artifacts on each player and returns unauthorized arrivals to Earth. Moon entry requires the Orbital Research Archive. Mars entry requires the Lunar Quantum Component and Mars Mission Authorization. `config/industrialcivilization/runtime.cfg` provides an explicit creative-testing bypass.
""")

write("AUTOCRAFTING_PROGRESSION.md", "Autocrafting Progression", """
| Tier | Ordinary craft time | Distinct capability | Limitation |
|---|---:|---|---|
| Manual Crafting Table | Immediate | Baseline assembly | Player labor |
| Automatic Crafting Table | 10–20 seconds | Removes player presence | Fixed recipe and physical routing |
| Electric Fabricator | 8 seconds at 32 EU/t; faster with legal aggregate input | EU-powered standard automation | One active recipe |
| Programmable Assembler | 12 seconds at 128 EU/t; faster with legal aggregate input | Queues, programs, shortage reporting | Logistics and MV/HV power |
| Robotic Manufacturing Cell | 16 seconds at 512 EU/t; near-instant with extreme legal input | Parallel high-throughput operations | Advanced infrastructure |
| Applied Energistics | Immediate/near-immediate | Network inventory, dependencies, routing, scheduling | Materials and physical processes |

AE2 perfects autocrafting; it does not introduce it. First-party manufacturing and replication use total-EU work, so parallel legal IC2 packets accelerate them while preserving baseline energy cost. Scientific observation and containment retain documented minimum elapsed durations.
""")

write("RESEARCH_PROGRESSION.md", "Research Progression", """
The modular Research Station and Environmental Experiment Module are native IC2 EU-powered machines with four-slot automation inventories, IC2-styled GUIs, persistent total-EU work, environment recognition, and ComputerCraft peripherals. Research Station archive computation scales with legal aggregate IC2 power. Experiment Modules retain a 600-tick observation floor. Forge Energy compatibility remains an invisible adapter.

- Orbit produces the Orbital Research Archive from sustained station operation and experiments.
- The Moon produces the Lunar Engineering Archive from local industry and lunar science.
- Mars produces the Martian Autonomy Archive from autonomous colony behavior and research.

Experiment modules fill data cartridges only in orbit, on the Moon, or on Mars. Research Station recipes validate the data's recorded environment and prerequisite archive or controller. ComputerCraft preserves its existing API and additionally exposes input tier, accepted EU/t, baseline, speed multiplier, work totals, and ETA.
""")

write("ORBITAL_STATION.md", "Orbital Station", """
Earth orbit is the first mandatory space destination. A Tier 1 launch grants orbit—not unrestricted Moon access. The functional station requires pressure, oxygen, storage, safe arrival/docking, communications, a computer, research equipment, solar generation, and eclipse storage.

Long-term benefits are orbital solar, microgravity/vacuum manufacturing, research, cargo transfer, fuel storage, Moon/Mars logistics, shipbuilding, and future power transmission. The real Environmental Solar Array generates 8 EU/t on Earth, 32 EU/t on the Moon, 16 EU/t on Mars with a deterministic dust-cycle derating, and 96 EU/t in orbit. The Advanced Tracking Solar Array reaches 192 EU/t in orbit. Both expose generation and lifetime-output telemetry to ComputerCraft.
""")

write("ORBITAL_RESEARCH.md", "Orbital Research", """
The final program combines a powered experiment run in orbit with Research Station processing. The resulting real Orbital Research Archive is persistent proof and the runtime Moon-access key.

`orbital_research_complete` is the sole research gate for `moon_access`. The dependency is statically validated and destination entry is enforced by the integration mod.
""")

write("LUNAR_PROGRAM.md", "Lunar Program", """
The Moon is a permanent industrial settlement, not a schematic stop. Its functional base combines habitat, oxygen, thermal management, communications, generation/storage, automated mining, local processing, local manufacturing, research readiness, and cargo return.

Exposed lunar solar targets 3–5× Earth output but long darkness creates storage and nuclear/orbital-power value. Optimized players bring a compact startup set, mine locally, manufacture later hardware on the Moon, and automate cargo.
""")

write("LUNAR_RESEARCH.md", "Lunar Research", """
Lunar research covers regolith, low-gravity metallurgy, vacuum furnaces, radiation, seismic monitoring, isotopes, closed-loop habitat operation, mining, communications, power reception, and precision manufacturing. Mastery requires local processing and sustained habitation—not merely owning a machine.

The Lunar Engineering Archive unlocks Quantum research only. It does not unlock Mars directly.
""")

write("QUANTUM_TECHNOLOGY.md", "Quantum Technology", """
Quantum Technology sits strictly after Lunar Research and before Mars. It requires monitored nuclear infrastructure, both Orbital and Lunar archives, high-tier IC2 processing/storage, programmable production, lunar output, and the Robotic Manufacturing Cell.

The Lunar Quantum Component preserves a mandatory Moon-manufacturing dependency. A complete charged QuantumSuit and Mars-readiness trial lead to `quantum_technology_complete`. Mars additionally requires the separate Mars Mission Authorization and Tier 2 spacecraft; a schematic alone is insufficient.
""")

write("MARS_PROGRAM.md", "Mars Program", """
Mars requires a Moon-manufactured Lunar Quantum Component plus a real Mars Mission Authorization produced by a powered Earth Research Station. Unauthorized arrivals are returned to Earth. The colony still requires pressure, oxygen, water, radiation protection, communications, dust-resilient power, storage, automated mining, local manufacturing, research equipment, and return/cargo capability.

The existing real Mars Sample is Galacticraft Desh: registry `galacticraftplanets:item_basic_mars`, metadata `2`. It is obtained through existing Galacticraft Mars progression (no replacement recipe was added), is required after Martian mining, and is consumed as the Analyzer's input while producing a durable Material Pattern Record.
""")

write("MARTIAN_AUTONOMY.md", "Martian Autonomy", """
The autonomy program demonstrates automated mining, queued manufacturing, local Martian processing, missing-material response, power-interruption response, sustained habitat, unattended production, and telemetry transmission. Mature programmable factories finish quickly; manual imports and intervention produce the intended pacing spread.

Martian experiment data plus an Industrial Control Processor produces the real Martian Autonomy Archive in a Mars-based Research Station. It is a hard AI Age prerequisite.
""")

write("LITE_MATTER_ENGINEERING.md", "Lite Matter Engineering", """
The existing `industrialcivilizationcore:molecular_analyzer` is preserved. Its recipe combines Galacticraft raw meteoric iron, a ComputerCraft advanced computer, steel, an IC2 MV machine block, advanced circuits, and Martian Desh. The working tile is a native IC2 EU sink and consumes 6,250 EU per analysis. It accepts an Earth Iron Ingot, lunar Meteoric Iron, and Galacticraft Desh metadata 2, creates an origin-tagged `industrialcivilizationcore:material_pattern_record`, and completes comparative characterization only after all three environments have been recorded. Forge Energy compatibility is an invisible adapter and never changes the player-facing EU semantics.

Current limitations: one Martian material, no general experiment framework, and no Earth/lunar comparative analysis. Current objectives use automatic Analyzer advancements and tangible records rather than manual validation. Lite Matter includes analysis, records, recovery, engineered/high-purity materials, and early characterization; it explicitly excludes full UU-Matter, replication, magical transmutation, and an AE2 unlock by itself.
""")

write("AI_AGE.md", "AI Age", """
The AI Age begins at approximately 20 optimized, 40 average, or 80 inefficient hours. It requires Orbital Research Archive + Lunar Engineering Archive + Quantum completion + Martian Autonomy Archive + Lite Matter completion.

The Artificial Industrial Intelligence Core is manufactured in the Robotic Cell from a Control Processor, the Martian Autonomy Archive, and the real Material Pattern Record produced by comparative Lite Matter analysis. It is a durable crafting authorization catalyst: inherited AE2 recipes are removed, twelve foundation replacements physically require the AI Core, and every remaining inherited crafting output is reconstructed through the AI-authorized catalog tier.

When the server confirms the AI Core, Martian Autonomy, and Lite Matter completion together, it grants `ai_age_entry` and opens the scrolling credits once for that player. The world remains playable for post-credits branches. Technical Phase Pearls use the compatible vanilla registry ID but a custom IC2-styled name/model and an AI-Core recipe; no pre-AI chapter references pearls.

AI entry begins intelligent inventory awareness, routing, dependency resolution, scheduling, AE2, UU-Matter research, replication research, and autonomous interplanetary coordination. It is the beginning of the endgame.
""")

write("POST_AI_ENDGAME.md", "Post-AI Endgame", """
Post-AI progression branches into AI Logistics, AI Manufacturing, Advanced Matter Engineering, Fusion and Antimatter, Orbital Megastructures, Interplanetary Colonization, Defense, Autonomous Research, and Civilization Infrastructure. These branches may proceed in parallel and none gates AI entry. Matter Replicator, Fusion Research Core, Cargo Controller, Megastructure Controller, and Colony Beacon machines provide real energy-, inventory-, and environment-constrained fulfillment.

The Matter Replicator consumes millions of EU and real research/manufacturing inputs to create stabilized UU-Matter. A second full operation creates one consumable authorized replication capsule, which releases exactly one recorded Martian Desh sample. Fusion/antimatter and megastructure processes require orbit; colony certification requires the Moon or Mars. The final Civilization-Scale AI Core combines replication, megastructure, and autonomous-colony records.

The continuing loop is Explore → Research → Automate → Scale → Establish Colony → Connect Logistics → Unlock Materials → Build Megaproject → Expand Farther. Repetition must add scale or systems, not repeat identical manual crafting.
""")

fields = "\n".join(f"- `{f['id']}`: `{f['type']}`" for f in telemetry["fields"])
write("TELEMETRY_SCHEMA.md", "Telemetry Schema", f"""
The integration mod persists local-only telemetry: first milestone completion time and evidence source, active ticks, manual crafts, manually broken blocks, dimension transfers, synchronized artifacts, per-machine completed operations, stored energy, current progress, cargo transfers, solar generation, and sustained off-world habitat samples. Players can inspect personal counters with `/ic_status`; machines expose operational counters through ComputerCraft. Nothing is transmitted off the computer.

`progression/telemetry-schema.json` defines the complete future pacing dataset:

{fields}

Complex orbital, lunar and Mars base quests now require sustained runtime evidence rather than inventory ownership: a nearby active Galacticraft Oxygen Detector, communications, operating local manufacturing, automated mining, local power, and dimension-specific requirements. Player oxygen exposure or a running sealer alone is not habitat quest evidence. Fields not yet attributable reliably across inherited mods—automatic mining totals, all EU network generation/consumption, reactor efficiency, chapter rollups and complete imported/locally produced resource totals—remain schema-only. The goal is pacing analysis and reliable quest completion, not surveillance or fixed timers.
""")

def evidence_for(milestone):
    if milestone.get("required_item"):
        return [milestone["required_item"]]
    return detection.get("overrides", {}).get(milestone["id"], [milestone["icon"]])


def evidence_text(value):
    spec = value if isinstance(value, dict) else {"item": value}
    count = spec.get("count", 1)
    amount = f"{count}× " if count != 1 else ""
    ore = f"; ore dictionary `{spec['ore_dict']}`" if spec.get("ore_dict") else ""
    return f"{amount}`{spec['item']}`{ore}"


main_milestones = [m for chapter in chapters for m in chapter["milestones"]]
main_ids = {m["id"] for m in main_milestones}
main_successors = {mid: [] for mid in main_ids}
for candidate in main_milestones:
    for prerequisite in candidate["prerequisites"]:
        if prerequisite in main_successors:
            main_successors[prerequisite].append(candidate["id"])

runtime_ids = {m["id"] for m in main_milestones if m.get("runtime_advancement")}
missing_runtime_steps = sorted(runtime_ids - set(RUNTIME_TEST_ACTIONS))
if missing_runtime_steps:
    raise RuntimeError("Missing main-quest runtime test actions: " + ", ".join(missing_runtime_steps))

test_sections = []
test_number = 0
for chapter in chapters:
    chapter_steps = []
    for milestone in chapter["milestones"]:
        test_number += 1
        prerequisites = milestone["prerequisites"]
        logic = milestone.get("prerequisite_logic", "AND")
        prerequisite_text = "none; this is the opening quest" if not prerequisites else (
            f" {logic} ".join(f"`{value}`" for value in prerequisites))
        if milestone.get("runtime_advancement"):
            mode = "Runtime advancement"
            action = RUNTIME_TEST_ACTIONS[milestone["id"]]
            negative = "Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task."
        else:
            mode = "Non-consuming inventory retrieval"
            required = ", ".join(evidence_text(value) for value in evidence_for(milestone))
            action = ("Before this quest unlocks, keep its matching evidence out of the player inventory. "
                      f"After it unlocks, obtain or Creative-give all of the following at the same time: {required}. "
                      "Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.")
            negative = "Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button."
        successors = main_successors[milestone["id"]]
        if successors:
            expected = ("The quest completes in F6 and its matching vanilla advancement is complete. "
                        "These direct main-line successors become eligible after their other prerequisites are satisfied: "
                        + ", ".join(f"`{value}`" for value in successors) + ".")
        else:
            expected = "The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it."
        if milestone["id"] == chapter["completion_milestone"]:
            expected += f" This is the declared completion milestone for Chapter {chapter['number']}."
        validation = " ".join(milestone["final_validation"])
        chapter_steps.append(f"""### {test_number}. {milestone['title']} (`{milestone['id']}`)

- **Prerequisites:** {prerequisite_text}.
- **Detection mode:** {mode}.
- **Exact test action:** {action}
- **Negative assertion:** {negative}
- **Expected quest result:** {expected}
- **Gameplay assertion:** {validation}
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.
""")
    entry_check = ("Confirm this opening quest is available immediately in the fresh world and every later chapter is visible but locked."
                   if chapter["number"] == 1 else
                   "Before completing this chapter's opening quest, confirm the chapter is visible for browsing but its locked quest cannot be opened or progressed. After the previous chapter's completion milestone is satisfied, confirm the opening quest becomes available without a reload.")
    test_sections.append(f"""## Chapter {chapter['number']:02d} — {chapter['title']}

**Chapter purpose:** {chapter['purpose']}

**Entry check:** {entry_check}

{chr(10).join(chapter_steps)}
**Chapter exit check:** Confirm `{chapter['completion_milestone']}` is complete, the next numbered chapter is visible and correctly unlocked when applicable, and unrelated later chapters remain locked.
""")

write("MAIN_QUESTLINE_TEST_PLAN.md", "Main Questline Creative Progression Test Plan", f"""
## Scope and pass condition

This plan tests only the **16 numbered chapters and their {len(main_milestones)} main-line quests**. Independent faction, vehicle, salvage, weapon, and strategic-defense side quests are excluded. The two main transition quests that support a side-route must be tested through their numbered-chapter construction route in this run.

A pass requires every quest to complete from its declared automatic evidence, every prerequisite boundary to behave correctly, every matching vanilla advancement to agree with Better Questing, and every broader gameplay assertion to be checked separately. Inventory evidence proves acquisition only; it does not prove that the represented structure or machine genuinely works.

Current main-line detection split: **{len(main_milestones) - len(runtime_ids)} non-consuming inventory tasks** and **{len(runtime_ids)} runtime-advancement tasks**.

## Test-world preparation

1. Fully quit Minecraft and restart the Technic pack so the latest integration JAR and generated quest database are loaded.
2. Create a new disposable Creative world with cheats enabled. Do not use a valued survival world and do not reuse a player whose advancements already contain Industrial Civilization progress.
3. Let the world warmup overlay finish. Open F6 and verify 16 numbered chapter tabs appear, the first quest is centered, all future chapters remain aspirationally visible, and locked quests cannot be opened or progressed.
4. Open Pause → Advancements and verify the Industrial Civilization root and ordered quest nodes exist. Return to the world and use F6 as the primary guide.
5. Do not use `/advancement grant`, Better Questing editing/complete commands, `/ic_test`, NBT editors, or the runtime space-gate bypass. Creative item giving is allowed only for the inventory-evidence steps listed below.
6. Create a labeled staging area with chests for quest evidence, IC2 power sources/storage, Industrial Civilization machines, ComputerCraft computers, Galacticraft launch/habitat equipment, and sample materials.
7. Keep the player's inventory free of evidence for quests that have not unlocked. After an inventory quest completes, return its evidence to the staging chest unless an upcoming runtime step requires it. This prevents a newly unlocked retrieval task from completing before its negative assertion can be observed.
8. Use Creative search or `/give @p <namespace:item> <count> <metadata>` for listed evidence. A trailing `:*` means any metadata accepted by the quest; prefer the exact machine or tool described by the quest.
9. Allow at least two seconds for inventory tasks and up to five seconds for ordinary runtime telemetry. Sustained off-world base mastery requires 24 five-second samples, or two continuous minutes.
10. After every action, compare F6 and Pause → Advancements. Record the quest ID, evidence used, result, unexpected unlocks, and screenshots. Run `/ic_status` when runtime evidence appears delayed.

## Global negative-gate checks

- Moon travel must fail before the Orbital Research Archive is recorded.
- Quantum Technology must remain locked before the Lunar Engineering Archive quest completes.
- Mars travel must fail without both the Lunar Quantum Component and Mars Mission Authorization.
- AI Age Entry must remain incomplete until the AI Core, Martian Autonomy, and Lite Matter runtime records all exist.
- The Technical Phase Pearl must have no usable pre-AI acquisition route and must not complete from a Creative-spawned pearl.
- Future visible quests must not accumulate progress while locked.

## Ordered quest procedure

{chr(10).join(test_sections)}
## End-of-run acceptance

1. Confirm all {len(main_milestones)} numbered-chapter quests are complete in F6 and exactly their matching main-line advancements are complete.
2. Confirm unfinished independent side-path tabs do not prevent completion of the numbered route.
3. Confirm AI entry displayed the Industrial Civilization credits containing `corysmart` exactly once and returned control to the same playable world.
4. Confirm Chapter 16 completion does not show a victory/end screen or disable continued play.
5. Save and quit, reload the same world, and verify all quest, advancement, authorization, artifact, machine-operation, and AI-credit state persists.
6. Inspect `logs/latest.log`, `logs/groovy.log`, and `crafttweaker.log`. Record every error with the current quest ID and attach screenshots plus `/ic_status` output where relevant.

## Failure record template

```text
Quest title / ID:
Chapter:
Expected prerequisite state:
Evidence or runtime action performed:
F6 result:
Advancement result:
/ic_status result:
Unexpected unlocks or early progress:
Reload persistence result:
Relevant log lines:
Screenshot filenames:
```
""")

checklist = [
"Quest book opens with F6.", f"All {len(chapters)} numbered chapters and all {len(graph['optional_branches'])} independent side-path tabs appear.",
"Every objective completes automatically from item evidence or a hooked runtime event; no manual checkbox task appears.",
"Every future quest is visible for aspirational browsing, but locked quests cannot be opened or progressed.",
"Numbered chapter tabs contain no optional side-path objectives.", "Early quests are reachable.",
"All former placeholder registry objects are absent from HEI and every replacement has a distinct sprite.",
"The Electric Fabricator processes a fixed recipe using IC2 EU.",
"The Programmable Assembler accepts ComputerCraft recipe selection and queues.",
"The Robotic Manufacturing Cell enforces Moon-only Quantum-component synthesis.",
"Electrification chapter completes.", "Automation chapter opens.",
"The Factions and Salvage path can start independently of the numbered chapters.",
"Pause > Advancements opens the vanilla screen and its Industrial Civilization tab contains the ordered root plus one visible advancement for every quest.",
"Pause > Factions & Settlements displays all six factions, reputation, attitude, membership eligibility, settlement types, and products.",
"Civil Defense contact, Territorial Militia contact, and a registered militia-outpost takedown complete their separate optional objectives.",
"The Strategic Defense path detects ICBM launch control, radar defense, and a conventional missile without unlocking prohibited strategic-payload shortcuts.",
"Every village and settlement merchant accepts IC Credits rather than emeralds.",
"Sneak-right-click joins an eligible faction, while reputation 60 plus eight IC Credits can recruit a friendly companion.",
"Hostile faction members attack, guards defend their settlement, and companions follow and defend their owner.",
"A fresh world has only three primitive settlements near spawn and increasingly industrial structures in the documented distance bands.",
"Resolving the criminal network can reveal and restore the abandoned factory.",
"Industrial Capacity Secured accepts either built Heavy Industry or the restored abandoned factory.",
"Programmable Capacity Secured accepts either built Programmable Manufacturing or the recovered factory control system.",
"Orbital Age follows Nuclear Age.", "Moon remains locked before Orbital Research.",
"Moon unlocks after the Orbital Research Archive.", "Lunar Research follows Lunar Settlement.",
"Quantum Technology remains locked before Lunar Research.", "Quantum Technology unlocks after the Lunar Engineering Archive.",
"Mars remains locked before Quantum Technology.", "Mars remains locked without Mars Mission Authorization.",
"Mars unlocks after Quantum Technology and authorization.", "Existing Galacticraft Desh metadata 2 Mars Sample is recognized.",
"Existing Molecular Analyzer is recognized.", "Martian Autonomy follows Mars Settlement.",
"AI Age remains locked before Martian Autonomy and Lite Matter Engineering.",
"The real AI Core is synthesized from Martian Autonomy, Lite Matter, and programmable manufacturing outputs and acts as a durable AE2 recipe catalyst.",
"Unlocking the AI Age opens the one-time scrolling credits screen without ending the world.",
"The Technical Phase Pearl has no pre-AI quest or recipe dependency and becomes craftable only with the durable AI Core.",
"The Galacticraft destination list permits orbit, gated Moon, and gated Mars only; unsupported bodies and the End cannot be entered, and Mars Mission Authorization unlocks the Tier 2 NASA Workbench page without a lunar dungeon.",
"Earth Iron, lunar Meteoric Iron, and Martian Desh all produce origin-tagged Analyzer records before comparative research completes.",
"An unshielded player in orbit, on the Moon, or on Mars takes radiation damage unless protected by an active sealed habitat or full QuantumSuit.",
"Completing an actual IC Credit buy or sell—not merely opening a merchant—records faction trade contact.",
"AI Age is presented as the beginning of the endgame.", "Post-AI branches are visible but do not block AI entry.",
"No circular or impossible dependencies are visible."
]
checklist.extend([
"Orbital, lunar, and Mars habitat quests trigger only after a nearby Galacticraft Oxygen Detector becomes active from breathable air; oxygen affecting the player or a running sealer alone is insufficient.",
"Functional off-world bases require two continuous minutes of stable habitat samples plus their placed/operating infrastructure.",
"A primitive settlement absorbs nearby stockpile items, pays an exact material bill, and constructs each physical upgrade without a random roll.",
"Already-generated Mars chunks receive deterministic civilization processing after an AI-age player loads them.",
"Apollo 11, 12, 14, 15, 16, and 17 markers show mission, landing date, coordinates, flag and heritage designation.",
"Faction villagers, militia patrols, and robbers use their Industrial Civilization faction skins.",
"World warmup remains visible at least 15 seconds and releases by 30 seconds.",
"Stone axe/chainsaw trees and 3x3/9x9 drills process at most 12 extra blocks per tick while preserving protection, drops, enchantments and per-block tool payment.",
"Radiation correctly follows players in vehicles and other moving entities as their AABB enters or leaves breathable air."
])
write("MANUAL_QUEST_TEST_CHECKLIST.md", "Manual Quest Test Checklist", "Use this checklist for 0.6.3 release-candidate regressions and dedicated side-path acceptance. The numbered campaign's completed acceptance evidence and earlier 0.6.2 regression results are recorded in `MAIN_QUESTLINE_LIVE_TEST_LOG.md`; rerun affected checks after progression or runtime changes. Do not use a valued world for a quest-database migration.\n\n" + "\n".join(f"- [ ] {i}. {v}" for i, v in enumerate(checklist, 1)) + "\n\nCheck `logs/latest.log`, `logs/groovy.log`, and `crafttweaker.log` after the run. Record quest/task IDs and screenshots for any mismatch.")

write("KNOWN_LIMITATIONS.md", "Verification Boundaries", """
The build is a hidden internal alpha. The numbered 16-chapter campaign passed live acceptance, but targeted systems and side paths still retain these verification boundaries:

- The critical orbital/lunar/Mars base milestones now have sustained runtime telemetry. Many simpler inherited-mod quests still use tangible, non-consuming item evidence; that proves acquisition, not permanent assembly or provenance.
- Static and JUnit validation cannot prove Better Questing/Galacticraft GUI rendering, Forge event ordering, third-party protection integrations, entity behavior or performance.
- IC2 Classic packet handling, MFSU output, physical 1/4/10/50-source networks, native overvoltage destruction, GUI telemetry, and save/reload continuation have full-client evidence. Unusual cable topologies, mixed third-party EnergyNet components, multiplayer ownership, and long-duration operation remain runtime scenarios.
- Settlement upgrades are deterministic and material-backed, but production rates, construction bills, trade circulation and weapon multipliers need measured economic playthroughs.
- Existing loaded Mars chunks can transform after AI. Unloaded chunks wait until an AI-age player loads them; this deliberately avoids an unbounded background rewrite.
- Apollo positions use documented latitude/longitude projected at 24 blocks per degree. Galacticraft terrain is not a geographic lunar simulation, and the block monuments are interpretive heritage markers.
- Faction skins are initial 1.12-compatible texture variants. They still need in-game lighting/model review and more role-specific differentiation.
- Tool and habitat rule tests cover deterministic boundaries; actual claims, modded trees, unusual seals, moving vehicles and mass block drops remain runtime scenarios.
- HeadlessMC is a viable Forge 1.12.2 launch/UI driver and can stage the installed `industrial-civilization-astra` instance locally. Public CI still cannot reconstruct the complete pack from Git alone because third-party binaries and the authorized first-launch dependency are intentionally not committed.
- Workshop review art and an exact placement contract now exist outside the game. Several decorative blocks/models pictured in the visual target are not implemented yet.
- The hidden Technic/GitHub delivery path is operational. Public release still requires the remaining redistribution/attribution review, multiplayer QA, side-path acceptance, and full survival-balance testing.
""")

print(f"Generated 26 progression documents from {len(chapters)} chapters, {len(graph['optional_branches'])} side paths, and {len(milestones)} milestones")
