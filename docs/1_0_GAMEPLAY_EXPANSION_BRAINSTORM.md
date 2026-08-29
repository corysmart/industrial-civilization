# Industrial Civilization 1.0 — Gameplay Expansion Brainstorm

Status: design analysis only. No gameplay, recipe, progression, or quest changes are made by this document.

## Executive answer

Industrial Civilization does not need another technology age for 1.0. It already has a coherent 16-chapter path from a manual workshop to AI, replication, interplanetary cargo, colonies, and orbital megastructures. What it lacks is a sufficiently physical answer to the question, “What is all this capacity for?”

The smallest convincing 1.0 addition is a three-part operating loop:

1. **Civilization service programs** give settlements and remote facilities persistent, legible needs for construction, power, goods, and transport. Meeting a need changes a real site and creates a useful service, not a quota trophy.
2. **Observable utility and freight networks** turn those needs into engineering problems: capacity, reserves, priority, congestion, and recoverable shortages across IC2 grids and physical logistics.
3. **AI policy control** lets the post-AI player state objectives—reserves, priorities, load classes, and route buffers—while ComputerCraft, AE2, cargo controllers, and the player-built factory remain the means of execution.

Together these change the endgame from collecting proof artifacts into operating a civilization:

```text
settlements and facilities request useful services
    ↓
the player's factories supply construction and operating inputs
    ↓
freight and power networks reveal bottlenecks
    ↓
expanded sites unlock production, research, exchange, or resilience
    ↓
new capability supports larger and more distant projects
    ↓
AI coordinates policies across infrastructure the player actually built
```

The core design principle should be: **the game creates demand by letting infrastructure do useful work, then lets the player decide how to engineer the supply.**

## 1. Verified baseline and source method

This analysis uses the repository's declared authority order rather than remembered mod behavior:

- `progression/chapters/*.json`, `progression/side-paths/*.json`, and `progression/progression-graph.json` define progression.
- `config/betterquesting/DefaultQuests.json` is the generated, current player-facing questbook: 26 lines, 145 quests, `pack_version` 20.
- `progression/unified-advancements.json` and `docs/UNIFIED_ADVANCEMENTS.md` define the advancement projection and foreign-mod anchoring.
- `docs/GAME_DESIGN_DOCUMENT.md`, system-specific documents, and `progression/pacing.json` explain design intent.
- `MachineRecipe.java`, `TileIndustrialMachine.java`, `SettlementEconomySystem.java`, `CivilizationWorldGenerator.java`, `FactionSystem.java`, `TileEnvironmentalSolarArray.java`, and related runtime classes establish what the current systems actually do.
- Groovy post-init scripts and `docs/ITEM_UNIFICATION_AUDIT.md` establish recipes and material vocabulary.
- `manifest/pack-version.json` identifies the current release as Industrial Civilization/Core 0.8.0 on Technic base 1.2.6. The game design document calls it a hidden internal, live-validated candidate.

When prose intent is ahead of runtime proof, this document says so. In particular, Chapter 16 names several civilization-scale behaviors, but some current quest objectives prove only possession of an associated artifact or controller.

### Current gameplay loop

The verified loop is:

```text
secure a workshop
→ establish safe IC2 electricity and ore processing
→ automate extraction, routing, and fixed crafting
→ build steel, fuel, freight, and heavy industry
→ add ComputerCraft telemetry and programmable manufacturing
→ engineer monitored nuclear power
→ establish orbit and perform orbital research
→ establish and industrialize the Moon
→ manufacture lunar-gated Quantum capability
→ establish and automate Mars
→ record Martian matter and synthesize an AI Core
→ unlock AE2, cargo, replication, fusion/antimatter, colonies, and megastructures
→ continue expanding without a terminal victory
```

The progression is capability-driven. The questbook generally asks for a working system—safe voltage, routed processing, a stable reactor, a sealed habitat, local off-world manufacturing—not raw totals. Existing infrastructure is explicitly meant to remain useful.

### Actual progression ordering and pressure

| Existing era | Current capability | Current gameplay pressure | Proposed additional pressure |
| --- | --- | --- | --- |
| 1. Early Survival and Workshop | Secure workshop, durable starter storage, manual production | Shelter, material families, organization | None; keep the opening compact |
| 2. Electrification | IC2 LV generation, storage, processing, voltage literacy | Safe packets, fuel, cable loss, ore throughput | Optional first settlement utility connection only |
| 3. Automated Industry | Quarry, routed processing, chunk loading, wireless control, fixed autocrafting | Transport jams, unattended throughput | First repeatable local delivery/service route |
| 4. Heavy Industry | Railcraft steel, refined fuel, advanced IC2, freight, defense, factions | Scale several continuous lines together | Settlement public works and regional freight become useful consumers |
| 5. Programmable Manufacturing | ComputerCraft telemetry, Fabricator, Assembler, queues | Missing inputs, scheduling, multi-step flow | Service-level monitoring and reserve reporting |
| 6. Nuclear Age | Stable reactor, telemetry, SCRAM, containment | High-output safety and continuity | Separate critical/noncritical loads; prove reserve and recovery |
| 7. Orbital Age | Permanent habitat, communications, research equipment | Launch planning, oxygen, eclipse storage | Treat the station as a supplied facility, not a single mission |
| 8. Orbital Research | Environment-tagged data, 96/192 EU/t orbital solar | Uptime and experimentation | Establish an orbital receiving/distribution hub; optional export of energy-intensive products |
| 9. Lunar Settlement | Habitat, mining, manufacturing, cargo | Long darkness, startup imports, local production | Explicit import buffer followed by increasing local substitution |
| 10. Lunar Research | Lunar archive, precision manufacturing | Darkness endurance and local research | Maintain a precision/research service whose usefulness continues |
| 11. Quantum Technology | EV industry, Robotic Cell, Moon-only Lunar Quantum Component, MFSU bursts | High-tier packets, charging, burst topology | Supply high-value infrastructure projects; no new component tier |
| 12. Mars Settlement | Habitat, water, mining, Desh, manufacturing, return route | Dust-derated solar and long resupply | Establish measurable reserves and local replacement capacity |
| 13. Martian Autonomy | Shortage response, load shedding, unattended production | Recover without direct intervention | Turn Mars into the first full utility/freight service-level trial |
| 14. Lite Matter Engineering | 6,250-EU Desh analysis and durable pattern record | Cross-world sample preparation | No bulk sink; preserve Desh as a scientific and limited engineering input |
| 15. AI Age | AI Core, Technical Phase Pearl, AE2 authorization | Reconcile accumulated research and industry | Unlock policy-level control, not merely better storage/crafting |
| 16. Post-AI Civilization | AE2, cargo key/controller, UU-Matter, replication, fusion/antimatter, colony and megastructure records | Huge EU bursts and artifact production | Operate multiple sites under persistent demand, failures, policies, and expansion choices |

### Existing optional paths

The current questbook exposes ten optional lines, even though five have standalone side-path JSON files and five are projections of optional milestones embedded in numbered chapters:

- Field Engineering: electric tools, chunk loading, wireless control, containment, recovery, and the Walking Quarry.
- Factions and Salvage: contacts, membership, companions, abandoned-factory restoration, and alternate industrial/programming access.
- Orbital Power: environmental and tracking solar.
- MFSU Burst Power: one/four/ten/fifty-MFSU legal packet banks and blink manufacturing.
- Cargo Logistics: regional freight and post-AI cross-dimensional cargo.
- Mobility and Nations: roads, six manufactured vehicles, service carrier, city exchange, and large workshops.
- Strategic Defense: IC2-integrated ICBM launch control, radar, and conventional missiles.
- Post-AI Horizons: cargo, predictive production, fusion, megastructure, colony, and civilization-scale AI branches.
- Industrial Agriculture: IC2 crop breeding, Hemp, String, Leads, and controlled livestock.
- Automated Agriculture: LV Industrial Foregoing trees, crops, husbandry, fertilizer, and water resources.

These are already broad enough. The remaining design problem is integration and repeat use, not another optional mod showcase.

### Advancement and quest philosophy already worth preserving

- The one visible advancement page anchors IC2 after the first generator, BuildCraft/Railcraft after early autocrafting, Galacticraft after nuclear power, and AE2 after AI Age.
- Better Questing descriptions usually follow motivation → mechanic → action and include operating instructions.
- Objectives complete automatically via runtime advancements or non-consuming item evidence.
- Later goals remain visible, and scientific gates cannot be skipped by faction/salvage alternatives.
- The weak point is not presentation. It is that some late descriptions promise coordination, prediction, megastructures, or autonomous colonies while their current evidence is only an item: for example, AI Factory Coordination requires an Interplanetary Cargo Controller; Predictive Production requires the AI Core; the megastructure and colony machines emit records/charters.

### Verified current systems inventory

