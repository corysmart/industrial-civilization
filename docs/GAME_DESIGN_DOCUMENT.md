# Industrial Civilization — Game Design Document

Status: Phase 2 playable development build  
Pack: Minecraft 1.12.2 / Technic Tekkit 2  
First-party runtime: IndustrialCivilizationCore 0.2.0  
Authoritative data: `progression/*.json`; generated quest pack: `config/betterquesting/DefaultQuests.json`

## Vision

Industrial Civilization is an IC2 expansion-scale progression pack about turning one workshop into a permanent, multi-world technical civilization. The player fantasy is not collecting isolated machines. It is learning to make energy, materials, logistics, research, and automation reinforce one another until Earth, orbit, the Moon, and Mars operate as one system.

The design has four pillars:

1. **IC2 is the visible technical language.** Custom machines consume and display EU, use IC2 voltage tiers, share the IC2-adjacent casing palette, and call compatibility energy “IC2” in player-facing UI.
2. **Capability gates, not component chores.** Mandatory quests prove functional workshops, stations, colonies, and research programs. Small parts are taught only when they explain a system.
3. **Research comes from place.** Orbit, lunar darkness, lunar manufacturing, Martian autonomy, and Desh analysis produce knowledge that Earth cannot substitute.
4. **The AI Age begins the endgame.** It authorizes AE2 and parallel civilization-scale programs; it is not a victory screen.

## Player-facing rules

- F6 opens the Better Questing guide, and the pause-menu Quest Guide button opens the same UI.
- All 122 quests use aspirational `ALWAYS` visibility. Locked future lines remain visible; secret objectives are not used.
- Every quest completes automatically through a runtime advancement or non-consuming inventory evidence. There are no manual checkbox tasks.
- Every modded quest includes the relevant Mac/no-numpad controls and operating instructions in its description.
- Every node picture is the actual required item, first evidence item, or an explicit real machine/artifact/vehicle override. Symbolic storyboard pictures are not emitted.
- Numbered chapters contain the critical route. Six independent side-path tabs can be pursued whenever their prerequisites or discoveries permit.
- Pause > Factions & Settlements is the complete in-game directory for faction reputation, attitude, membership rules, settlement types, products, and NPC interaction instructions.

## Complete progression flow

| # | Story and capability | What the player builds or proves | Gate/output |
|---:|---|---|---|
| 1 | Survive and organize the first workshop | Starter material families, Iron Chest storage, basic defense, manual production, secure room | `secure_workshop` |
| 2 | Replace muscle with safe electricity | IC2 generator, storage, cables, Macerator/processing set, electric tools, voltage literacy | `stable_electrical_workshop` |
| 3 | Remove human attention from routine work | Automated ore handling, quarry/mining, item logistics, chunk loading, wireless control, early autocrafting | `early_autocrafting` |
| 4 | Become strategically important | Railcraft steel/fuel/freight, advanced IC2, Techguns armament, factions, MFFS defense, heavy manufacturing | `industrial_capacity_access` |
| 5 | Give the factory a nervous system | ComputerCraft monitoring, Electric Fabricator, Programmable Assembler, queues and multi-step production | `programmable_capacity_access` |
| 6 | Produce orbital-scale power safely | IC2 reactor, measured output, telemetry, automatic shutdown, containment, sustained monitored operation | `monitored_nuclear_power` |
| 7 | Make orbit a permanent destination | Tier 1 launch, sealed habitat, power, communications, Research Station, Experiment Module | `functional_orbital_station` |
| 8 | Turn the station into a laboratory | Environment-tagged orbital data, solar production, tracking power, Orbital Research Archive | `orbital_research_complete`; Moon authorization |
| 9 | Build a lunar settlement, not a flag | Moon landing, habitat, solar/storage, mining, manufacturing, cargo, sustained local operation | `functional_lunar_base` |
| 10 | Use lunar conditions as scientific inputs | Lunar data, darkness endurance, precision manufacturing, Lunar Engineering Archive | `lunar_research_complete`; Quantum authorization |
| 11 | Industrialize quantum technology | Extreme-voltage IC2, Robotic Manufacturing Cell, lunar quantum component, Nano/Quantum equipment | `quantum_technology_complete`; Mars readiness |
| 12 | Establish a distant Martian colony | Mars authorization, Tier 2 route, habitat, power, local Desh mining, manufacturing, cargo | `functional_martian_base`; real Desh sample |
| 13 | Make Mars respond without supervision | Resource/power response, unattended production, Martian research data and autonomy archive | `martian_autonomy_complete` |
| 14 | Record matter without prematurely copying it | Molecular Analyzer, 6,250-EU Desh analysis, pattern record and recovery proof | `lite_matter_complete` |
| 15 | Synthesize the civilization’s technical memory | AI input audit, Artificial Industrial Intelligence Core, AI-authorized AE2 foundation | `ai_age_entry` |
| 16 | Expand through parallel endgame programs | Cargo network, UU-Matter, controlled replication, fusion, antimatter, megastructure, colonies, scale AI | `continuous_civilization` with no terminal win |

