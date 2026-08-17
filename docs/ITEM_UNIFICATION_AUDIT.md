# Pack-wide material and ecology audit

Industrial Civilization uses one visible technical vocabulary when two items
perform the same job. A native component remains when it teaches a distinct
machine, chemistry, research, vehicle, weapon, or spaceflight mechanic. Old
registry entries are hidden and made uncraftable rather than deleted when
deletion would corrupt existing saves.

## Canonical material mapping

| Role | Canonical pack material | Pack-wide decision |
|---|---|---|
| Copper, tin, bronze, lead, uranium | IC2-compatible OreDict families | UniDict and pack scripts choose one ingot/dust provider; Techguns ore and duplicate metal providers are disabled. |
| Steel | Railcraft steel through `ingotSteel` / `plateSteel` | Retained as the established coke/blast-furnace process; Techguns and ICBM duplicate steel pieces are disabled or hidden. |
| Rubber insulation | IC2 Rubber (`ic2:itemmisc:450`, `itemRubber`) | Industrial Foregoing Plastic is removed from `itemRubber`; it remains a distinct polymer only where IF mechanics genuinely require plastic. |
| Generic adhesive | IC2 Sticky Resin (`ic2:itemharz`) | Every inherited crafting-table Slimeball ingredient is replaced. Slimeballs and Ghast Tears are hidden from HEI. |
| Early renewable fiber | IC2 Hemp (`ic2:itemmisc:159`) | Native Hemp-to-String is the normal route; spider drops are not part of Earth ecology. |
| Basic / advanced circuits | IC2 Electronic Circuit (`451`) / Advanced Circuit (`452`) | ICBM duplicate circuit tiers are hidden and uncraftable. First-party Control Processors and AE2 processors remain distinct because they represent programmable and network manufacturing. |
| Portable batteries / wire | IC2 storage items and cables | ICBM duplicate batteries and wires are hidden; ICBM launch infrastructure consumes IC2 MV/HV parts. ProjectRed logic wire remains because it implements a distinct bundled-redstone system. |
| Structural alloy | IC2 Advanced Alloy (`ic2:itemmisc:257`) | Replaces inherited Ghast Tear crafting dependencies; unique Galacticraft rocket metals and Railcraft components remain native. |
| Silicon / technical crystal | AE2 Certus Quartz Crystal (`appliedenergistics2:material:0`) | Replaces every inherited Nether Quartz ingredient; Certus ore is available in the Overworld. |
| Nether masonry / soil | Cobblestone, Sand, Brick | Netherrack, Soul Sand, and Nether Brick recipe inputs use their ordinary Overworld structural equivalents. |
| Nether biology / heat | IC2 Hemp, Carbon Plate, Coal Dust, Sticky Resin | Nether Wart, Blaze Rod, Blaze Powder, and Magma Cream inputs become farmed or manufactured IC2 materials. |
| Luminous circuit reagent | Redstone | Replaces Glowstone Dust recipe inputs so lighting and circuit chains remain Overworld-accessible. |
| Wither-scale artifact | Lunar Quantum Component | Replaces Nether Stars and preserves their endgame role behind lunar precision manufacturing. |
| End geology / phase matter | Obsidian, Technical Phase Pearl, Advanced Alloy, Stone Brick, Energy Crystal | Replaces End Stone, Chorus Fruit, Popped Chorus, Purpur, and End Crystal inputs. Shulker storage recipes are disabled instead of bypassing the storage progression. |
| Gunpowder | Two coal dust plus redstone | Supplies an explicit industrial route because Creepers are suppressed on Earth. |
| Leads | Four Hemp-derived String plus one Sticky Resin -> two Leads | Early, renewable, and discoverable without hostile mobs or diamond-tier machinery. |

## Hostile-drop acquisition audit

| Drop | Status and replacement policy |
|---|---|
| Bone | Adult cattle, pigs, sheep, and horses killed directly by a player have a supplemental 1-in-8 chance to drop one bone. Existing drops remain unchanged; children, pets, civilians, faction NPCs, and non-player deaths do not qualify. |
| String | IC2 Hemp is a tier-3 crop, grows at light level 7+, matures at stage 6, and regrows from stage 4. Melon is a strong accessible parent because its Green trait and properties score closely against Hemp; Melon plus a different tier-2/3 crop such as Cocoa is a practical parallel-plot search. No grass-drop shortcut is added. Its native recipe supplies String. |
| Slimeball | No generic recipe consumes it. Sticky Resin is the adhesive vocabulary; biological Slimeball items remain registry-compatible but hidden. |
| Ghast Tear | No crafting-table recipe requires it. Advanced Alloy is the technical structural replacement. |
| Gunpowder | The coal-dust/redstone blend supplies ammunition and explosive recipes without Creepers. |
| Ender Pearl | Natural acquisition is suppressed. The registry item is deliberately retained as the AI-age Technical Phase Pearl manufactured through the durable AI Core route. |
| Blaze Rod / Powder and Magma Cream | No crafting-table recipe requires them. Carbon Plate, Coal Dust, and Sticky Resin express the same heat/structure/seal roles through visible IC2 production. |
| Spider Eye | Retained only for biological/potion semantics; it is not a required progression item or generic adhesive. |
| Nether Star | No crafting-table recipe requires it. The Moon-manufactured Lunar Quantum Component is the controlled endgame substitute. |
| Other Nether / End materials | Recipe inputs are translated to the mapping above. Elytra dyeing and duplication remain because they already require an Elytra and cannot gate unrelated technology; engineered Ender Pearls remain as Technical Phase Pearls. |
| Galacticraft creature drops | Native off-world ecology is retained only where Galacticraft allows those creatures; Moon monsters are suppressed and Mars pre-AI ecology remains Galacticraft-native. No such drop is required by the numbered quest line. |

## Duplicate chains removed or retained

- ICBM circuits, batteries, wires, steel ingots, clumps, and plates are hidden
  and their recipes removed; launch hardware uses IC2 infrastructure directly.
- Techguns duplicate metal providers and ore generation remain disabled.
- Galacticraft Venus lead is removed from the early lead OreDict shortcut.
- Industrial Foregoing Plastic no longer masquerades as IC2 Rubber.
- Native Galacticraft rocket materials, AE2 processors, Railcraft coke/steel and
  track parts, ProjectRed bundled logic, vehicle chassis parts, weapon
  mechanisms, and first-party research artifacts remain because each carries a
  distinct gameplay role.

`tools/audit_item_unification.py` enforces the mapping, recipe replacements,
renewable bone rule, real-item agriculture quest icons, and absence of
dimension-locked quest requirements. `groovy/postInit/material_ecology_audit.groovy`
reports the final live Forge recipe registry after every mod and script has run.
