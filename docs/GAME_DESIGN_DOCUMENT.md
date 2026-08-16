# Industrial Civilization — Game Design Document

- Status: 0.5.0 hidden internal alpha; numbered campaign live-validated
- Pack: Industrial Civilization: Astra / Minecraft 1.12.2 / Forge 14.23.5.2860
- First-party runtime: IndustrialCivilizationCore 0.5.0
- Industrial Civilization pack release: 0.5.0 (Technic base selector: 1.2.6)
- Authoritative data: `progression/*.json`; generated quest pack: `config/betterquesting/DefaultQuests.json`

## Vision

Industrial Civilization is an IC2 expansion-scale progression pack about turning one workshop into a permanent, multi-world technical civilization. The player fantasy is not collecting isolated machines. It is learning to make energy, materials, logistics, research, and automation reinforce one another until Earth, orbit, the Moon, and Mars operate as one system.

The design has four pillars:

1. **IC2 is the visible technical language.** Custom machines consume and display EU, use IC2 voltage tiers, share the IC2-adjacent casing palette, and call compatibility energy “IC2” in player-facing UI.
2. **Capability gates, not component chores.** Mandatory quests prove functional workshops, stations, colonies, and research programs. Small parts are taught only when they explain a system.
3. **Research comes from place.** Orbit, lunar darkness, lunar manufacturing, Martian autonomy, and Desh analysis produce knowledge that Earth cannot substitute.
4. **The AI Age begins the endgame.** It authorizes AE2 and parallel civilization-scale programs; it is not a victory screen.

## Player-facing rules

- F6 opens the Better Questing guide. Pause > Advancements opens Minecraft's advancement screen, whose Industrial Civilization tab mirrors the intended progression.
- All 129 quests use aspirational `ALWAYS` visibility. Locked future lines remain visible; secret objectives are not used.
- Every quest completes automatically through a runtime advancement or non-consuming inventory evidence. There are no manual checkbox tasks.
- Every modded quest includes the relevant Mac/no-numpad controls and operating instructions in its description.
- Every node picture is the actual required item, first evidence item, or an explicit real machine/artifact/vehicle override. Symbolic storyboard pictures are not emitted.
- Numbered chapters contain the critical route. Seven independent side-path tabs can be pursued whenever their prerequisites or discoveries permit, including faction/discovery objectives and strategic ICBM defense.
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

The generator also owns story openings/transitions, contextual control blocks, evidence-driven icons, five era backgrounds, and `pack_version` 9. Each quest tab begins at the center of its artwork and expands through deterministic rotated rings or ellipses, giving connector paths varied wheel, arc, diamond, and orbital geometry. Existing worlds must import the new Better Questing default after backup; new worlds receive it directly.

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
| Onysd Vehicles | W/A/S/D drive; Option+K horn; Option+L cycle seats; sneak-right-click parked service carrier for cargo |

Ordinary right-click interaction and machine-specific operational steps are stated beside these bindings; the quest text does not send the player to an external wiki or the Controls menu.

## First-party machines: construction and behavior

Recipes below use E-Circuit = `ic2:itemmisc:451` and A-Circuit = `ic2:itemmisc:452`. Exact shaped layouts remain visible through HEI; rows summarize the authoritative Groovy recipes.