Canonical dependency order is Earth industry → monitored nuclear power → orbit → Orbital Research Archive → Moon → Lunar Engineering Archive and lunar quantum component → Mars authorization → Mars → Martian Autonomy Archive → Material Pattern Record → AI Core → AE2 and post-AI programs.

### Independent side paths

| Side path | Purpose | Relationship to the critical route |
|---|---|---|
| Field Engineering | Whole-tree tools, area drills, remote controls, recovery, and resilience | Optional quality-of-life and reliability capabilities |
| Factions and Salvage | Settlements, criminal encounters, abandoned factory restoration | Can provide documented alternate access to industrial and programmable capacity; cannot skip scientific gates |
| Orbital Power | Environmental and tracking solar development | Optional power specialization that becomes especially valuable in orbit |
| Cargo Logistics | Freight and cross-dimensional cargo mastery | Optional until the post-AI logistics program requires civilization-scale proof |
| Mobility and Nations | Roads, six real vehicles, city exchange, service carrier, and covered workshops | Optional industrial mobility and advanced armament capacity |
| Post-AI Horizons | Matter, fusion, colony, logistics, and megastructure programs | Parallel by design; no single branch blocks AI entry or all other branches |

## How the quest system is built

`progression/chapters/*.json`, `progression/side-paths/*.json`, and `progression/progression-graph.json` define titles, prerequisites, validation, and path placement. `progression/objective-detection.json` defines tangible multi-item evidence where Forge has no stable event. `tools/generate_objectives.py` deterministically assigns IDs and produces Better Questing 3 JSON.

Quest tasks use two mechanisms:

- `bq_standard:advancement` for first-party machine operations and dimension/environment events implemented by IndustrialCivilizationCore.
- `bq_standard:retrieval` for non-consuming ownership/construction evidence across other mods.

The generator also owns story openings/transitions, contextual control blocks, evidence-driven icons, five era backgrounds, and `pack_version` 6. Existing worlds must import the new Better Questing default after backup; new worlds receive it directly.

## Controls taught in quests

| System | Shipped Mac/no-numpad control |
|---|---|
| Quest guide | F6 |
| Custom NPC quest log | F8 |
| IC2 mode / armor | F10 mode; Backslash armor modifier; Option+H boost; Option+O HUD expansion |
| Galacticraft | Command+I spaceship inventory |
| HEI | R recipe; U uses; Option+I bookmark |
| Techguns | Option+Y force reload |
| ComputerCraft | Right-click computer; `help`; Control+T terminate; peripheral discovery examples are written inline |
| AE2 | Command+P toggle search focus |
| Railcraft locomotive | Option+[ reverse; Option+period faster; Option+] slower; Option+; mode; Option+' whistle |
| Waila | Command+[ display; Command+] liquid; Command+; recipe; Command+0 config; Command+' uses |
| Pack area tools | Sneak suppresses whole-tree/area behavior for precision work |
| Factions and NPCs | Pause > Factions & Settlements; right-click trades; sneak-right-click membership/recruitment/dismissal |
| Onysd Vehicles | W/A/S/D drive; H horn; C cycle seats; sneak-right-click parked service carrier for cargo |