| System | What currently exists and matters to this brainstorm |
| --- | --- |
| Recipes and material unification | IC2-compatible copper/tin/bronze/lead/uranium families, Railcraft steel, IC2 Rubber/Sticky Resin, IC2 basic/advanced circuits, AE2 Certus as the technical crystal, and IC2 Advanced Alloy are the canonical vocabulary. Duplicate Techguns/ICBM metal, circuit, battery, and wire chains are disabled/hidden. Unique rocket, track, vehicle, weapon, research, and AE2 processor parts remain only when they teach a distinct system. |
| IC2 power progression | LV generation/storage and processing grow into transformed advanced networks, nuclear baseload, EV/Quantum capability, and tier-safe endgame machines. Native overvoltage remains destructive. Forge Energy is hidden compatibility plumbing at 8 FE = 1 EU, normally fed from the EU backbone through Electric Flux Generators. |
| MFSU burst engineering | One, four, ten, and fifty independently connected tier-3 MFSUs can deliver legal 512-EU packets that aggregate into 512/2,048/5,120/25,600 EU/t of work. Energy-limited machines accelerate; scientific/containment floors remain. This is a topology and storage challenge, not a higher voltage tier. |
| Extraction and processing | BuildCraft Quarry output feeds physical pipes/tubes/Logistics Pipes/rail into IC2 ore processing. Chunk loading and wireless redstone are optional reliability tools. |
| Walking Quarry | A first-party controller coordinates a ProjectRed frame carriage, Block Breaker/Placer, redstone bus, BuildCraft Quarry recovery, output routing, and one fixed Worldspike to move the same exhausted Quarry to the next lane without teleportation or a spare. |
| Programmable manufacturing | The Electric Fabricator performs fixed EU recipes; the Programmable Assembler adds selection, queues, shortage reporting, and multi-step production; the Robotic Cell adds high-tier, environment-aware synthesis. All expose ComputerCraft status, recipes, queues, energy, work, speed, and ETA. |
| ComputerCraft integration | Supplied examples cover factory status, reactor monitoring/SCRAM, habitat environment, and rocket fuel. First-party peripherals expose real machine and cargo state; ComputerCraft supervises rather than replaces physical production. |
| Nuclear engineering | Unmodified IC2 reactor mechanics, monitored through Energy Control/Plethora/ComputerCraft, with a remote redstone SCRAM and optional MFFS containment. Stable monitored output gates space industry. |
| Industrial Agriculture | Optional IC2 crop breeding produces tier-3 Hemp, renewable String, Resin-based Leads, and controlled livestock. It replaces fantasy hostile-drop dependencies with a technical biological path. |
| Automated Agriculture | Optional LV-era Industrial Foregoing machines automate trees/charcoal, field crops, breeding/separation, peaceful animal products, sewage/fertilizer, and water resources through IC2-owned recipes and the energy bridge. |
| Factions and settlements | Six persistent factions use reputation, IC Credits, membership, companions, guarded property, specialized markets, and hostile/neutral behavior. Three primitive settlements, abandoned factories, militia outposts, operational specialty factories, cities, roads, and utilities appear in distance bands. Primitive settlements absorb real nearby stock and pay exact bills for three physical upgrades. |
| Roads and vehicles | A generated three-wide road grid changes from dirt to paved slabs in industrial regions. A covered IC2 Car Workshop manufactures six Onysd vehicle roles. The Passenger Carrier has 54 item slots, a 64,000 mB tank, mobile crafting, and a BuildCraft-facing Vehicle Service Dock. |
| Strategic defense | Optional IC2-integrated ICBM launch control, radar, and conventional missiles; Techguns arms; MFFS containment/fields; Civil Defense and militia behavior. Nuclear/exotic payloads do not bypass scientific progression. |
| Regional/nation cargo | Generated cities have powered nation-managed cargo controllers on `earth_nation_exchange`, with deterministic iron/redstone/coal/paper/bread specialties. Loaded controllers restock slowly and exchange one item at a time. |
| Interplanetary cargo | Same-name, loaded controllers in different dimensions move one item per 100 ticks from input to remote output while spending 512 EU per transfer. The current system has channel and transfer telemetry but no manifest, priority, or route-capacity model. |
| Research and location gates | Experiment Modules record environment-tagged orbit/Moon/Mars data. Research Stations validate the tag and prior artifacts. Orbital Archive unlocks Moon; Lunar Archive unlocks Quantum; Moon-made Lunar Quantum Component plus Earth-made authorization unlock Mars; Martian Autonomy Archive and Desh pattern work feed AI. |
| AE2 | The reconstructed AE2 catalog is authorized only after AI Age. It provides network inventory, dependency scheduling, and autocrafting while physical machine throughput remains real. |
| UU-Matter and replication | A 4,096,000-EU Matter Replicator process creates UU-Matter from a processor, durable pattern record, and reagent. A second controlled process makes a replication record, whose use releases one authorized Desh sample. Replication is deliberately narrow, not arbitrary transmutation. |
| Fusion/antimatter | The Fusion Research Core operates in orbit, consumes UU-Matter, AI Core, and processor, requires 32,768,000 EU plus a 600-tick containment floor, and emits a Contained Antimatter Capsule. |
| Megastructures and colonies | Post-AI controllers turn antimatter/cargo/AI evidence into a Megastructure Control Record in orbit, or cargo/processor/AI evidence into a Moon/Mars colony charter. These are strong thematic anchors but currently prove records more than operating physical complexes. |
| Post-AI Mars and space society | Mars chunks loaded after an AI-age player can deterministically gain roads, settlements, factories, cities, and human factions. Moon/Mars fantasy monsters are replaced/suppressed in favor of human citizens, pirates, militia, and salvage behavior. |

## 2. Verified location and environment roles

### Earth

**Current support:** Earth is the only starting industrial ecosystem and the home of the full workshop-to-nuclear chain, IC2 crop/automated agriculture, settlements, faction markets, roads, nation exchange, operational specialty factories, vehicle production, and most inherited mod infrastructure. The Mars Mission Authorization recipe must run in an Earth Research Station. Earth environmental solar is 8 EU/t, or 12 EU/t for tracking.

**Long-term role:** Earth is already the strongest candidate for diversified manufacturing, agriculture, settlement demand, salvage, faction relations, and mature logistics. That role is supported by actual systems, not genre convention.

**Current weakness:** after off-world gates are cleared, Earth settlements do not create enough continuing reason to expand the productive economy. Primitive settlements can consume exact stockpile bills and physically upgrade three times, but tier-3 sites have no further service program.

### Earth orbit

**Current support:** Orbit is the first mandatory destination. It requires a sealed powered station and produces orbital research data/archive. Environmental solar produces 96 EU/t and tracking solar 192 EU/t, versus 8/12 on Earth. Orbit is also the required environment for contained antimatter and the Orbital Megastructure Controller. There is intentionally no direct manufacturing multiplier; high throughput emerges by charging storage and delivering legal aggregate EU packets.

**Long-term role:** high-availability solar power, research, traffic staging, energy-intensive manufacturing chosen by the player, antimatter containment, and megastructure coordination.

**Current weakness:** between the initial research station and the post-AI record-producing megastructure controller, station growth is mostly asserted by quest text. Orbit needs useful receiving, buffering, dispatch, and power-distribution duties that visibly enlarge the station.

### Moon

**Current support:** the Moon requires a permanent habitat, darkness-surviving power, automated mining, local manufacturing, research, and cargo. Environmental solar produces 32 EU/t but has a long darkness challenge. Raw Meteoric Iron is Moon-derived and used in the Molecular Analyzer. Lunar data plus the orbital archive creates the Lunar Engineering Archive. The Lunar Quantum Component can only be manufactured by a Robotic Cell on the Moon and is required for Mars readiness/authorization. A post-AI colony charter can be produced there.

**Long-term role:** precision and research industry built around the already mandatory lunar manufacturing base; a grid where storage scheduling matters; support for orbital infrastructure through proximity in the player's network, without inventing a teleport or numerical crafting bonus.

**Current weakness:** after the Lunar Quantum Component and archive are obtained, continuing lunar output has little systemic consumer. The base remains physically useful only if the player chooses to keep manufacturing there.

### Mars

**Current support:** Mars uniquely supplies Desh. Desh is required to craft the Analyzer and is consumed in its 6,250-EU pattern-record operation. Mars also produces environment-tagged data and the Martian Autonomy Archive. Solar produces 16 EU/t and periodically derates to 4 EU/t under a deterministic dust cycle. Chapter 13 explicitly tests shortage replenishment, load shedding, restoration, and unattended production. After AI Age, loaded and newly explored Mars terrain can gain deterministic human settlements, factories, roads, and utilities. A post-AI colony charter can be produced there.

**Long-term role:** the strongest existing location for autonomy, resilient grids, local replacement production, and post-AI expansion. This role follows the current progression and runtime upgrade system; it does not require Mars to become a generic bulk-mining planet.

**Current weakness:** the autonomy archive is a gate, not an ongoing operating contract. Desh's legitimate use is narrow, and post-AI settlements appear without being deeply tied to the player's supply and policy network.

### Location-specialization conclusions

The current design supports a **network of different operational problems**, not a mandatory four-way commodity loop:

