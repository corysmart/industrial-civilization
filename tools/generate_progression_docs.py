#!/usr/bin/env python3
"""Generate human-readable Phase 2 design documents from canonical JSON."""
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
milestones = ([m for c in chapters for m in c["milestones"]] +
              [m for p in side_paths for m in p["milestones"]])
by_id = {m["id"]: m for m in milestones}

NOTICE = "<!-- Generated from progression/*.json by tools/generate_progression_docs.py. -->\n\n"


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

The model contains **{len(chapters)} numbered chapters**, **{len(graph['optional_branches'])} independent side paths**, **{len(milestones)} milestones**, and **{len(placeholders['entries'])} explicit placeholders**. Every objective uses `ALWAYS` visibility: future goals are aspirationally visible even while locked. The AI Age begins the endgame; it is not a victory screen.
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
write("OPTIMIZATION_OPPORTUNITIES.md", "Optimization Opportunities", "Time differences emerge from engineering quality: parallelism, power stability, routing, preparation, local production, and reduced downtime.\n\n" + "\n\n".join(opt_sections))

write("ANTI_GRIND_RULES.md", "Anti-Grind Rules", """
1. Every repeated task receives automation before large-scale demand.
2. Optimization is multiplicative through parallelism, routing, power quality, replenishment, and local production.
3. Mandatory quests represent capabilities—not cables, batteries, plates, or arbitrary micro-components.
4. No fixed waiting exists to manufacture the 20/40/80-hour spread.
5. Poor play remains successful; it is slower only because of manual work, downtime, shortages, travel, and rebuilding.
6. Old factories are expanded, integrated, repurposed, and automated rather than discarded each era.
7. Rewards are information, access, research artifacts, or small test items—not free high-tier infrastructure.
""")

placeholder_rows = ["| ID | Quest milestone | Test item | Final target | Recipe |", "|---|---|---|---|---|"]
for p in placeholders["entries"]:
    ingredients = " + ".join(p["temporary_recipe"])
    placeholder_rows.append(f"| `{p['id']}` | `{p['milestone_id']}` | `{p['placeholder_item']}` | `{p['final_target']}` | {ingredients} |")
write("PLACEHOLDER_SYSTEM.md", "Placeholder System", f"""
Every missing runtime system has a visibly named `[TEST PLACEHOLDER]` item with a stable registry ID, explanatory tooltip, final target, and replacement contract. Recipes live only in `groovy/postInit/industrial_civilization_placeholders.groovy` and are controlled by `config/industrialcivilization/placeholders.cfg`:

```properties
enableTestingPlaceholders=true
```

Set it to `false` to remove every temporary recipe and AI-gated AE2 recipe without deleting the canonical graph. Research artifacts use Paper + Redstone; machine artifacts use Iron Ingot + Redstone.

{chr(10).join(placeholder_rows)}

When a final implementation arrives, keep `milestone_id`, replace only `placeholder_item`/temporary validation with the runtime condition, and mark `replacement_status` accordingly.
""")

write("QUEST_IMPLEMENTATION.md", "Quest Implementation", f"""
Better Questing 3 reads `config/betterquesting/DefaultQuests.json`. Run `python3 tools/generate_objectives.py` after canonical edits; never hand-edit generated quests. The current projection has {len(chapters) + len(graph['optional_branches'])} quest lines ({len(chapters)} numbered chapters and {len(graph['optional_branches'])} independent side paths) and {len(milestones)} quests.

Possession milestones use Standard Expansion `bq_standard:retrieval` tasks with NBT ignored, group detection enabled, and consumption disabled. Construction, operation, mastery, research, and transitions use manual checkboxes where no reliable runtime trigger exists. Each such quest lists final validation; placeholder-backed quests add a conspicuous `TEMPORARY VALIDATION` section. All quests use `ALWAYS` visibility and locked progress, so players can browse the whole civilization plan without completing locked objectives early.

Quest IDs are deterministic from chapter/side-path order and milestone order. Cross-line prerequisites use the same global numeric map. `pack_version` is 3. Import/update the default pack through Better Questing when a world retains the older database.

The quest graph documents gates but does not yet intercept Galacticraft destination selection. Runtime destination and operation enforcement remains future work.
""")