Ordinary right-click interaction and machine-specific operational steps are stated beside these bindings; the quest text does not send the player to an external wiki or the Controls menu.

## First-party machines: construction and behavior

Recipes below use E-Circuit = `ic2:itemmisc:451` and A-Circuit = `ic2:itemmisc:452`. Exact shaped layouts remain visible through HEI; rows summarize the authoritative Groovy recipes.

| Block | How it is built | Runtime role |
|---|---|---|
| Molecular Analyzer | Meteoric Iron + advanced ComputerCraft computer + steel + IC2 MV machine + A-Circuits + Martian Desh | Tier-3 IC2 sink; 25,000-EU buffer; right-click with Desh metadata 2 consumes 6,250 EU and one sample to create a non-replicable Material Pattern Record |
| Electric Fabricator | Steel, piston, E-Circuits, IC2 LV machine, rubber, crafting table | 40,000 EU / 32 EU-t / 160-tick fixed manufacturing; makes Precision Frames and Blank Data Cartridges |
| Programmable Assembler | Steel, advanced computer, A-Circuits, Electric Fabricator, pistons, redstone | 120,000 EU / 128 EU-t / 240 ticks; makes Control Processors; ComputerCraft can select recipes and queue 1–64 operations |
| Robotic Manufacturing Cell | A-Circuits, advanced computer, steel, Programmable Assembler, pistons, IC2 MV machine | 400,000 EU / 512 EU-t / 320 ticks; dimension-aware lunar components, AI cores, and civilization-scale AI |
| Research Station | Glass, advanced computer, steel, IC2 MV machine, A-Circuits, Programmable Assembler | 100,000 EU / 32 EU-t / 600 ticks; converts environment-tagged data and prior archives into orbital, lunar, Mars, and autonomy gates |
| Orbital Experiment Module | Meteoric Iron, glass, steel, Research Station, A-Circuits, Blank Data Cartridge | 80,000 EU / 32 EU-t / 600 ticks; records orbit/Moon/Mars data with an `Environment` NBT tag |
| Factory Control Terminal | Not craftable; generated in rare abandoned-factory structures and repaired through the encounter | Staged salvage encounter and alternate industrial/programming capacity proofs |
| Environmental Solar Array | Glass, E/A-Circuits, IC2 MV machine, steel, Control Processor | 200,000-EU buffer; outputs 8 EU/t Earth, 96 orbit, 32 Moon, and 16 Mars with deterministic dust derating to 4 |
| Tracking Solar Array | Observers, Control Processor, three Environmental Solar Arrays, A-Circuits, advanced computer | 192 EU/t in orbit and 12 EU/t on Earth; grants sustained tracking-array proof after real generation |
| Matter Replicator | A-Circuits, AI Core, IC2 HV machines, Robotic Cell, obsidian, Material Pattern Record | 8,000,000 EU / 2,048 EU-t / 2,000 ticks; produces UU-Matter capsule and controlled replication record only after AI |
| Fusion Research Core | Nether Stars, AI Core, IC2 HV machines, Matter Replicator, obsidian, Control Processor | 40,000,000 EU / 8,192 EU-t / 4,000 ticks; produces contained antimatter only in orbit |
| Interplanetary Cargo Controller | Meteoric Iron, advanced computer, Control Processors, Robotic Cell, Ender Eyes, recovered factory control system | 4,000,000 EU / 512 EU-t; paired same-name channels move one item between loaded controllers in different dimensions |
| Orbital Megastructure Controller | Beacons, AI Core, IC2 HV machines, Cargo Controller, obsidian, Control Processor | 24,000,000 EU / 8,192 EU-t / 2,000 ticks; in orbit consumes post-AI proofs to create a Megastructure Control Record |
| Autonomous Colony Beacon | Beacons, AI Core, Control Processors, Cargo Controller, diamonds, Robotic Cell | 8,000,000 EU / 2,048 EU-t / 1,200 ticks; on Moon or Mars produces a colony charter from AI, control, and cargo proofs |
| Car Workshop | Advanced IC2 and programmable components | Places a 9×7 assembly set piece; covered 128-EU/t manufacturing programs create six Onysd vehicle crates |
| Gun Factory | Advanced IC2 and programmable components | Places a 9×7 armament set piece; covered 512-EU/t programs produce shotgun and automatic-rifle outputs |
| Repair Bench | IC2 machine components | Repairs the nearest rusted large workshop within 12 blocks, consuming one complete IC2 Machine Block |
| Vehicle Service Dock | BuildCraft pipe and IC2 machine components | Bridges BuildCraft item/fluid capabilities to a parked Industrial Service Carrier within four blocks |