| Location | Evidence-backed distinction | Recommended durable role | Permanent lock? |
| --- | --- | --- | --- |
| Earth | Settlements, factions, agriculture, full inherited industry, Earth-only Mars authorization | Diverse production and civilization demand | No; it is the mature center, not the only factory |
| Orbit | 12×/16× environmental/tracking solar over Earth; orbital research; antimatter/megastructure recipes | Energy-rich hub, dispatch, research, burst production | Only scientific/containment recipes already locked there; ordinary production remains portable |
| Moon | Meteoric Iron, lunar data/archive, Moon-only quantum component, 32 EU/t plus darkness | Precision/research campus and storage-engineering site | Preserve existing key locks; later service outputs may be substitutable at high cost |
| Mars | Desh, dust derating, autonomy archive, post-AI world upgrade | Resilience/autonomy proving ground and growing remote civilization | Preserve Desh/research origin; do not force all bulk resources through Mars |

The design should not make every location exchange with every other. A healthy network can be asymmetric: Earth supplies a new orbital facility; orbit becomes a hub; the Moon supplies occasional high-value precision outputs; Mars initially imports spares, then reduces that dependency through local production. Players may build extra links where their own factory layout makes them advantageous.

## 3. What the game already does extremely well

1. **Technological progression is coherent.** Each era introduces a capability and usually asks the player to prove it physically.
2. **IC2 is genuinely central.** EU packets, tiers, transformers, storage, native overvoltage, reactors, and aggregate legal MFSU bursts matter throughout.
3. **Automation has a readable ladder.** Manual crafting → fixed autocrafting → EU Fabricator → queued Assembler → Robotic Cell → AE2 is a strong progression.
4. **Place matters for research.** Orbit, Moon, Earth, and Mars recipes validate their actual environment and tagged research data.
5. **Off-world settlement is more than flag planting.** Habitation, oxygen, power, extraction, manufacturing, communications, and sustained operation are required.
6. **Cross-mod composition is already a design strength.** The Walking Quarry, reactor SCRAM, vehicle service carrier, orbital burst power, and workshop weather/repair rules ask the player to connect systems.
7. **Optional specialization is broad without blocking science.** Factions, agriculture, freight, vehicles, defense, burst power, and field engineering remain choices.
8. **Failure is often technical and legible.** Overvoltage, eclipse/dust shortages, reactor heat, missing inputs, clogged outputs, rain-rusted workshops, and habitat loss all point toward engineering responses.
9. **Material vocabulary is disciplined.** IC2 circuits/rubber, Railcraft steel, and retained unique components avoid duplicate chains and hostile-drop dependencies.
10. **The pack rejects a terminal victory.** AI Age is a transition, and Chapter 16 deliberately leaves the world open.

## 4. Actual gameplay gaps

### Missing player motivation

The player gains massive throughput, replication, cargo, fusion/antimatter, colony, and megastructure capabilities, but few systems continuously ask for their coordinated use. Chapter 16 says “Explore → Research → Automate → Scale → Colonize → Connect → Build,” yet the runtime mostly awards proof artifacts rather than generating the next engineering situation.

### Weak long-term use of infrastructure

- Primitive settlement growth ends at tier 3.
- Operational factories and cities are generated endpoints rather than sites with evolving service needs.
- Habitats, research stations, and lunar/Martian factories can become gate-completion monuments.
- The post-AI megastructure and colony machines create a record/charter, not a sustained site that needs power, freight, redundancy, and local industry.

### Locations that lose relevance

- Orbit has excellent power and special recipes, but too little intermediate infrastructure progression.
- The Moon has strong one-time scientific and Quantum roles, then weak repeat demand.
- Mars has the best autonomy theme, but autonomy is a finite archive proof rather than a continuing operating state.

### Technologies with little ongoing purpose

- Roads and most vehicles primarily move players; only the service carrier directly joins item/fluid automation.
- Rail freight is optional and can be bypassed by local pipes or later cargo without a distinct high-volume role.
- MFFS and strategic defense protect assets, but peaceful players have little systemic reason to protect distributed infrastructure.
- The AI Core is an authorization catalyst and recipe ingredient. It does not yet change the player's control abstraction.
- Cargo controllers transfer one item every 100 ticks between loaded, same-channel controllers in different dimensions for one 512-EU charge. They prove connectivity but do not expose manifests, priority, route capacity, or shortage policies.

### Insufficient consumption and economic feedback

The settlement ledger is the one real civilization-scale consumer: it absorbs bounded quantities from nearby inventories, runs deterministic abstract production, and pays exact upgrade bills. However, its internal state is not a sufficiently visible operating system, only primitive settlements upgrade, and demand stops after the third stage. City exchange creates goods rather than depending on upstream production, so it demonstrates trade but does not strongly feed demand back into the player's factories.

### Insufficient logistics pressure

The progression teaches local routing and proves cross-dimensional transfer, but little in between requires a regional warehouse, scheduled rail service, route reserves, or prioritization. The best logistics strategy can collapse into “keep both cargo chunks loaded and share a channel.”

### Insufficient reasons for redundancy

Mars power response and nuclear SCRAM teach resilience once, but most infrastructure has no explicit service level. A factory can stop without consequence beyond delayed personal output. There is little reason to build reserve generation, alternate freight, or spare-parts buffers once a quest proof is complete.

### Missing civilization-level management

The repository has machine telemetry but no coherent view of facility status, shortages, route congestion, or grid reserve. The player can script this independently, which should remain possible, but the game does not give their scripts a civilization-scale set of objectives.

### Insufficient post-AI transformation

This is the clearest gap. AE2 improves inventory and crafting; the AI Core unlocks recipes. Neither changes the player's role from automation engineer to systems architect. “Predictive Production” currently completes from AI Core evidence, not from a maintained forecast or buffer. “AI Factory Coordination” completes from a cargo-controller item, not coordinated factories.

### Missing emergent sandbox goals

Settlement geography, roads, factories, orbital sites, and colonies provide excellent places to build. What is missing is a repeatable but finite-at-each-site reason to connect and improve them. The world needs opportunities, not infinite procedural “craft N” quests.

## 5. Tier A — strongly recommended for 1.0

### A1. Civilization Service Programs

**Classification:** critical progression at one introductory proof; thereafter emergent sandbox system. Implementation scope: **High**.

**Player problem:** industrial output has no durable, believable consumer after the player's own technology is built. Existing settlements grow physically but stop demanding services.

**Player fantasy:** become the engineer whose factories, utilities, and transport materially develop inhabited places.

**Existing systems used:** settlement stockpile ledger and physical upgrades; IC Credits and reputation; generated settlement/factory/city geography; IC2 power; agriculture; Railcraft/BuildCraft/Logistics Pipes; roads and vehicles; cargo controllers; ComputerCraft; MFFS and defense where chosen.

**Capability:** each eligible settlement or remote facility can operate one clearly described **service program**. Examples are a public utility, construction expansion, industrial workshop, research annex, freight exchange, agricultural district, emergency reserve, or orbital/Martian facility expansion. A program has:

- a physical receiving point or existing controller;
- a bounded construction bill;
- one or more operating conditions such as delivered EU, maintained food/spares, or freight throughput;
- a visible world change;
- an ongoing benefit and modest ongoing upkeep only while that benefit is used.

This is not a generic request board. Programs derive from site type and existing specialty. A primitive settlement may expand its material yard; an electronics factory may commission a powered logistics annex; a Survey site may operate a data program; an orbital station may add a cargo pier; a Martian settlement may establish a spare-parts and emergency-power district.

**Gameplay loop:** survey a site → inspect its current shortage/capability → build or connect a warehouse, grid, and route → deliver the bounded construction inputs → maintain the operating conditions for a commissioning window → see a physical expansion → use the new service → optionally improve reliability and throughput.

**Inputs:** canonical construction materials, food/fuel where appropriate, manufactured components, actual EU delivery, route capacity, and sometimes reputation or research authorization. Requirements must be drawn from the site's function. A freight annex uses steel, processors, and power because it contains freight equipment; an agricultural district uses machines, water, and power because it produces food/fiber.

**Outputs:** real capabilities: broader or specialized trade stock; a loading terminal; repair/service access; a research or telemetry endpoint; a powered public utility; an expanded physical settlement; better local production; access to a new project at that site. IC Credits and reputation are secondary feedback, not the main reward.

**Progression placement:** first optional introduction in Heavy Industry after `faction_contacts`; one mandatory civilization-service commissioning after Programmable Manufacturing or before AI Age is enough to prove the concept. Off-world variants appear only after the corresponding functional base exists.

**Critical or optional:** one tutorial-scale capability proof should be critical to the late campaign because operating infrastructure is central to the title. Individual sites, specialties, and expansion depth remain optional.

**Automation opportunity:** pipe inputs into the receiving inventory; use rail or service vehicles for regional supply; use ComputerCraft to report remaining bills and operating status; automate agriculture and component production; later apply AI reserve and priority policies.

**Failure states:** missing construction inputs pause work; insufficient EU pauses the service; output congestion pauses production; food/spares shortage reduces or suspends a benefit; a disconnected route drains local reserves. No random destruction or silent downgrade.

**Recovery:** restore the missing service, draw from a local reserve, reroute freight, shed noncritical load, or temporarily supply by vehicle. Progress and completed construction persist.