| Block | How it is built | Runtime role |
|---|---|---|
| Molecular Analyzer | Meteoric Iron + advanced ComputerCraft computer + steel + IC2 MV machine + A-Circuits + Martian Desh | Tier-3 IC2 sink; 25,000-EU buffer; right-click with Desh metadata 2 consumes 6,250 EU and one sample to create a non-replicable Material Pattern Record |
| Electric Fabricator | Steel, piston, E-Circuits, IC2 LV machine, rubber, crafting table | 40,000-EU buffer; 5,120-EU work at a 32-EU/t, 160-tick baseline; legal aggregate input scales manufacturing |
| Programmable Assembler | Steel, advanced computer, A-Circuits, Electric Fabricator, pistons, redstone | 120,000-EU buffer; 30,720-EU work at a 128-EU/t, 240-tick baseline; queues 1–64 operations |
| Robotic Manufacturing Cell | A-Circuits, advanced computer, steel, Programmable Assembler, pistons, IC2 MV machine | 400,000-EU buffer; 163,840-EU work at a 512-EU/t, 320-tick baseline; dimension-aware, burst-scalable manufacturing |
| Research Station | Glass, advanced computer, steel, IC2 MV machine, A-Circuits, Programmable Assembler | 100,000-EU buffer; 19,200-EU energy-limited archive computation at a 32-EU/t, 600-tick baseline |
| Orbital Experiment Module | Meteoric Iron, glass, steel, Research Station, A-Circuits, Blank Data Cartridge | 80,000-EU buffer; 19,200-EU work plus a real 600-tick observation floor; records environment-tagged data |
| Factory Control Terminal | Not craftable; generated in rare abandoned-factory structures and repaired through the encounter | Staged salvage encounter and alternate industrial/programming capacity proofs |
| Environmental Solar Array | Glass, E/A-Circuits, IC2 MV machine, steel, Control Processor | 200,000-EU buffer; outputs 8 EU/t Earth, 96 orbit, 32 Moon, and 16 Mars with deterministic dust derating to 4 |
| Tracking Solar Array | Observers, Control Processor, three Environmental Solar Arrays, A-Circuits, advanced computer | 192 EU/t in orbit and 12 EU/t on Earth; grants sustained tracking-array proof after real generation |
| Matter Replicator | A-Circuits, AI Core, IC2 HV machines, Robotic Cell, obsidian, Material Pattern Record | 8,000,000-EU buffer; 4,096,000-EU work at a 2,048-EU/t, 2,000-tick baseline; fully burst-scalable after AI |
| Fusion Research Core | Nether Stars, AI Core, IC2 HV machines, Matter Replicator, obsidian, Control Processor | 40,000,000-EU buffer; 32,768,000-EU work at an 8,192-EU/t baseline plus a 600-tick containment floor |
| Interplanetary Cargo Controller | Meteoric Iron, advanced computer, Control Processors, Robotic Cell, Ender Eyes, recovered factory control system | 4,000,000 EU / 512 EU-t; paired same-name channels move one item between loaded controllers in different dimensions |
| Orbital Megastructure Controller | Beacons, AI Core, IC2 HV machines, Cargo Controller, obsidian, Control Processor | 24,000,000 EU / 8,192 EU-t / 2,000 ticks; in orbit consumes post-AI proofs to create a Megastructure Control Record |
| Autonomous Colony Beacon | Beacons, AI Core, Control Processors, Cargo Controller, diamonds, Robotic Cell | 8,000,000 EU / 2,048 EU-t / 1,200 ticks; on Moon or Mars produces a colony charter from AI, control, and cargo proofs |
| Car Workshop | Advanced IC2 and programmable components | Places a 9×7 assembly set piece; covered 128-EU/t manufacturing programs create six Onysd vehicle crates |
| Gun Factory | Advanced IC2 and programmable components | Places a 9×7 armament set piece; covered 512-EU/t programs produce shotgun and automatic-rifle outputs |
| Repair Bench | IC2 machine components | Repairs the nearest rusted large workshop within 12 blocks, consuming one complete IC2 Machine Block |
| Vehicle Service Dock | BuildCraft pipe and IC2 machine components | Bridges BuildCraft item/fluid capabilities to a parked Industrial Service Carrier within four blocks |

All twelve processing machines are IC2 EU sinks with sided inventories (inputs on non-bottom faces, output below), a four-slot IC2-styled GUI, automatic matching for inserted inputs, and a ComputerCraft peripheral. Total-EU work preserves each historical baseline while independently legal packets aggregate into higher throughput. Experiment, fusion, megastructure, and colony processes retain only their documented observation/stabilization floors. The same blocks expose Forge Energy only as hidden compatibility plumbing at the canonical 8 FE = 1 EU ratio.

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
| Artificial Industrial Intelligence Core | Robotic Cell: processor + Martian archive + pattern record | Durable crafting key for the complete reconstructed AE2 catalog and post-AI machines; triggers one-time credits when all AI gates are met |
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
- **ComputerCraft/Computronics:** monitoring and first-party peripheral control. Machine methods expose status, energy, capacity, progress, environment, recipes, queues, completion counts, cargo channels, accepted EU/t, input tier, baseline, speed multiplier, work totals, and ETA.
- **Galacticraft:** rockets, Moon/Mars dimensions, habitats, oxygen, and cargo hardware. The travel map retains its visual astronomy but its actionable destination whitelist is narrowed to orbit, research-gated Moon, and Quantum/authorization-gated Mars. Because the Moon intentionally has no dungeons/inorganic structures, Mars Mission Authorization directly unlocks the Tier 2 NASA Workbench page; server transfer checks remain a backstop.
- **Railcraft/BuildCraft/ProjectRed/MFFS/Techguns:** freight, extraction/logistics, wireless control, containment/defense, and industrial armament.
- **ICBM Classic:** private-test strategic weapons dependency. Duplicate steel, circuits, wire, and battery parts are hidden and uncraftable; launch hardware uses IC2 MV/HV blocks, circuits/cable, steel, computers, and first-party processors. Runtime power comes from the pack's 8:1 IC2 conversion bridge. Native nuclear, antimatter, and red-matter shortcuts are disabled.
- **Onysd Vehicles:** real driving physics and chassis. The pack curates six roles and extends the minibus into a 54-slot, 64,000-mB mobile workshop that interfaces with BuildCraft while parked.
- **AE2:** every inherited crafting output is captured and reconstructed behind the AI Age. Twelve foundation outputs use the durable AI Core directly; the remaining catalog additionally consumes an Energy Acceptor and IC2 advanced circuit.
- **Better Questing + Standard Expansion:** player-facing tutorial, story, automatic tasks, and aspirational map.
- **GroovyScript:** reloadable recipe locks, HEI filtering, firearm/analyzer recipes, and tooltips. Java owns persistent state and runtime behavior.