All twelve processing machines are IC2 EU sinks with sided inventories (inputs on non-bottom faces, output below), a four-slot IC2-styled GUI, automatic matching for inserted inputs, and a ComputerCraft peripheral. The same blocks expose Forge Energy only as hidden compatibility plumbing at the canonical 8 FE = 1 EU ratio.

## First-party artifacts: production and gates

| Item | How it is obtained | What it proves or unlocks |
|---|---|---|
| Precision Frame | Electric Fabricator: iron + redstone | First fabricated component |
| Blank Data Cartridge | Electric Fabricator: paper + redstone → 2 | Input for off-world Experiment Module runs |
| Research Data | Experiment Module + cartridge in orbit/Moon/Mars | Environment-tagged scientific evidence |
| Orbital Research Archive | Research Station in orbit + orbital data | Authorizes Moon entry |
| Lunar Engineering Archive | Research Station on Moon + lunar data + orbital archive | Quantum authorization input |
| Control Processor | Programmable Assembler: frame + cartridge + redstone | Common programmable-machine component |
| Lunar Quantum Component | Robotic Cell on Moon: processor + lunar archive + Meteoric Iron | Mars readiness and authorization input |
| Mars Mission Authorization | Research Station on Earth: lunar quantum component + lunar archive | Authorizes Mars entry |
| Martian Autonomy Archive | Research Station on Mars: Martian data + processor | AI Core prerequisite |
| Material Pattern Record | Molecular Analyzer + powered Desh analysis | Lite Matter completion and AI/matter input; explicitly marked non-replicable |
| Underworld Dossier | Pack-owned faction encounter | Reveals criminal/factory route |
| Criminal Network Ledger | Criminal encounter resolution | Proof that the network was investigated/defeated |
| Factory Restoration Certificate | Staged abandoned-factory repair | Alternate heavy-industrial capacity proof |
| Recovered Factory Control System | Factory terminal audit/repair | Programmable bypass proof and cargo-controller ingredient |
| Artificial Industrial Intelligence Core | Robotic Cell: processor + Martian archive + pattern record | Durable crafting key for curated AE2 foundation recipes and post-AI machines |
| UU-Matter Capsule | Matter Replicator: processor + pattern record + glowstone | Controlled post-AI matter feedstock |
| Controlled Replication Record | Matter Replicator: UU capsule + pattern record + processor | Right-click consumes record and releases one replicated Desh sample |
| Contained Antimatter Capsule | Fusion Core in orbit: UU capsule + AI Core + processor | Megastructure program input |
| Interplanetary Cargo Network Key | Cargo Controller: recovered system + AI Core + processor | Authorizes/configures civilization cargo network |
| Megastructure Control Record | Orbital Megastructure Controller: antimatter + cargo key + AI Core | Orbital-scale construction proof |
| Autonomous Colony Charter | Colony Beacon on Moon/Mars: cargo key + processor + AI Core | Permanent colony proof |
| Civilization-Scale AI Core | Robotic Cell: megastructure record + colony charter + replication record | Open-ended capstone; records capacity, not final victory |