**Planet/location assumptions:** Earth site types and settlement ledgers already exist. Orbit has a mandatory station and special power/research role. Moon and Mars already require functional bases; Mars additionally has post-AI settlements. Programs should be authored per verified site role, never assigned from generic planet stereotypes.

**Multiplayer value:** builders can develop sites while logistics players establish routes, electrical engineers commission grids, programmers expose status, and faction-oriented players choose projects. All roles are optional and one player can do them sequentially.

**Micromanagement risk:** high if every site consumes many item types continuously. Limit each active program to a few legible categories, use generous buffers, stop consumption when the associated service is idle, and make completed construction permanent.

**Minimal first-party glue:** extend the existing settlement/cargo-controller state and faction directory to expose a service manifest, power/uptime state, and next physical stage. Prefer existing inventories, IC2 sensing, and ComputerCraft peripherals over a new family of blocks. If a physical interface is unavoidable, one reusable service/utility interface is the ceiling.

**Why this is not an arbitrary sink:** every consumed item constructs or operates something visible that provides a service. The settlement is not “eating iron”; it is building a rail shed, maintaining a machine shop, or stocking repair parts.

### A2. Observable Utility and Freight Networks

**Classification:** tutorial capability in Heavy/Nuclear Industry, then emergent sandbox system and megaproject substrate. Implementation scope: **High**.

**Player problem:** machine-level power and item routing are deep, but the game rarely asks the player to manage site-level capacity, reserves, priority, and recovery. Interplanetary cargo proves transfer, not logistics.

**Player fantasy:** operate power and freight as infrastructure—designing substations, buffers, terminals, reserve paths, and graceful failure rather than carrying emergency stacks by hand.

**Existing systems used:** IC2 generators, cables, transformers, storage and MFSU banks; nuclear and solar; redstone control; ComputerCraft/Plethora telemetry; BuildCraft and Logistics Pipes; Railcraft; Vehicle Service Dock/carrier; Galacticraft cargo; chunk loading; first-party cargo controllers; AE2 after AI.

**Capability:** define named facilities and routes using infrastructure the player already builds. The system should measure and expose, not simulate an invisible network:

- facility input/output reserves;
- generation, stored EU, accepted load, and reserve duration;
- critical and noncritical load classes controlled by redstone or ComputerCraft;
- route source, destination, item class, recent throughput, backlog, and last successful delivery;
- local minimum reserves and destination targets;
- terminal congestion and unavailable/unloaded endpoints.

Cargo controllers should gain manifests and per-route telemetry. They should not become a universal storage network. Their current one-item/100-tick, 512-EU transfer can remain a baseline; higher throughput should require parallel controllers, larger buffers, more power, or a deliberately engineered terminal. Rail should own high-volume regional bulk movement; vehicles should own flexible setup/emergency delivery; pipes and Logistics Pipes should own local distribution; cross-dimensional controllers should own scheduled interplanetary transfer.

**Gameplay loop:** identify a consumer → select the appropriate transport domain → build source/destination buffers and route → observe throughput → encounter a deterministic shortage or congestion caused by demand → increase capacity, add reserve, or change priority → verify recovery.

**Inputs:** physical storage, routes, loaded endpoints where required, IC2 power, control processors for advanced terminals, and player-authored ComputerCraft logic.

**Outputs:** predictable delivery, legible bottlenecks, fewer emergency trips, service-program uptime, and a platform for AI policy control.

**Progression placement:** local route metrics begin with Automated Industry; facility power reserve and load shedding become explicit around Nuclear Age; regional freight manifests arrive in Heavy/Programmable Industry; interplanetary manifests extend the existing post-AI cargo controller.

**Critical or optional:** one facility reserve/recovery proof and one freight-manifest proof should be critical before or during AI entry. Rail, vehicles, Logistics Pipes, and custom scripts remain alternative/optional solutions.

**Automation opportunity:** automatic restocking, dispatch when a train/carrier is available, route selection, alerting, load shedding, generator start/stop, and staged recovery. AE2 may schedule manufacturing but must hand products to physical outbound buffers.

**Failure states:** source reserve below target; destination full; endpoint unloaded; route lacks power; train/vehicle absent; output clogged; eclipse/dust event drains reserve; reactor shutdown removes baseload; a priority inversion starves habitat or control systems.

**Detection:** local indicators plus ComputerCraft methods and a compact civilization status view: shortage reason, affected site, last delivery, queue depth, EU reserve, and current policy action.

**Recovery:** draw emergency storage; start backup generation; pause noncritical manufacturing; use a vehicle bridge; reroute to a second terminal; clear output; increase train frequency; add controller lanes; restore the failed source. Nothing disappears and no random disaster is required.

**Planet/location assumptions:** the Moon's darkness and Mars's deterministic dust derating already create reserve problems. Orbit already supplies unusually high solar. Earth already has roads, rail, cities, factories, and agriculture. These mechanics amplify existing differences rather than adding arbitrary location locks.

**Multiplayer value:** freight dispatch, grid operation, factory production, and remote-base construction become independently useful specialties.

**Micromanagement risk:** severe if every item needs a bespoke route or if endpoints require constant chunk-loader babysitting. Support item categories, hysteresis, minimum/maximum targets, clear offline status, and large enough buffers. Do not make route interruption random.

**Minimal first-party glue:** telemetry and manifest state on existing controllers/machines; a common ComputerCraft status contract; perhaps a meter capability on an existing control terminal. Rail, pipes, AE2, and IC2 continue doing the physical work.

### A3. AI Industrial Policy Layer

**Classification:** post-AI system. Implementation scope: **Very High**.

**Player problem:** AI Age currently unlocks AE2 and advanced recipes, but does not materially change the level at which the player controls industry. Several Chapter 16 goals are names without matching operating mechanics.

**Player fantasy:** stop issuing every production order and begin defining what the civilization must maintain—while remaining responsible for the machines, power, storage, sensors, and routes that make the policy achievable.

**Existing systems used:** AI Core; ComputerCraft; AE2 autocrafting and inventory; Programmable Assemblers/Robotic Cells; common machine peripherals; cargo manifests; IC2 grids and redstone control; settlement/facility service programs.

**Capability:** authorize a small policy vocabulary over registered, observable infrastructure:

- maintain a minimum reserve and a preferred reserve;
- prioritize one demand class over another;
- preserve emergency EU reserve;
- pause or resume a redstone-addressable noncritical line;
- request production when a monitored buffer falls below target;
- request freight when a destination reserve falls below target;
- cap exports that would violate a source reserve;
- escalate an unresolved shortage to the player with its causal chain.

Policies must have explicit scope. The AI sees only attached inventories, machines, meters, and registered routes. It can act only through AE2 jobs, ComputerCraft-accessible machines, redstone controls, and cargo dispatch. It cannot place blocks, synthesize absent recipes, teleport goods, or infer hidden inventories.

**Gameplay loop:** install sensors/controllers → register a facility and its actuators → choose reserve and priority policies → run a commissioning scenario → watch a real shortage occur → inspect the AI's action and unmet dependencies → improve the physical system → expand the policy to more sites.

**Inputs:** AI Core authorization, a ComputerCraft or existing control terminal, network access, physical inventories, declared recipes, addressable loads, route manifests, and power.

**Outputs:** fewer repetitive interventions, controlled response to shortages, understandable priorities, and genuine civilization-scale coordination.

**Progression placement:** immediately after `ae2_autocrafting` and after the first observable service/freight network exists. AI policy must not be designed before its physical controlled systems.

**Critical or optional:** a minimal policy commissioning should replace the current weak evidence behind AI Factory Coordination/Predictive Production and be critical to the post-AI identity. Complex policies and multi-site coordination remain optional.

**Automation opportunity:** this is itself an automation layer, but skilled players can outperform defaults with Lua, better facility boundaries, more sensors, well-chosen hysteresis, route redundancy, and separate critical/noncritical grids.

**Failure states:** no known recipe; reserve target impossible; actuator missing; destination offline; insufficient power; policy conflict; route saturated; upstream material exhausted. The AI should never silently cheat or thrash.

**Recovery:** present the blocking dependency; allow the player to lower a target, add capacity, connect an actuator, supply material, reprioritize, or disable a policy. Preserve a manual override and an event history.

**Planet/location assumptions:** none are intrinsic. Policies operate wherever physical telemetry exists. Mars is the natural first multi-policy test because Chapter 13 already teaches shortage and power response; that is progression evidence, not a hard-coded AI bonus.

**Multiplayer value:** policy authors can coordinate systems built by electrical, logistics, factory, settlement, and orbital specialists. Ownership/permissions should prevent one player's policy from commandeering another's infrastructure.

**Micromanagement risk:** a spreadsheet-like editor would overwhelm the game. Use a few Minecraft-readable concepts—reserve, priority, critical load, route—and show status at actual terminals. Advanced players can use Lua; ordinary players can commission a preset and then modify one threshold at a time.

**Minimal first-party glue:** a policy evaluator and shared telemetry/control API. It should orchestrate AE2, ComputerCraft, redstone, IC2, inventories, and cargo rather than replace them.