## NPCs, factions, and settlement geography

IndustrialCivilizationCore implements six persistent factions: the Frontier Cooperative, Riverside Works Consortium, honorable Civil Defense, evil Territorial Militia, Survey Detachment 7, and Ashline Raiders. Civil Defense is attached only to cities and honorable factories; its effective standing is the lowest of Civil Defense, Riverside, and Survey reputation. Territorial Militia occupies militia outposts, armament/criminal factories, and abandoned factories and shares an underworld alignment with Ashline Raiders. NPC attitudes are derived from per-player reputation, possessions, actions, and membership.

All generated and surviving village merchants use IC Credits instead of emeralds. Normal right-click trades, sneak-right-click requests eligible membership, and a member at 60 reputation can spend eight credits to recruit a companion. Hostile NPCs attack, guards protect settlements, and companions follow and defend their owner. The pause-menu faction directory explains these rules and shows every known or aspirational faction entry.

Earth equipment markets have two ceilings: fixed settlement capacity and live stock equal to one less than the interacting player's industrial stage. Cooperatives sell agricultural utility vehicles; cities sell compact cars and passenger carriers; militia sell ATVs, off-roaders, pistols, shotguns, and eventually rifles; survey sites sell expedition vehicles. Retail is 36–128 credits and reputation discounts stay small. Vehicle construction consumes 12–28 precision frames, 4–16 processors, and 32–64 steel; firearms require 4–16 frames plus IC2 electronics and steel. A pristine used item returns at most 32% of retail, falling with condition, so stores are useful without bypassing progression.

New worlds suppress endless vanilla village generation and place three primitive settlements close to spawn at approximately 240, 520, and 800 blocks. Abandoned factories begin beyond 900 blocks, militia outposts beyond 1,400, guarded operational specialty factories beyond 2,200, and industrial cities beyond 3,000. Operational factory markets specialize in steel, electronics, fuel, armaments, or research. Each settlement has a persistent stockpile ledger. Nearby real inventory items and completed IC Credit trades enter that ledger; deterministic production recipes operate every 1,200 ticks; exact wood/stone/food/iron/circuit/fuel/credit bills pay for three physical building expansions. No upgrade chance or random free material is used.

Beyond the primitive zone, a regional three-block road grid appears: dirt approaches precede paved double-slab roads nearer industrial regions. Selected outposts, operational factories, and cities receive real IC2 cable spines, solar generation, wall-height outlet points, and BuildCraft transport holders. Cities receive nation-managed cargo controllers on a shared exchange channel and deterministic local specialties. Neutral coal/component trades are deliberately more favorable than crafting IC Credits; friendly and trusted reputation improve prices only one or two steps, preventing reputation from trivializing the economy.

Twenty-five percent of eligible vanilla Earth zombie spawn attempts become network-registered, human-rendered robbers; the rest are suppressed. Natural conversion stops while four robbers already exist within 64 horizontal and 24 vertical blocks, and ordinary robbers follow normal hostile-mob despawning so abandoned groups do not accumulate. These defaults are configurable under `ecology` in `config/industrialcivilization/runtime.cfg`. Robbers are independent human mob entities with fully opaque skins, silent ambient behavior, human injury/death audio and cloth footsteps; they do not inherit zombie bodies, groans or undead daylight behavior. Early robbers use melee weapons, break wooden doors, raid nearby inventories, and steal wooden utility blocks. As the nearest player's stage rises they gain firearms, range, limited explosives, and a one-in-eight chance for a two-member squad. Stolen stacks remain in robber NBT and drop on death.