Research artifacts that carry progression knowledge must be held and right-clicked once. Quest descriptions teach this at the objective that produces or consumes them.

## Cross-mod composition

- **IC2 Classic:** visible power system, voltage tiers, generators, storage, processing, reactor, electric tools, Nano/Quantum equipment.
- **ComputerCraft/Computronics:** monitoring and first-party peripheral control. Machine methods expose status, energy, capacity, progress, environment, recipes, queues, completion counts, and cargo channels.
- **Galacticraft:** rockets, Moon/Mars dimensions, habitats, oxygen, cargo hardware, and normal Tier 1 → Moon schematic → Tier 2 progression. First-party gates validate entry rather than replacing the travel UI.
- **Railcraft/BuildCraft/ProjectRed/MFFS/Techguns:** freight, extraction/logistics, wireless control, containment/defense, and industrial armament.
- **Onysd Vehicles:** real driving physics and chassis. The pack curates six roles and extends the minibus into a 54-slot, 64,000-mB mobile workshop that interfaces with BuildCraft while parked.
- **AE2:** installed but ordinary recipes removed. After AI, twelve curated foundation outputs require the durable AI Core, Control Processor, iron, and redstone; broader AE2 remains future balance work.
- **Better Questing + Standard Expansion:** player-facing tutorial, story, automatic tasks, and aspirational map.
- **GroovyScript:** reloadable recipe locks, HEI filtering, firearm/analyzer recipes, and tooltips. Java owns persistent state and runtime behavior.

## NPCs, factions, and settlement geography

IndustrialCivilizationCore implements five persistent factions: the Frontier Cooperative, Riverside Works Consortium, Civil Defense Militia, Survey Detachment 7, and Ashline Raiders. NPC attitudes are derived from per-player reputation and membership. Legitimate factions consider crafting, industrial capacity, research, trading, raider kills, civilian safety, and guarded-property behavior; the raiders reward destructive play. Membership is therefore earned through demonstrated playstyle rather than a universal dialogue choice.

All generated and surviving village merchants use IC Credits instead of emeralds. Normal right-click trades, sneak-right-click requests eligible membership, and a member at 60 reputation can spend eight credits to recruit a companion. Hostile NPCs attack, guards protect settlements, and companions follow and defend their owner. The pause-menu faction directory explains these rules and shows every known or aspirational faction entry.

New worlds suppress endless vanilla village generation and place three primitive settlements close to spawn at approximately 240, 520, and 800 blocks. Abandoned factories begin beyond 900 blocks, militia outposts beyond 1,400, guarded operational specialty factories beyond 2,200, and industrial cities beyond 3,000. Operational factory markets specialize in steel, electronics, fuel, armaments, or research. Existing world chunks are never deleted, so geography validation requires a fresh test world.

Beyond the primitive zone, a regional three-block road grid appears: dirt approaches precede paved double-slab roads nearer industrial regions. Selected outposts, operational factories, and cities receive real IC2 cable spines, solar generation, wall-height outlet points, and BuildCraft transport holders. Cities receive nation-managed cargo controllers on a shared exchange channel and deterministic local specialties. Neutral coal/component trades are deliberately more favorable than crafting IC Credits; friendly and trusted reputation improve prices only one or two steps, preventing reputation from trivializing the economy.

The Car Workshop manufactures six distinct Onysd chassis: a compact city car, frontier off-roader, passenger/service carrier, agricultural tractor, utility cart, and scout ATV. Generic Onysd workstation crafting is removed. The service carrier extends the minibus with 54 item slots, a 64,000 mB fluid tank, mobile crafting, and capability access through a nearby Vehicle Service Dock. Both the Car Workshop and Gun Factory deploy as large equipment sets from one controller. They require player-provided cover: direct rain rusts the controller and halts production until a Repair Bench consumes one IC2 Machine Block.