## 6. Concrete Tier A player scenarios

### Scenario A — a settlement becomes an industrial client

```text
the player meets a Riverside electronics factory
↓
its existing controller reports that a powered freight annex would expand repair/component service
↓
the player connects a regional warehouse and supplies the bounded steel/processor construction bill
↓
the annex cannot commission because its buffer receives parts but its IC2 reserve collapses during factory bursts
↓
the player adds transformed storage and separates the annex from noncritical production
↓
the annex commissions and a physical loading shed appears
↓
the factory now offers repair/component service while consuming parts only when that service operates
↓
the player later automates the service reserve instead of feeding it manually
```

The important output is not reputation or a completion badge. It is a useful, visible facility connected to the player's economy.

### Scenario B — the Moon stops being a completed quest room

```text
the lunar base opens a precision/research service program after the existing archive and quantum component
↓
startup spares arrive from Earth through an orbital receiving hub
↓
a lunar-night interval drains storage and pauses noncritical manufacturing
↓
habitat and communications remain powered because they are classified as critical
↓
the player adds storage, schedules high-energy work during sunlight, and buffers outbound precision products
↓
local mining/manufacturing replaces most imported structural parts
↓
the Moon becomes one specialized node in a larger network, not a permanent arbitrary monopoly on every advanced part
```

### Scenario C — AI changes the player's job

```text
three facilities publish reserves, demand, route health, and load classes
↓
the player sets: preserve habitat EU, keep two stacks of repair parts on Mars, and supply settlement service orders second
↓
a Martian dust derating reduces generation while a freight route is below target
↓
the AI pauses a noncritical Robotic Cell, requests replacement parts from AE2, and dispatches only the amount Earth can spare
↓
the route cannot meet the target because its controller lane is saturated
↓
the player sees the causal chain, adds a parallel lane or local production, and recommissions the policy
↓
the AI handles the next ordinary shortage; the player remains responsible for redesigning the inadequate system
```

## 7. Tier B — strong post-1.0 additions

### B1. Authored regional and planetary development programs

**Classification:** optional specialization / post-AI system. Scope: High.

Build a curated library of site programs on top of A1 rather than inventing a new campaign. Candidate programs, each grounded in current roles:

- Earth: agricultural distribution district, machine-service works, rail exchange, Civil Defense reserve, Survey research annex.
- Orbit: permanent freight pier, eclipse reserve station, solar-charged burst manufacturing hall, experiment campus, shipyard support.
- Moon: darkness-resilient precision campus, Meteoric Iron handling, lunar research support, orbital-construction supply.
- Mars: water/habitat reserve, local spare-parts works, dust-resilient grid, autonomous freight depot, post-AI civic expansion.

Each should use the A1 evaluation template and produce a physical/useful change. None should be required merely to keep a location relevant; 1.0's generic service/freight/policy loop should already do that.

**Inputs/outputs:** canonical materials, EU, freight capacity, and site-specific operating evidence produce a visible annex and a service suited to the location. **Placement/criticality:** after each location's functional-base milestone; all are optional. **Automation:** the complete A2/A3 reserve, manifest, and policy stack. **Failure/recovery:** shortages pause the annex and identify a missing service; supply, reroute, or redesign restores it without losing construction. **Location evidence:** only the roles in Section 2 qualify. **Multiplayer:** strong construction/logistics/programming collaboration. **Micromanagement:** limit each site to one active expansion and a few broad inputs. **Scope:** High because the shared framework is reusable but each program needs authored construction and benefits.

### B2. Regional Freight Terminals

**Classification:** optional specialization / megaproject. Scope: High.

**Capability:** compose Railcraft long-distance bulk movement, Logistics Pipes/BuildCraft local sorting, IC2-powered terminal operations, ComputerCraft dispatch, and vehicle emergency access.

**Player problem:** rail currently has no protected high-volume domain.

**Benefit:** a terminal handles large scheduled batches more efficiently and visibly than many point-to-point pipes. It creates warehouses, loading tracks, sidings, and route redundancy.

**Failure/recovery:** absent train, full siding, empty source, wrong consist, or unpowered loader; recover by dispatching, clearing, rerouting, or bridging with a service carrier.

**First-party glue:** shared manifest/telemetry only. Do not add a custom train or universal loader.

**Inputs/outputs:** powered loaders, rolling stock, track, warehouse space, schedules, and local sorting produce high-volume predictable regional service. **Placement/criticality:** Heavy Industry onward; optional. **Automation:** ComputerCraft dispatch and Logistics Pipes distribution. **Failure/recovery:** absent train, full siding, wrong consist, power loss, or empty source are visible at the terminal and recover through dispatch, clearing, rerouting, or a vehicle bridge. **Location assumptions:** Earth already generates regional roads/factories/cities; off-world rail is player-chosen, never required by planet. **Multiplayer:** a deep freight specialty. **Micromanagement:** batch manifests and automatic return schedules, not per-cart clerical work. **Scope:** High.

### B3. Grid Operations Center

**Classification:** optional specialization / megaproject. Scope: Medium–High.

**Capability:** combine IC2 storage/transformers, Energy Control displays, ComputerCraft, redstone breakers, nuclear/solar generation, and MFSU banks into a facility-level grid dashboard and dispatcher.

**Player problem:** large power systems are hard to reason about as a whole.

**Benefit:** reserve duration, generation margin, critical-load state, recent trip, and peak accepted EU become visible. The player earns control, not another generator.

**First-party glue:** common telemetry aggregation and load-class registration. Existing cables, storage, transformers, and switches remain the grid.

**Inputs/outputs:** meters/peripherals, real IC2 storage, redstone-addressable breakers, and generation produce reserve forecasts, priority control, and black-start capability. **Placement/criticality:** Nuclear Age onward; optional specialization. **Automation:** automatic backup start, load shedding, and staged restoration. **Failure/recovery:** a tripped source, depleted reserve, overload, or failed actuator is reported with the affected bus; the player isolates, supplies, or restarts it. **Location assumptions:** useful everywhere, especially existing eclipse/dust environments. **Multiplayer:** supports a dedicated utility engineer. **Micromanagement:** summarize facility buses rather than every cable. **Scope:** Medium–High.

### B4. Infrastructure protection and recovery contracts

**Classification:** optional specialization. Scope: Medium–High.

Valuable remote facilities may offer authored protection/continuity programs using MFFS, Civil Defense, vehicles, ammunition, radar, lighting, and redundant supplies. Threats must come from current factions or from the player's decision to place assets in contested territory; no random meteor or invisible sabotage.

Military consumption becomes contextual: ammunition is used by an actual defended site, vehicles support patrol/repair access, and electronics/power operate radar and fields. Peaceful players can choose safer routes, diplomacy, redundancy, or nonmilitary projects.

**Inputs/outputs:** power, ammunition, electronics, vehicles, field/radar infrastructure, and optional faction standing preserve a chosen service and improve continuity. **Placement/criticality:** Heavy Industry onward; always optional. **Automation:** alarms, MFFS access control, radar, resupply, and emergency shutdown. **Failure/recovery:** depleted ammunition/power, damaged physical defenses, or hostile reputation suspends protection; repair, resupply, diplomacy, or rerouting recovers it. **Location assumptions:** only current faction/strategic-material behavior supports a site; no generic planetary attacks. **Multiplayer:** gives defense players an economic role. **Micromanagement:** no scheduled raids or daily patrol supplies; consume resources only through actual conflict/operation. **Scope:** Medium–High.

### B5. Capability-producing megaprojects

**Classification:** megaprojects. Scope: High to Very High.

Recommended projects are expansions of A1/A2, not ceremonial item sinks:

| Megaproject | Problem solved | Existing systems combined | Ongoing benefit | Engineering challenge |
| --- | --- | --- | --- | --- |
| Orbital freight ring/hub | Many launches/routes lack staging and buffers | Galacticraft cargo, controllers, storage, IC2 solar, ComputerCraft | Multi-route buffering, priority, dispatch | Power, loaded endpoints, throughput, congestion |
| Planetary reserve station | Dust/eclipse/reactor trips endanger critical sites | IC2 generation/storage/transformers, redstone, telemetry | Emergency power and black-start capability | Reserve sizing, isolation, recharge priority |
| Regional public-works exchange | Settlements are disconnected consumers | Railcraft, roads/vehicles, warehouses, service programs | Aggregates orders and distributes specialties | Batch scheduling and last-mile routing |
| Lunar precision campus | Moon loses purpose after Quantum gate | Existing lunar base, Robotic Cells, research, storage | Repeat precision/research service | Darkness scheduling and import substitution |
| Martian autonomous district | Autonomy ends at an archive | Local mining/manufacturing, habitats, cargo, AI policies | New expansion site that survives routine shortages | Local reserves, fallback production, policy conflicts |

The current Orbital Megastructure Controller can become a commissioning/control component for a real built complex. Its control record should acknowledge a functioning structure, not stand in for the entire structure.