write("AUTOCRAFTING_PROGRESSION.md", "Autocrafting Progression", """
| Tier | Ordinary craft time | Distinct capability | Limitation |
|---|---:|---|---|
| Manual Crafting Table | Immediate | Baseline assembly | Player labor |
| Automatic Crafting Table | 10–20 seconds | Removes player presence | Fixed recipe and physical routing |
| Electric Fabricator | 4–8 seconds | EU-powered standard automation | One active recipe |
| Programmable Assembler | 1–4 seconds | Queues, programs, shortage reporting | Logistics and MV/HV power |
| Robotic Manufacturing Cell | 0.5–2 seconds | Parallel high-throughput operations | Advanced infrastructure |
| Applied Energistics | Immediate/near-immediate | Network inventory, dependencies, routing, scheduling | Materials and physical processes |

AE2 perfects autocrafting; it does not introduce it. Smelting, macerating, refining, isotope separation, fuel preparation, UU-Matter, and replication remain time- and power-bound.
""")

write("RESEARCH_PROGRESSION.md", "Research Progression", """
One future modular Research Station should combine environment, infrastructure, experiment modules, samples, operational data, power, data storage, and ComputerCraft reporting. It must never be merely a generic powered progress bar.

- Orbit produces the Orbital Research Archive from sustained station operation and experiments.
- The Moon produces the Lunar Engineering Archive from local industry and lunar science.
- Mars produces the Martian Autonomy Archive from autonomous colony behavior and research.

Target APIs are documented by the canonical quest descriptions: environment/status discovery, available experiments, requirements, progress, operational data, missing infrastructure, output archive, start, and cancel. Placeholder possession temporarily stands in only for the final combined conditions.
""")

write("ORBITAL_STATION.md", "Orbital Station", """
Earth orbit is the first mandatory space destination. A Tier 1 launch grants orbit—not unrestricted Moon access. The functional station requires pressure, oxygen, storage, safe arrival/docking, communications, a computer, research equipment, solar generation, and eclipse storage.

Long-term benefits are orbital solar, microgravity/vacuum manufacturing, research, cargo transfer, fuel storage, Moon/Mars logistics, shipbuilding, and future power transmission. Standard IC2 solar targets 8–10× Earth output in orbit; tracking arrays target 15–25×. Those output multipliers are design targets, not implemented runtime behavior.
""")

write("ORBITAL_RESEARCH.md", "Orbital Research", """
The final program combines vacuum materials, solar radiation, telescope, microgravity manufacturing, life-support endurance, communications, and eclipse tests with station/oxygen/energy/solar/manufacturing/transmission uptime. The temporary Orbital Research Archive recipe is only a questline test mechanism.

`orbital_research_complete` is the sole research gate for `moon_access`. Static validation proves the dependency; destination-level enforcement is not yet implemented.
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
Mars requires Quantum completion plus `placeholder_mars_mission_authorization` during testing. The colony requires pressure, oxygen, water, radiation protection, communications, dust-resilient power, storage, automated mining, local manufacturing, research equipment, and return/cargo capability.

The existing real Mars Sample is Galacticraft Desh: registry `galacticraftplanets:item_basic_mars`, metadata `2`. It is obtained through existing Galacticraft Mars progression (no replacement recipe was added), is required after Martian mining, and is consumed as the Analyzer's input while producing a durable Material Pattern Record.
""")

write("MARTIAN_AUTONOMY.md", "Martian Autonomy", """
The autonomy program demonstrates automated mining, queued manufacturing, local Martian processing, missing-material response, power-interruption response, sustained habitat, unattended production, and telemetry transmission. Mature programmable factories finish quickly; manual imports and intervention produce the intended pacing spread.

The temporary Martian Autonomy Archive represents the combined final proof and is a hard AI Age prerequisite.
""")

write("LITE_MATTER_ENGINEERING.md", "Lite Matter Engineering", """
The existing `industrialcivilizationcore:molecular_analyzer` is preserved. Its existing recipe combines Galacticraft raw meteoric iron, a ComputerCraft advanced computer, steel, an IC2 MV machine block, advanced circuits, and Martian Desh. The working tile accepts only Galacticraft Desh metadata 2, consumes 50,000 FE per analysis, creates `industrialcivilizationcore:material_pattern_record`, and records the current Lite Matter completion flag.

Current limitations: one Martian material, FE rather than native EU semantics, no general experiment framework, and no Earth/lunar comparative analysis. The questline represents those future studies with explicit manual validation. Lite Matter includes analysis, records, recovery, engineered/high-purity materials, and early characterization; it explicitly excludes full UU-Matter, replication, magical transmutation, and an AE2 unlock by itself.
""")