Vanilla Earth skeleton attempts only become network-registered, human-rendered Territorial Militia patrol riflemen when they occur within 128 blocks of a registered militia outpost; attempts elsewhere are suppressed. At most six natural patrols may occupy that patrol region, and they use normal hostile-mob despawning so repeated nights do not create a permanent army. Radius and cap are configurable under `ecology`. Patrols are independent human mob entities with no skeleton geometry, rattling, daylight burning or inherited bow AI; they use human injury/death audio and cloth footsteps. Patrols are neutral unless the player carries a firearm anywhere in inventory, has dismantled three distinct militia outposts, or personally attacks their local patrol group. Firearm recognition uses Techguns' gun interface and explicit launcher/laser/blaster/rifle/pistol families, so holstered and exotic weapons count. A patrol fires an accurate, visible 5.5-damage projectile every 24–32 ticks when it has line of sight; sword rushing is intentionally lethal, while cover, movement, arrows, and environmental traps remain viable. Direct or arrow aggression creates a small militia-reputation loss and local permanent aggro. Patrol penalties are floored at −10, above the hostile threshold, so even 100 patrol kills cannot start a global war. Environmental and explosive trap kills carry no reputation penalty. Each rifle has a 35% drop chance. Other vanilla hostile mobs are suppressed on Earth; string, gunpowder and slime remain available through appropriately specialized settlement markets, while Technical Phase Pearls remain AI-age manufacturing content.

Militia outposts register persistent world coordinates when generated. Breaking sixteen blocks within an outpost records one takedown and −8 militia reputation even if every guard is already gone. Three different outpost takedowns cross the hostility threshold and make patrols treat that player as an organized threat.

The Moon has no natural mobs, villages, or dungeons. Newly generated dungeon chunks are returned to lunar stone. Apollo 11, 12, 14, 15, 16 and 17 use NASA latitude/longitude projected at 24 blocks per degree, with mission/date/coordinate plaques and United States flags; player-built bases are untouched. Mars permits only Galacticraft-native creatures before AI. After AI, both new terrain and already-generated chunks are deterministically processed when an AI-age player loads them, with a bounded eight-chunk work budget per scan. Sparse settlements, outposts, cities, roads, and utilities can then appear. Earth equipment-market rules do not automatically apply there.

The Car Workshop manufactures six distinct Onysd chassis: a compact city car, frontier off-roader, passenger/service carrier, agricultural tractor, utility cart, and scout ATV. Generic Onysd workstation crafting is removed. The service carrier extends the minibus with 54 item slots, a 64,000 mB fluid tank, mobile crafting, and capability access through a nearby Vehicle Service Dock. Both the Car Workshop and Gun Factory deploy as large equipment sets from one controller. Their twelve architecture components—frames, casings, plates, floors, hazard stripes, cable blocks/covers, reinforced glass, tool walls and cabinets—are normal independently craftable/placeable blocks. They require player-provided cover: direct rain rusts the controller and halts production until a Repair Bench consumes one IC2 Machine Block.

Machine/market firearms carry persistent condition and wear when used. Placed vehicles accumulate sampled mileage wear, boosted driving wears faster, severe wear limits operation, and zero condition immobilizes the chassis. One IC2 LV machine block restores a parked vehicle; the Repair Bench restores a held firearm with the same material. Exact damaged-item NBT controls resale offers.

## Visual design

Custom content uses original IC2-adjacent pixel art: pale blue-gray casing, dark inset work areas, cyan instrumentation, and copper/orange energy accents. World faces remain 16×16; inventory/NEI sprites are authored at 64×64 from `progression/runtime-content.json`. Five 512×512 quest backgrounds represent Earth industry, orbit, lunar/quantum, Mars/matter, and post-AI civilization. Backgrounds are deliberately dark with open center space for readable nodes.

The art source of truth and reproducible review assets are documented in `progression/runtime-content.json`; generated quest-background prompts are recorded in `docs/art/QUEST_BACKGROUND_PROMPTS.md`.

## Validation and deployment