**Inputs/outputs:** each project consumes construction suited to its physical function and produces an enduring capability listed above. **Placement/criticality:** Post-AI; all optional, though one operating-project proof may replace the current artifact-only capstone evidence. **Automation:** manifests, grid control, policies, and local production. **Failure/recovery:** insufficient service degrades or pauses a benefit; construction persists and can be recommissioned. **Location assumptions:** the table derives each placement from existing power, settlement, lunar, or Martian mechanics. **Multiplayer:** excellent multi-discipline projects. **Micromanagement:** projects use commissioning windows and broad service levels, not continual block-count submissions. **Scope:** High to Very High.

### B6. Continuing research programs

**Classification:** optional specialization. Scope: Medium.

Research Stations and Experiment Modules can accept small authored programs whose outputs improve visibility, efficiency ceilings, or project options—not a new linear tech tier. Examples include better route diagnostics, improved reserve forecasting, or alternative construction plans. They should consume environment-tagged observations and real operating data, with few repeat runs.

**Player fantasy/problem:** scientific infrastructure should keep informing engineering after it opens the next gate. **Inputs/outputs:** environment-tagged data, powered observations, and facility telemetry produce diagnostic or design capabilities rather than generic buffs. **Placement/criticality:** after each current archive; optional. **Automation:** schedule experiments and route data, but preserve observation floors. **Failure/recovery:** lost power or missing environment data pauses a run; restore and resume. **Location assumptions:** use only the existing environment-tag mechanism and actual research roles. **Multiplayer:** supports research specialists. **Micromanagement:** authored one-time/few-time programs, no infinitely repeatable research currency. **Scope:** Medium.

## 8. Tier C — experimental prototypes

### C1. Infrastructure condition and maintenance

The rain-rust/Repair Bench loop is understandable because the cause is visible and recovery is concrete. A very limited extension to heavily used freight terminals or settlement utilities could create spare-parts demand. Prototype cautiously: wear should be forecastable, slow, suspend a service rather than erase blocks, and be avoidable through good construction. Broad machine wear would become chores and should be rejected.

### C2. Policy-assisted expansion planning

An AI could compare a new site's declared service requirements with known reserves and produce a build/supply checklist. It must never place infrastructure or solve layout. This may help the civilization-architect fantasy, but risks becoming a menu-driven quest generator.

### C3. Route interruptions caused by physical world state

Rail damage, a deliberately shut terminal, missing chunk loading, or a destroyed controller can produce interesting failures because the player can inspect them. Prototype only deterministic state-based detection. Do not spawn random breakdowns or delete cargo.

### C4. Abstract population bands

Settlement demand could scale with a coarse inhabited/expanded/industrial status. This may make food, housing, utilities, and transport meaningful, but literal NPC counts and happiness meters would pull the game toward city management. If tested, population should be an outcome of physical services and only a source of broad demand/capability bands.

### Tier C evaluation matrix

| Prototype | Player fantasy | Existing systems / physical loop | Inputs → outputs | Placement / criticality | Automation, failure, recovery | Location / multiplayer | Micromanagement risk | Scope |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Limited infrastructure condition | Maintain a real working facility | Repair Bench, workshop rust, service programs; inspect and replace a forecast part | spare part + service access → restored throughput | Heavy Industry onward; optional | monitor condition; wear pauses service; replace part and resume | no planet lock; maintenance/logistics roles cooperate | Very high; reject if it spreads to ordinary machines | Medium |
| Policy-assisted expansion planning | Plan a new district as a systems architect | AI policy layer reads an authored service manifest; player still builds and connects it | known reserves/routes → actionable checklist | Post-AI; optional | recompute after design changes; impossible prerequisites are explained | all sites with manifests; useful planning collaboration | high if it becomes procedural task spam | Medium–High |
| Physical route interruption | Diagnose and restore infrastructure | rail, controllers, chunk loading, power, status telemetry | intact loaded route → delivery; physical break → visible outage | Freight mastery onward; optional | detect last successful segment; repair/load/reroute | strongest on distributed builds; supports dispatch/repair roles | unacceptable if random or cargo-destroying | High |
| Abstract population bands | Grow an inhabited industrial region | settlement service state and physical expansions; no literal citizen simulation | sustained services → broader demand/service tier | Post-1.0 settlement depth; optional | service policies maintain bands; shortage pauses growth; restoration resumes | settlement-specific, including post-AI Mars; builders/logistics cooperate | high if exposed as happiness meters or many per-capita goods | Very High |

## 9. Tier D — reject

| Rejected idea | Reason |
| --- | --- |
| Global “submit 50,000 iron” objectives | No simulated consumer, no new capability, and rewards raw repetition |
| Generic planet assignments such as Earth food/Moon quantum/Mars ore/orbit power | Only parts of this match the repository; hard assignment would be arbitrary and brittle |
| Mandatory all-to-all planetary trade | Creates route complexity without purpose |
| New post-AI machine or energy tier | The existing capability stack is already extensive; it would postpone rather than solve the operating-game gap |
| New universal logistics/storage block | Would obsolete BuildCraft, Logistics Pipes, Railcraft, vehicles, cargo controllers, and AE2 |
| Detached city-management screen | Hides the Minecraft infrastructure that should embody development |
| Random disasters, raids, breakdown rolls, or cargo loss | Produces punishment without an engineering cause |
| Broad durability/maintenance on every machine | Converts scale into chores and punishes automation |
| Infinite procedural contract/quest spam | Replaces sandbox goals with notifications and quotas |
| Prestige/reset loop | Destroys the civilization whose operation should be the endgame |
| Dozens of colony resources or intermediate parts | Violates material unification and adds clicks rather than decisions |
| Hard permanent import dependency for every outpost | Prevents autonomy and makes logistics tedious; startup dependency should usually be reducible |
| AI omniscience or automatic block placement | Removes the need for sensors, machines, networks, and player engineering |
| Mandatory combat attacks on infrastructure | Changes the pack's center of gravity and penalizes peaceful specialization |

## 10. Interplanetary economy and logistics analysis

### Derived commodity/service flows

These are recommendations, not claims that every flow is currently enforced:

| Source | Destination | Stage | Flow | Repository support | Persistence/substitution | Gameplay created |
| --- | --- | --- | --- | --- | --- | --- |
| Earth | Orbit | Chapters 7–8 | habitat supplies, construction materials, ordinary components | Earth owns the mature launch industry; orbit begins as a new station | Strong at startup; declines as orbital production expands | launch planning, receiving buffers, forgotten-cargo recovery |
| Orbit | Moon/Mars/Earth | Chapter 8 onward | player-chosen energy-intensive manufactured goods and route consolidation | 96/192 EU/t solar and no arbitrary craft multiplier | Entirely substitutable elsewhere; comparative advantage only | storage charging, burst scheduling, hub congestion |
| Earth/orbit | Moon | Chapter 9 | startup equipment and darkness reserve support | lunar chapter explicitly advises compact startup imports and orbital-power/cargo reuse | Declines with local lunar manufacture; emergency support persists | import substitution and reserve sizing |
| Moon | Earth/orbit/Mars projects | Chapters 10–16 | Meteoric Iron and occasional Lunar Quantum/precision products | actual Moon material and Moon-only quantum recipe | Key scientific uses remain; ordinary precision outputs should gain expensive alternatives | high-value low-volume freight, reason to retain lunar campus |
| Earth/orbit/Moon | Mars | Chapters 12–13 | startup systems and critical spares | Mars progression explicitly warns against repeated Earth trips and requires local production | declines as autonomy grows; emergency reserve persists | staged autonomy and fallback logistics |
| Mars | wider network | Chapters 14–16 | Desh samples/research records and optional specialized service outputs | actual Mars-only Desh, Martian data/archive | keep low-volume and evidence-based; do not invent a bulk-export mandate | strategic cargo protection and scientific supply |

The strongest persistent interplanetary dependency should be **services and reliability**, not compulsory bulk shuttling. New sites import startup capacity; mature sites substitute locally; certain high-value research/environment products remain worth moving; hubs and emergency reserves keep routes relevant.

### Transportation domains

| Technology | Best domain | Why it remains valid |
| --- | --- | --- |
| Player inventory | setup, diagnostics, emergency hand-carry | immediate and flexible, poor scale |
| BuildCraft/ProjectRed pipes | local deterministic movement | visible, physical, good around one plant |
| Logistics Pipes | smart local/regional request routing | item-aware control without replacing production |
| Railcraft | high-volume regional batch freight | geography, terminals, consists, and long routes |
| Roads/vehicles | exploration, construction support, flexible last mile, emergency bridge | low setup cost and player-driven access |
| Vehicle Service Carrier | mobile item/fluid support | already bridges vehicles to BuildCraft and mobile crafting |
| Galacticraft cargo | pre-AI/manual space logistics | fits launch-era constraints |
| Interplanetary Cargo Controller | scheduled loaded cross-dimensional freight | high-tech endpoint that can gain manifests/priority |
| AE2 | inventory knowledge and production scheduling | should feed terminals, not erase geography |

No player should be forced through every row. Service distance, volume, flexibility, and progression naturally select a sensible mode.

## 11. Civilization-scale power and resilience

The desired evolution can be achieved without a new energy system:

