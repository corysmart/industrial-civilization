# Final Manual Test Checklist

## Startup

1. In Technic Launcher, select **Industrial Civilization: Astra** at `/Users/cory/Library/Application Support/technic/modpacks/industrial-civilization-astra`.
2. Confirm 64-bit Java 8 and allocate at least 6 GiB; 8 GiB is recommended for the complete pack.
3. Launch once and wait at the title screen. Do not open the pre-existing `New World` save.
4. Confirm the menu label reads `Industrial Civilization v0.4.0` and inspect Loaded Mods for the expected mod IDs, including Onysd Vehicles 1.4.0, Obfuscate 0.4.2, ICBM Classic 6.5.5, Connected Glass 1.1.8, and SuperMartijn642 Core Lib 1.1.21.
5. Open a disposable world, run `/gs reload --clean`, and confirm chat reports a successful reload with no errors. Review `logs/groovy.log` if it does not.
6. Confirm the welcome chat identifies **F6** as the Industrial Civilization Guide key. Press F6 and verify the Industrial Civilization home banner renders without magenta missing-texture panels, then inspect the 16 chapters and 7 side-path tabs.
7. Open the in-game pause menu. Confirm **Advancements** opens the vanilla advancement screen, then verify its Industrial Civilization tab contains the visible, ordered progression tree. F6 remains the Better Questing guide.
8. Open Options → Controls and confirm the Conflicts section is empty. On macOS, spot-check F6 (guide), Option+Y (Techguns reload), Command+R (GroovyScript reload), Option+R (inventory sort), and Command+[/]/;/0/' (Waila).
9. Enter Creative inventory and locate the **Industrial Civilization** tab. Confirm all 30 custom blocks (including the 12 independently placeable workshop architecture components) and all 23 custom items appear. Use the tab-page arrows to confirm **Applied Energistics 2** and Onysd Vehicles are also present; all namespaces must appear in the Search tab.

## Disposable world