Offline checks cover registry objects, models/textures, runtime recipes, quest counts/prerequisites/task types, energy interop, keybind defaults, JAR integrity, and absence of placeholder IDs. They do not claim to simulate Forge rendering or a complete multiplayer playthrough.

Quest/config changes can be loaded into an existing test world with `/bq_admin default load` after backing up its quest data. Resource additions and first-party JAR changes require a client restart. The source tree is deployed to `/Users/cory/Library/Application Support/technic/modpacks/industrial-civilization-astra`; that installed directory is a test target, not the Git authority. The older `tekkit-2` directory is retained only as pre-pack migration context.

## Implemented now

- 16 chapters, 7 independent side paths, 129 automatic quests, full aspirational visibility, no manual checkbox tasks.
- A single connected Industrial Civilization advancement page exposes 324 achievable nodes in gameplay order, including 194 ported objectives from Minecraft and integrated mods. Foreign advancement pages are hidden, and 37 impossible, duplicate, magical, End-only, or fake-monster objectives are removed or replaced.
- Story/mission/proof/control descriptions and actual objective icons for the entire generated quest set.
- Five era-specific quest backgrounds and IC2-styled custom block/item/GUI art.
- Thirty first-party blocks (18 machines/utility blocks plus 12 workshop architecture components), twenty-three first-party items, and the IC Credit currency with real construction/production paths.
- EU-native machines, environment-aware research, pre-selection Moon/Mars gates, solar multipliers/dust behavior, cargo channels, salvage route, AI synthesis, complete AI-gated AE2 catalog, and concrete post-AI proofs.
- The End and portal activation are disabled. Natural pearls are suppressed; the compatible pearl registry item becomes a custom-rendered Technical Phase Pearl with an AI-only IC2 recipe and no pre-AI dependencies.
- AI entry grants its advancement and opens the Industrial Civilization credits once per player, prominently crediting creator `corysmart`, then returns to the still-playable post-credits world.
- The main menu uses a pack-owned widescreen steam-to-electric-to-space industrial city, the Industrial Civilization title plate, and no inherited Tekkit promotional links. The macOS/desktop window and crash metadata also identify Industrial Civilization.
- Space radiation requires a powered sealed Oxygen Sealer habitat or full IC2 QuantumSuit, and the Analyzer now performs real Earth/Moon/Mars comparison.
- Stone-tier axe and better whole-tree behavior, IC2 chainsaw whole-tree behavior, 3×3 drill, 9×9 diamond drill, proportional durability/EU cost, Sneak precision override, 512-log support, and a 12-extra-block-per-tick server work budget.
- First-completion quest timestamps/evidence sources and sustained telemetry detection for functional orbital, lunar and Mars bases.
- Deterministic material-backed settlement upgrades, post-AI processing of existing Mars chunks, historically mapped Apollo markers, initial faction NPC skins, and a 15–30 second terrain warmup.
- JUnit mechanics rules plus the documented HeadlessMC real-client E2E route.
- Static/runtime-content/progression/energy validation harnesses and public Git source control.
- Live acceptance of all 111 numbered Better Questing tasks through Chapter 16, including final-milestone save/reload persistence and continued post-campaign play.

## Remaining work

### Required playtest verification

1. Complete a measured survival campaign without Creative-staged ingredients, record resource/power bottlenecks, and tune pacing without adding arbitrary microcrafts.
2. Run dedicated acceptance passes for all seven independent side paths; ten faction/vehicle side-line advancements remained intentionally incomplete at the end of numbered-campaign acceptance.
3. Exercise Earth and space factions, equipment drops, procedural settlements/cities/fabrication centers, vehicle handling, and world-generation spacing across fresh chunks and migrated low-exploration worlds.
4. Exercise every custom machine GUI/peripheral method, cross-dimensional cargo channel, and multiplayer ownership path under chunk unload/reload and save/restart.

### Verification and release boundaries

- Procedural settlements, trade prices, NPC pathfinding, reputation pacing, faction combat, vehicle handling, and capstone throughput require measured playtesting for final numerical tuning; their gameplay paths are implemented.
- Static validation cannot prove Better Questing/Galacticraft GUI rendering or every third-party runtime event, so the disposable-world checklist remains authoritative.
- Public release work is intentionally deferred: the hidden Technic/GitHub delivery path is operational, but the remaining third-party redistribution review, attribution, multiplayer QA, and release packaging are separate from gameplay completion.

This document describes the current build, not aspirational functionality disguised as complete. When runtime behavior or canonical progression changes, update the machine-readable source and regenerate the projections before changing this document.