```text
machine packet safety
→ workshop generation/storage balance
→ factory transformer and buffer design
→ reactor baseload plus solar/other generation
→ critical/noncritical facility buses
→ remote reserve stations
→ multi-site status and policy
```

Recommended failure model:

| Failure | Cause | Detection | Prevention | Recovery | Automation response |
| --- | --- | --- | --- | --- | --- |
| Power deficit | load exceeds generation | falling storage and negative margin | more generation, scheduled loads | start backup or pause loads | shed low-priority lines |
| Reserve exhaustion | eclipse/dust/reactor trip lasts longer than design | estimated reserve duration | larger/local storage, alternate source | emergency import or black start | preserve habitat/comms reserve |
| Input starvation | upstream reserve/route empty | machine missing-input plus route backlog | source minima and local spares | dispatch or local substitute | request production/freight |
| Output congestion | destination full | output inventory and terminal queue | larger buffer or consumption | clear/reroute | pause upstream production |
| Cargo endpoint offline | chunk unloaded, power absent, controller damaged | last-delivery age and endpoint state | chunk loading, alternate route | load/repair/reroute | hold shipment and alert |
| Dependency cascade | one failure starves multiple lines | causal chain from consumers to source | segmentation and reserves | restore critical upstream service first | prioritize by declared class |
| Reactor shutdown | heat/control trip | existing telemetry | tested design and SCRAM | safe inspection/refuel/restart | isolate reactor and start backup |
| Habitat service loss | oxygen/power condition fails | existing detector and grid status | redundant supply and critical bus | restore power/oxygen, use suit | shed everything below habitat priority |

Failures suspend capabilities; they should not randomly destroy player work. Native IC2 overvoltage and reactor danger remain the deliberate exceptions because the player directly builds those hazardous systems and receives clear rules.

## 12. Player-facing civilization metrics

Metrics should answer engineering questions at a glance:

- **Facility:** online/degraded/offline; unmet service; critical/noncritical loads; recent reason for state change.
- **Power:** current generation, demand, stored EU, reserve duration at current load, largest legal packet seen, tripped/isolated loads.
- **Materials:** current reserve, minimum/preferred target, rate of change, first missing upstream input.
- **Freight:** source/destination, last delivery, recent items/minute, backlog, destination fullness, endpoint availability.
- **Settlement/service:** construction stage, remaining bounded bill, commissioning conditions, operating input, benefit currently enabled.
- **Automation:** policy active, last action, unresolved blocker, manual override state.

Avoid a global spreadsheet. Present a compact summary in the existing Factions & Settlements directory or a status command, while detailed data lives at physical controllers and through ComputerCraft. An alert should link the player to a site and a reason: “Mars Spares Depot below minimum; orbital route offline for 6 minutes,” not “Efficiency 72%.”

## 13. Cross-mod emergent engineering proposals

### Autonomous Regional Freight Terminal

**Capability:** move and dispatch bulk goods between a regional warehouse and local factories without a bespoke universal logistics block.

**Existing technologies:** Railcraft for long-distance freight; ComputerCraft for dispatch; IC2 for terminal power; Logistics Pipes/BuildCraft for local sorting; vehicles for emergency/last-mile delivery.

**First-party glue:** common manifest, route status, and service-program interface.

**Why the combination is better:** each system keeps a meaningful physical domain, and the player designs the yard, buffers, local routing, and recovery path.

### Resilient Settlement Utility

**Capability:** provide a settlement with commissioned, priority-aware electrical service.

**Existing technologies:** IC2 generation/storage/transformers; Energy Control displays; ProjectRed redstone; ComputerCraft monitoring; settlement upgrade/service state.

**First-party glue:** measure delivered service and expose critical-load/commissioning state.

**Why the combination is better:** the grid is a build, not a purchased “settlement power” block.

### Lunar Night Production Scheduler

**Capability:** keep habitat and research online through darkness while shifting energy-intensive manufacturing to sunlight.

**Existing technologies:** environmental solar, MFSU banks, Robotic Cells, ComputerCraft, ProjectRed controls, cargo buffers.

**First-party glue:** shared reserve/load telemetry and optional AI policy evaluation.

**Why the combination is better:** it emerges from real generation and storage behavior and allows several valid solutions, including larger storage, nuclear backup, reduced load, or imports.

### Martian Autonomous Spares Depot

**Capability:** maintain repair-component reserves and recover routine manufacturing after dust-related power reduction or supply interruption.

**Existing technologies:** local mining/manufacturing, Programmable/Robotic production, cargo controllers, IC2 storage, ComputerCraft, AE2 after AI.

**First-party glue:** reserve target, route manifest, and service status.

**Why the combination is better:** Mars autonomy becomes a physical system whose parts can be inspected and improved.

### Orbital Burst Manufacturing Hall

**Capability:** convert exceptional orbital solar into scheduled high-throughput manufacturing while preserving IC2 packet rules.

**Existing technologies:** environmental/tracking solar, MFSU banks, transformers/cables, Robotic Cells or endgame machines, ComputerCraft telemetry, cargo terminal.

**First-party glue:** none beyond optional facility status; the current aggregate legal-packet model already enables the capability.

**Why the combination is better:** it rewards an expert topology instead of granting an orbital crafting multiplier.

### Protected Interplanetary Logistics Hub

**Capability:** keep strategic cargo and communications online in a contested optional playstyle.

**Existing technologies:** MFFS, ICBM radar/defense, faction guards, Techguns production, IC2 power, cargo controllers, ComputerCraft alarms.

**First-party glue:** route/service health and optional defense contract attribution.

**Why the combination is better:** defense protects something economically useful and remains optional.

## 14. Quest implications

The questbook should teach each Tier A system once, using motivation → mechanic → action, then get out of the way.

### Civilization Service Programs

**Introductory quest: “Industry Has Clients.”** Motivation: a factory becomes civilization when its output lets another site do something it could not do. Mechanic: settlement/facility programs consume a bounded construction bill and require observable service. Action: inspect one site's program and connect its receiving inventory.

**Capability proof: “Commission a Public Service.”** Maintain the required goods and power through one commissioning interval and witness the physical/useful upgrade. No further settlement-upgrade quest chain is needed.

### Observable Utility and Freight Networks

**Introductory quest: “Design for the Shortage.”** Motivation: a route or grid is not reliable merely because it worked once. Mechanic: reserves, route health, and load classes reveal how long a site can operate. Action: register one source and destination buffer and display its status.

**Capability proof: “Recover Without Hand-Carrying.”** Trigger a safe, deterministic shortage by pausing a source or noncritical generator, then restore the service through reserve, rerouting, backup, or load shedding. The quest should not prescribe which solution.

### AI Industrial Policy

**Introductory quest: “State the Objective.”** Motivation: the AI Core should coordinate known infrastructure, not replace it. Mechanic: policies act only on registered sensors, routes, recipes, and controls. Action: set one minimum reserve and attach one production or freight actuator.

**Capability proof: “The System Responds.”** Let the reserve fall below target, observe the AI request a real production/delivery action, and restore the reserve without the player manually moving the item. This should replace item-only evidence for Predictive Production. More complex policies remain sandbox play.

## 15. Player-driven versus quest-driven placement

| Proposal | Classification | Quest treatment |
| --- | --- | --- |
| First civilization service commissioning | Critical progression/tutorial capability | Two concise teaching quests |
| Additional settlement/facility programs | Emergent sandbox | No quests; discover through sites/directory |
| Facility reserve and freight manifest | Tutorial capability | One setup and one recovery proof |
| Rail/vehicle/pipe/cargo specialization | Optional specialization | Existing side paths plus at most one terminal example |
| AI reserve/priority policy | Post-AI system | Two concise commissioning quests |
| Grid operations center | Optional specialization/megaproject | One optional capability proof |
| Orbital hub, lunar campus, Martian district | Megaprojects | One introductory project each at most; construction remains player-directed |
| Infrastructure defense | Optional specialization | Existing defense line can point to protected services |

## 16. Multiplayer implications

The recommended systems support natural roles without assigning classes:

- electrical engineers build generation, transformers, reserves, segmentation, and recovery;
- logistics players operate rail yards, cargo manifests, warehouses, and emergency routes;
- programmers build dashboards and policy integrations;
- industrial players scale material/component output;
- settlement builders commission and physically expand sites;
- orbital engineers stage power, cargo, habitats, and research;
- defense specialists protect chosen strategic infrastructure.

All objectives remain achievable in single-player by working on one subsystem at a time. Shared infrastructure needs permissions for policy actuators, route changes, and settlement project selection. Read-only status should be broadly shareable; control should follow owner/team authorization.

## 17. System interaction map