## Visual design

Custom content uses original IC2-adjacent pixel art: pale blue-gray casing, dark inset work areas, cyan instrumentation, and copper/orange energy accents. World faces remain 16×16; inventory/NEI sprites are authored at 64×64 from `progression/runtime-content.json`. Five 512×512 quest backgrounds represent Earth industry, orbit, lunar/quantum, Mars/matter, and post-AI civilization. Backgrounds are deliberately dark with open center space for readable nodes.

The art source of truth and reproducible review assets are documented in `progression/runtime-content.json`; generated quest-background prompts are recorded in `docs/art/QUEST_BACKGROUND_PROMPTS.md`.

## Validation and deployment

Offline checks cover registry objects, models/textures, runtime recipes, quest counts/prerequisites/task types, energy interop, keybind defaults, JAR integrity, and absence of placeholder IDs. They do not claim to simulate Forge rendering or a complete multiplayer playthrough.

Quest/config changes can be loaded into an existing test world with `/bq_admin default load` after backing up its quest data. Resource additions and first-party JAR changes require a client restart. The source tree is deployed to `/Users/cory/Library/Application Support/technic/modpacks/tekkit-2`; that installed directory is a test target, not the Git authority.

## Implemented now

- 16 chapters, 6 independent side paths, 122 automatic quests, full aspirational visibility, no manual checkbox tasks.
- Story/mission/proof/control descriptions and actual objective icons for the entire generated quest set.
- Five era-specific quest backgrounds and IC2-styled custom block/item/GUI art.
- Eighteen first-party blocks, twenty-two progression artifacts, and the IC Credit currency with real construction/production paths.
- EU-native machines, environment-aware research, Moon/Mars gates, solar multipliers/dust behavior, cargo channels, salvage route, AI synthesis, curated AE2 unlock, and concrete post-AI proofs.
- Stone-tier axe and better whole-tree behavior, IC2 chainsaw whole-tree behavior, 3×3 drill, 9×9 diamond drill, proportional durability/EU cost, and Sneak precision override.
- Static/runtime-content/progression/energy validation harnesses and private Git version control.

## Remaining work

### Required playtest verification

1. Restart once and visually inspect all five quest backgrounds, representative evidence icons, wrapping/scrolling of long descriptions, and locked-node browsing.
2. Import pack version 6 into a disposable world and verify representative retrieval and advancement tasks across Earth, orbit, Moon, Mars, AI, and a side path.
3. Complete a measured critical-route playthrough, record resource/power bottlenecks, and tune toward the intended 3–6 hour test route without adding arbitrary microcrafts.
4. Exercise every custom machine GUI/peripheral method and cross-dimensional cargo channel under chunk unload/reload and save/restart.

### Known implementation gaps

- Galacticraft handles oxygen/pressure; richer habitat integrity and radiation simulation are not implemented. Unauthorized dimension entry is corrected after transfer because this Galacticraft fork offers no stable pre-transfer cancellation hook.
- The Analyzer accepts only Martian Desh metadata 2. Earth/lunar comparative material studies and a general experiment framework remain future content.
- Procedural settlements, trade prices, NPC pathfinding, reputation pacing, faction combat, and companion behavior need a fresh-world measured playtest and balance pass.
- AE2 has a curated foundation set, not a fully reconstructed balanced recipe tree for every part.
- Matter, fusion, logistics, megastructure, colony, and civilization-scale AI have concrete machine proofs, but their physical scale, throughput, and resource balance need full-playthrough tuning.
- Static validation cannot prove Better Questing GUI rendering, existing-world import behavior, or every third-party runtime event.
- Public release work is intentionally deferred: third-party licenses, authorized download links/Technic Solder, distribution packaging, and compatibility migration testing are not complete.

This document describes the current build, not aspirational functionality disguised as complete. When runtime behavior or canonical progression changes, update the machine-readable source and regenerate the projections before changing this document.