- Create a new disposable survival world.
- Open the quest UI and confirm all 16 chapters and 7 independent side-path tabs appear in order, including Factions & Salvage and Strategic Defense.
- Place and roof the Car Workshop, manufacture each of the six curated vehicle crates, then drive each vehicle with W/A/S/D and test Option+K horn/Option+L seat cycling.
- Park the Passenger Carrier, verify its 54-slot inventory, mobile crafting grid, 64,000 mB tank, and item/fluid transfers through a Vehicle Service Dock.
- Expose a Car Workshop to rain, verify it rusts and stops, then repair it at a Repair Bench for exactly one IC2 Machine Block.
- In a fresh world, verify dirt-to-paved regional roads, real IC2 utility spines/outlets, and a live specialty-item exchange between two loaded city cargo controllers.
- Confirm ProjectE/EMC searches return no active items. Before AI, AE2 recipes must be absent; after AI, the twelve foundation recipes and captured full catalog must appear.
- Confirm Gold, Diamond, Crystal, and Obsidian Chest recipes are absent; Copper/Iron remain.
- Confirm pistol, combat shotgun, M4, and ammunition recipes resolve without missing ingredients.
- Place/energize Techguns Ammo Press and one representative gun; verify reload/fire behavior in a safe test area.
- In Creative search, confirm no Zombie, Skeleton, Creeper, Spider, Enderman, Witch, Slime, or other vanilla hostile spawn eggs are listed. Confirm the replacement eggs are named **Spawn Robber** and **Spawn Territorial Militia Patrol**, and produce fully visible human-rendered Robbers and patrol riflemen. Listen while idle, walking, injured and killed: there must be no zombie groans, skeleton rattles or other original-mob audio; only cloth steps and human injury/death sounds are allowed. During two survival nights, confirm robber density normally remains at or below four within a 64-block area, rejected zombie attempts do not remain as zombies, distant robbers naturally despawn, and organized squads are uncommon. Confirm skeleton attempts produce no skeleton or patrol more than 128 blocks from a registered militia outpost; inside that radius confirm no more than six natural patrols accumulate and distant patrols despawn normally. Verify an unarmed player is ignored by militia, a gun anywhere in inventory triggers militia fire, sword rushing is lethal, an arrow causes local militia aggro/reputation loss, fall/cactus/TNT trap kills do not, and three separately dismantled outposts trigger militia hostility. Separately confirm Civil Defense officers in cities/honorable factories ignore guns and militia history but become hostile after sufficiently negative Riverside/Survey/Civil Defense standing.
- Break a natural tree with a Stone Axe and an IC2 Chainsaw; verify the full connected tree falls and durability/EU is charged once per log. Repeat while sneaking and confirm only the selected log breaks.
- Repeat tree felling with an irregular branching tree, at least one modded `isWood`/`isLeaves` tree, and a generated tree above 96 logs. Confirm a claimed/protected log cancels normally and the 12-block/tick queue does not bypass the protection. Observe server tick time and mass drops while processing the large tree.
- Mine a flat stone wall with IC2 Mining Drill metadata 0 and Diamond Drill metadata 1; verify perpendicular 3×3 and 9×9 areas, per-block EU use, normal drops, and sneaking precision mode.
- Repeat drill tests against floor, ceiling and two wall axes. Test energy sufficient for only part of the plane, Fortune/Silk Touch/Unbreaking where supported, mixed ores, protected blocks, tile entities and a full inventory. Verify normal drops and that work spans multiple ticks without a long server stall.
- Use the NPC Wand to instantiate the three faction blueprints; verify neutral trade, hostile ranged combat, warning/trespass hostility, and persistence after reload.
- Verify IC2 energy values and reactor metadata through Plethora; test SCRAM with no fuel first.
- Verify Galacticraft's map permits only currently reachable orbit/Moon/Mars destinations, never the End/Venus/Asteroids. Confirm Mars Mission Authorization directly unlocks the Tier 2 Rocket NASA Workbench page without a Moon dungeon.
- Craft the Analyzer, charge it from IC2 EU, and consume exactly 6,250 EU each for Earth Iron, lunar Meteoric Iron, and Martian Desh. Inspect all three origin-tagged records and confirm comparative research completes only after the set.
- In orbit/Moon/Mars, confirm radiation begins damaging an exposed player after 30 seconds, then stops inside a roofed room served by an active sealed Oxygen Sealer or while wearing a full IC2 QuantumSuit.
- Repeat radiation protection in irregular rooms, a large base, two connected sealed rooms, at a loaded/unloaded chunk boundary, and with configured modded sealing blocks. Test missing, depleted and damaged QuantumSuit pieces. Ride or drive a moving entity across breathable-air boundaries and confirm protection follows the player's live AABB.
- In orbit, Moon and Mars, seal a room and confirm a placed Galacticraft Oxygen Detector reaches its active/redstone state. Player oxygen exposure or a running sealer without an active detector must not complete habitat quests. Keep the detector and all required infrastructure operating together for two continuous minutes; confirm mining/manufacturing/communications and functional-base mastery trigger from telemetry, record once, and reset their stability streak when any required component stops.
- Deposit upgrade materials into inventories within 24 blocks of a primitive settlement. Confirm no random upgrade occurs, only 16 items are absorbed per cycle, the documented bill is consumed, and the settlement constructs exactly one expansion. Complete a real IC Credit trade and confirm circulation reaches the nearest settlement ledger.
- Visit already-generated Mars chunks before AI, unlock AI, then reload those chunks. Confirm they receive deterministic bounded processing and do not duplicate structures after further reloads.
- Visit all six Apollo marker coordinates and verify mission name, real landing date, latitude/longitude plaque, United States flag, no loot, and no ordinary Moon mobs or structures.
- Place and craft each workshop architecture block independently, then deploy both workshop controllers and confirm their generated frames, floors, hazard stripes, cable covers, tool walls and cabinets use the same blocks.
- Inspect every faction villager plus robber and militia patrol under daylight, darkness and armor. Confirm skins fit the correct entity UV model and remain faction-readable at normal distance.
- Time world entry and confirm the warmup overlay cannot finish before 15 seconds, releases when stable, and always times out by 30 seconds or allows the explicit safe skip.
- Restart the client and confirm the main menu shows the Industrial Civilization title plate and widescreen industrial-city background, the window title says `Industrial Civilization`, and no Tekkit promotional links remain.
- Reach AI entry and confirm the scrolling Industrial Civilization credits prominently include `corysmart`, open once, and close back to the playable world. Craft the custom-rendered Technical Phase Pearl afterward and verify its quest/advancement completes; no chapter before AI may require a pearl or eye.
- Attach a wired modem to the Analyzer and call all documented methods.

If startup fails, return `logs/latest.log`, the newest file in `crash-reports/`, and the visible Technic error.