```text
extraction (Quarry / Walking Quarry / local off-world mining)
    ↓
IC2 processing + Railcraft steel + agriculture + fuel
    ↓
programmable manufacturing / Robotic Cells / AE2 scheduling
    ↓
local pipes ─ regional rail/roads ─ interplanetary cargo
    ↓                    ↓                    ↓
facility buffers ─ route manifests ─ location-specific sites
    ↓
civilization service programs
    ├─→ physical settlement/facility expansion
    ├─→ specialized trade, repair, research, and freight capability
    ├─→ continuing construction and operating demand
    └─→ reasons for remote infrastructure and protection
    ↓
larger and more distributed loads
    ↓
IC2 grid reserves, segmentation, load shedding, backup generation
    ↓
AI policies coordinate declared reserves/priorities through physical systems
    ↓
shortages reveal bottlenecks → player redesigns → civilization expands again
```

The three Tier A proposals are deliberately interdependent. Service programs create legitimate demand. Utility/freight observability turns demand into solvable engineering. AI policies reduce routine intervention only after the physical systems exist.

## 18. Risks and safeguards

| Risk | Safeguard |
| --- | --- |
| Settlement demand becomes item tax | Every bill corresponds to visible construction or active service; keep categories few and bounded |
| Continuous upkeep becomes chores | Consume only while service operates; use large buffers and slow rates; allow automation before scale |
| Cargo controller obsoletes rail/pipes | Preserve domains, low baseline cadence, physical buffers, and parallel terminal scaling |
| Rail becomes mandatory | Accept alternative transport where volume/distance permits; reward rail's domain rather than hard-gate it |
| AI becomes magic | Limit sensing/action to registered peripherals, inventories, routes, recipes, and redstone controls |
| AI editor becomes spreadsheet | Small policy vocabulary, presets, local terminals, advanced Lua as optional depth |
| Planet roles become arbitrary locks | Require repository evidence for location-specific recipes/conditions; use comparative advantages otherwise |
| Failure feels punitive | Deterministic causes, early warnings, persistent progress, safe suspension, clear recovery |
| Too many generated objectives | Programs exist at authored site types and are discovered in-world; no endless quest notifications |
| Feature scope threatens 1.0 | Implement one vertical slice of each Tier A system and defer program breadth/megaproject variety |

## 19. Minimum complete 1.0 set

The four hypotheses in the brief are mostly correct, but they can be reduced to three implementable systems:

1. **Civilization-scale economic demand is essential**, but it should be expressed as service programs with visible construction and useful operation—not a general contract generator.
2. **Meaningful large-scale logistics and power observability are essential together.** Demand without route/grid state becomes busywork; observability without demand becomes a dashboard toy.
3. **AI Age must change the player's level of control.** A small reserve/priority policy layer is more important than adding any post-AI machine.

“Continuing usefulness of multiple environments” is an outcome of those systems, not a fourth standalone feature. Earth settlements, orbital power/hubs, lunar precision/darkness, and Martian autonomy become useful because site programs, freight, reserves, and policies exploit their existing differences. Do not bolt a new commodity onto each planet merely to satisfy a symmetry requirement.

For the smallest credible 1.0 vertical slice:

- extend one Earth settlement through one post-tier-3 service program;
- give that program a bounded construction bill, real EU/service condition, physical upgrade, and useful output;
- expose one source/destination freight manifest and one facility power reserve/load class;
- adapt the same framework to one existing off-world facility, preferably Mars because autonomy already teaches shortage and load response;
- replace the item-only Predictive Production proof with one real AI reserve policy response;
- make one colony or megastructure proof depend on a commissioned operating facility rather than only producing a charter/record.

That is enough to demonstrate the complete loop. More site programs, terminals, defense contracts, and megaprojects can follow without holding 1.0 hostage.

## 20. Recommended implementation order

1. **Define the service contract model.** Specify bounded construction inputs, operating conditions, benefits, failure state, persistence, and physical expansion hooks. Use the current settlement ledger as the first implementation target.
2. **Make state observable.** Add player-facing and ComputerCraft-readable service, reserve, route, and failure fields. Without this, later automation will be opaque.
3. **Implement one Earth vertical slice.** Extend a tier-3 primitive settlement or one industrial site into a useful service, using actual inventories and IC2 delivery.
4. **Add freight manifests and load classes.** Extend existing cargo/controller behavior; integrate local/rail alternatives through a common status contract rather than replacing them.
5. **Implement deterministic recovery tests.** Validate input starvation, output congestion, power deficit, route offline, and recovery without loss.
6. **Implement the AI policy evaluator.** Start with minimum reserve, priority, and preserve-emergency-power. Require explicit sensors and actuators.
7. **Replace weak Chapter 16 evidence.** Make AI Factory Coordination, Predictive Production, colony, and megastructure milestones prove operating behavior proportional to their names.
8. **Add one off-world vertical slice.** Mars is the best first target; follow with orbital hub or lunar campus only after the generic system works.
9. **Expand optional programs and megaprojects.** Add regional terminals, protection contracts, and authored location programs as post-1.0 breadth.

Do not begin with AI menus or a large library of contracts. The physical economy and its telemetry must exist first, or the AI will have nothing meaningful to coordinate.

## 21. Repository evidence index

This index makes the baseline claims easy to re-check during design:

| Topic | Primary repository evidence |
| --- | --- |
| Release and high-level philosophy | `manifest/pack-version.json`; `docs/GAME_DESIGN_DOCUMENT.md`; `README.md` |
| Exact 16-chapter order and milestones | `progression/chapters/*.json`; `progression/progression-graph.json`; `docs/PROGRESSION_OVERVIEW.md` |
| Ten visible side paths and current quest wording | `config/betterquesting/DefaultQuests.json`; `docs/QUEST_IMPLEMENTATION.md`; `progression/side-paths/*.json` |
| Advancement structure | `progression/unified-advancements.json`; `docs/UNIFIED_ADVANCEMENTS.md` |
| Pacing and infrastructure reuse | `progression/pacing.json`; `progression/optimization-profiles.json`; `docs/OPTIMIZATION_OPPORTUNITIES.md` |
| IC2 EU, voltage, machine work, and MFSU bursts | `docs/IC2_NATIVE_POWER_SCALING.md`; `docs/ENERGY_INTEROPERABILITY.md`; `IndustrialMachineKind.java`; `TileIndustrialMachine.java` |
| Exact first-party processing and environment recipes | `MachineRecipe.java`; `docs/RESEARCH_PROGRESSION.md`; `docs/GAME_DESIGN_DOCUMENT.md` |
| Material unification and recipe policy | `docs/ITEM_UNIFICATION_AUDIT.md`; `groovy/postInit/industrial_material_unification.groovy`; `groovy/postInit/dimension_material_unification.groovy` |
| Settlement growth, production, and consumption | `SettlementEconomySystem.java`; `CivilizationWorldGenerator.java`; `docs/NPC_FRAMEWORK.md` |
| Factions, IC Credits, reputation, and markets | `FactionSystem.java`; `MarketEconomy.java`; `config/industrialcivilization/faction-system.json`; `docs/FACTIONS.md` |
| Roads, vehicles, service carrier, city exchange | `docs/VEHICLES_AND_NATIONS.md`; `VehicleIntegrationSystem.java`; `TileVehicleServiceDock.java`; `CivilizationWorldGenerator.java` |
| Cargo behavior | `TileIndustrialMachine.java` (`transferCargo`); Chapter 16 milestones; current cargo quest descriptions |
| Environment power and survival | `TileEnvironmentalSolarArray.java`; `SpaceSurvivalSystem.java`; `docs/ORBITAL_STATION.md`; `docs/LUNAR_PROGRAM.md`; `docs/MARS_PROGRAM.md` |
| Moon role | Chapters 9–11; `docs/MOON_PROGRESSION.md`; `MachineRecipe.java` |
| Mars role and post-AI transformation | Chapters 12–14; `PlanetaryUpgradeSystem.java`; `CivilizationWorldGenerator.java`; `docs/MARTIAN_AUTONOMY.md` |
| AI/AE2 and post-AI systems | Chapters 15–16; `docs/AI_AGE.md`; `docs/AI_AGE_LOCK.md`; `docs/POST_AI_ENDGAME.md`; `MachineRecipe.java` |
| Agriculture | side paths 03–04; `docs/ITEM_UNIFICATION_AUDIT.md`; `groovy/postInit/industrial_foregoing_lv.groovy` |
| Walking Quarry cross-mod pattern | side path 05; `TileMobileQuarryController.java`; `MobileQuarryRules.java` |

## Final design answer

Industrial Civilization becomes a game about building and operating a civilization when the things the player has built begin to depend on one another in understandable, useful, and recoverable ways.

The factory should not consume steel because a quest wants a number. It should consume steel because a settlement is building a freight annex. That annex should need power because it loads cargo. Its cargo should matter because a lunar or Martian facility has a reserve target. A shortage should expose whether the weak link is production, storage, grid capacity, or transport. AI should respond only through the sensors and controls the player installed. When the response is inadequate, the player's next goal should emerge naturally: enlarge the buffer, add a train, separate the grid, build local production, commission another terminal, or redesign the policy.

That loop uses almost everything Industrial Civilization already has. It gives settlements, roads, vehicles, rail, cargo, IC2 grids, ComputerCraft, AE2, multiple environments, defense, and megaprojects reasons to reinforce one another. Most importantly, it moves the late-game fantasy from **owning advanced artifacts** to **being the architect of a living industrial system**.