write("AI_AGE.md", "AI Age", """
The AI Age begins at approximately 20 optimized, 40 average, or 80 inefficient hours. It requires Orbital Research Archive + Lunar Engineering Archive + Quantum completion + Martian Autonomy Archive + Lite Matter completion.

The `[TEST PLACEHOLDER] Artificial Industrial Intelligence Core` represents the future manufactured core. The main integration script removes every original AE2 crafting recipe. Only while `enableTestingPlaceholders=true`, a small set of AE2 entry recipes is restored, and every recipe physically requires the placeholder AI Core. Turning the toggle off removes both placeholder acquisition and AE2 testing entry recipes.

AI entry begins intelligent inventory awareness, routing, dependency resolution, scheduling, AE2, UU-Matter research, replication research, and autonomous interplanetary coordination. It is the beginning of the endgame.
""")

write("POST_AI_ENDGAME.md", "Post-AI Endgame", """
Post-AI progression branches into AI Logistics, AI Manufacturing, Advanced Matter Engineering, Fusion and Antimatter, Orbital Megastructures, Interplanetary Colonization, Defense, Autonomous Research, and Civilization Infrastructure. These branches may proceed in parallel and none gates AI entry.

The continuing loop is Explore → Research → Automate → Scale → Establish Colony → Connect Logistics → Unlock Materials → Build Megaproject → Expand Farther. Repetition must add scale or systems, not repeat identical manual crafting. UU-Matter and replication remain storyboard/manual milestones in this phase; no free-matter placeholder recipe exists.
""")

fields = "\n".join(f"- `{f['id']}`: `{f['type']}`" for f in telemetry["fields"])
write("TELEMETRY_SCHEMA.md", "Telemetry Schema", f"""
Telemetry is **not implemented**. `progression/telemetry-schema.json` prepares a future opt-in, aggregate-only implementation for pacing validation. It defines:

{fields}

The goal is to compare real engineering behavior with 20/40/80 targets, not surveil players or enforce timers.
""")

checklist = [
"Quest book opens with F6.", "All 16 numbered chapters and all 5 independent side-path tabs appear.",
"Every future quest is visible for aspirational browsing, but locked quests cannot be opened or progressed.",
"Numbered chapter tabs contain no optional side-path objectives.", "Early quests are reachable.",
"Placeholder recipes work when enabled.", "Electrification chapter completes.", "Automation chapter opens.",
"The Factions and Salvage path can start independently of the numbered chapters.",
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
"Placeholder AI Core unlocks the supplied AE2 entry recipes only in testing mode.",
"AI Age is presented as the beginning of the endgame.", "Post-AI branches are visible but do not block AI entry.",
"No circular or impossible dependencies are visible."
]
write("MANUAL_QUEST_TEST_CHECKLIST.md", "Manual Quest Test Checklist", "Do not use a valued world for the first database import. No runtime claims are made until this checklist passes.\n\n" + "\n".join(f"- [ ] {i}. {v}" for i, v in enumerate(checklist, 1)) + "\n\nCheck `logs/latest.log`, `logs/groovy.log`, and `crafttweaker.log` after the run. Record quest/task IDs and screenshots for any mismatch.")

write("KNOWN_LIMITATIONS.md", "Known Limitations", """
- Static validation cannot prove Better Questing GUI rendering, retrieval-task runtime behavior, or existing-world import behavior.
- Galacticraft destination selection is not runtime-locked by archives/authorization yet; the quest graph is authoritative but enforcement is future integration work.
- Construction, environment, operation, sustained uptime, telemetry, research, and mastery checks use explicit manual checkboxes until APIs exist.
- Research Station, experiment module, three archives, three manufacturing tiers, lunar Quantum component, Mars authorization, and AI Core are test placeholders.
- Orbital/lunar/Mars solar multipliers, dust, darkness, pressure, radiation, and cargo automation are design targets, not new runtime mechanics in this phase.
- The real Analyzer currently accepts only Martian Desh metadata 2, consumes 50,000 FE, and does not yet perform Earth/lunar comparative research.
- Temporary AE2 entry covers a small test set; original AE2 recipes stay removed. Cable/terminal and complete AE2 progression still require final AI Age recipe work.
- UU-Matter, replication, fusion, antimatter, megastructures, autonomous expansion, and civilization-scale AI remain storyboard/manual endgame milestones.
- Quest updates may require importing Better Questing defaults in existing worlds. Back up world quest data first.
""")

print(f"Generated 25 progression documents from {len(chapters)} chapters, {len(graph['optional_branches'])} side paths, and {len(milestones)} milestones")
